package carnero.csms;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import java.util.ArrayList;
import java.util.Locale;

public class csms extends AppWidgetProvider {
	private final Locale loc = Locale.getDefault();

	@Override
	public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
		try {
			refresh(context, ids);
		} catch (Exception e) {
			// nothing
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equalsIgnoreCase("csmsNewMessage") == true) {
			final Bundle bundle = intent.getExtras();
			final String address = bundle.getString("address");
			final String text = bundle.getString("text");

			refresh(context, address, text);
		} else if (intent.getAction().equalsIgnoreCase("csmsTouch") == true) {
			refresh(context);

			try { // start default android's messaging application (will work on pure android only)
				final Intent intentMsg = new Intent(Intent.ACTION_VIEW);
				intentMsg.setClassName("com.android.mms", "com.android.mms.ui.ConversationList");
				final PendingIntent intentPending = PendingIntent.getActivity(context, 0, intentMsg, PendingIntent.FLAG_UPDATE_CURRENT);
				intentPending.send();
			} catch (Exception e) {
				// nothing
			}
		}

		super.onReceive(context, intent);
	}

	private void refresh(Context context) {
		refresh(context, null, null, null);
	}

	private void refresh(Context context, String address, String text) {
		refresh(context, null, address, text);
	}

	private void refresh(Context context, int[] ids) {
		refresh(context, ids, null, null);
	}

	private void refresh(Context context, int[] ids, String intentAddress, String intentText) {
		String person = null;
		String address = null;
		String text = null;

		if (context == null) {
			return;
		}

		final AppWidgetManager manager = AppWidgetManager.getInstance(context);
		final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout);

		try {
			if (intentAddress == null || intentText == null) {
				ArrayList<message> msgs = new ArrayList<message>();
				int count = 0;
				Cursor cursor = context.getContentResolver().query(
						Uri.parse("content://sms/inbox"),
						new String[] {"_id", "thread_id", "address", "person", "date", "body"},
						null,
						null,
						"date desc"
				);

				if (cursor != null) {
					try {
						count = cursor.getCount();

						Log.i("c:sms", "Messages found in database: " + count);

						if (count > 0) {
							cursor.moveToFirst();

							do {
								message msg = new message();

								msg.id = cursor.getLong(cursor.getColumnIndex("_id"));
								msg.thread = cursor.getLong(cursor.getColumnIndex("thread_id"));
								msg.address = cursor.getString(cursor.getColumnIndex("address"));
								msg.date = cursor.getLong(cursor.getColumnIndex("date"));
								msg.text = cursor.getString(cursor.getColumnIndex("body"));

								msgs.add(msg);
							} while (cursor.moveToNext() != false);
						}
					} finally {
						cursor.close();
					}
				}

				for (message msg : msgs) {
					Cursor cursorContact = context.getContentResolver().query(
							Uri.withAppendedPath(
									Uri.parse("content://com.android.contacts/phone_lookup"),
									Uri.encode(msg.address)
							),
							new String[] {"_id", "display_name"},
							null,
							null,
							null
					);

					if (cursorContact != null) {
						try {
							if (cursorContact.moveToFirst()) {
								msg.person = cursorContact.getString(cursorContact.getColumnIndex("display_name"));
							}
						} finally {
							cursorContact.close();
						}
					}
				}

				final message msg = msgs.get(0);

				address = msg.address.toLowerCase(loc);
				person = msg.person.toLowerCase(loc);
				text = msg.text.toLowerCase(loc);
			} else {
				address = intentAddress.toLowerCase(loc);
				text = intentText.toLowerCase(loc);

				Cursor cursorContact = context.getContentResolver().query(
						Uri.withAppendedPath(
								Uri.parse("content://com.android.contacts/phone_lookup"),
								Uri.encode(intentAddress)
						),
						new String[] {"_id", "display_name"},
						null,
						null,
						null
				);

				if (cursorContact != null) {
					try {
						if (cursorContact.moveToFirst()) {
							person = cursorContact.getString(cursorContact.getColumnIndex("display_name"));
						}
					} finally {
						cursorContact.close();
					}
				}
			}

			// display sms info


			if ((person != null || address != null) && text != null) {
				if (person != null && person.length() > 0) {
					views.setTextViewText(R.id.sender, person.toLowerCase(loc));
				} else {
					views.setTextViewText(R.id.sender, address.toLowerCase(loc));
				}
				views.setTextViewText(R.id.message, text.toLowerCase(loc));
			} else {
				views.setTextViewText(R.id.sender, "");
				views.setTextViewText(R.id.message, "");
			}

			// set pendingintent on click
			final Intent intentWid = new Intent(context, csms.class);
			intentWid.setAction("csmsTouch");
			final PendingIntent intentPending = PendingIntent.getBroadcast(context,  0, intentWid, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.widget, intentPending);

			if (ids != null && ids.length > 0) {
				final int idsCnt = ids.length;

				for (int i = 0; i < idsCnt; i++) {
					manager.updateAppWidget(ids[i], views);
				}
			} else {
				final ComponentName component = new ComponentName(context, csms.class);
				
				manager.updateAppWidget(component, views);
			}
		} catch (Exception e) {
			// nothing
		}
	}
}
