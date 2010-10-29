package carnero.csms;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;
import java.util.ArrayList;
import java.util.Locale;

public class base {
	private final Locale loc = Locale.getDefault();

	public void refresh(Context context) {
		refresh(context, null, null, null);
	}

	public void refresh(Context context, String address, String text) {
		refresh(context, null, address, text);
	}

	public void refresh(Context context, int[] ids) {
		refresh(context, ids, null, null);
	}

	public void refresh(Context context, int[] ids, String intentAddress, String intentText) {
		String person = null;
		String address = null;
		String text = null;

		if (context == null) {
			return;
		}

		final AppWidgetManager manager = AppWidgetManager.getInstance(context);
		final RemoteViews viewsWhite = new RemoteViews("carnero.csms", R.layout.layout_white);
		final RemoteViews viewsBlack = new RemoteViews("carnero.csms", R.layout.layout_black);

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

				if (msgs.isEmpty() == false) {
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
				}
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
					viewsWhite.setTextViewText(R.id.sender, person.toLowerCase(loc));
					viewsBlack.setTextViewText(R.id.sender, person.toLowerCase(loc));
				} else {
					viewsWhite.setTextViewText(R.id.sender, address.toLowerCase(loc));
					viewsBlack.setTextViewText(R.id.sender, address.toLowerCase(loc));
				}
				viewsWhite.setTextViewText(R.id.message, text.toLowerCase(loc));
				viewsBlack.setTextViewText(R.id.message, text.toLowerCase(loc));
			} else {
				viewsWhite.setTextViewText(R.id.sender, null);
				viewsBlack.setTextViewText(R.id.sender, null);
				viewsWhite.setTextViewText(R.id.message, null);
				viewsBlack.setTextViewText(R.id.message, null);
			}

			// set pendingintent on click
			final Intent intentWidWhite = new Intent(context, csms_white.class);
			intentWidWhite.setAction("csmsTouch");
			final PendingIntent intentPendingWhite = PendingIntent.getBroadcast(context,  0, intentWidWhite, 0);
			viewsWhite.setOnClickPendingIntent(R.id.widget, intentPendingWhite);

			final Intent intentWidBlack = new Intent(context, csms_black.class);
			intentWidBlack.setAction("csmsTouch");
			final PendingIntent intentPendingBlack = PendingIntent.getBroadcast(context,  0, intentWidBlack, 0);
			viewsBlack.setOnClickPendingIntent(R.id.widget, intentPendingBlack);

			if (ids != null && ids.length > 0) {
				final int idsCnt = ids.length;

				for (int i = 0; i < idsCnt; i++) {
					manager.updateAppWidget(ids[i], viewsWhite);
				}
				for (int i = 0; i < idsCnt; i++) {
					manager.updateAppWidget(ids[i], viewsBlack);
				}
			} else {
				final ComponentName componentWhite = new ComponentName(context, csms_white.class);
				final ComponentName componentBlack = new ComponentName(context, csms_black.class);
				
				manager.updateAppWidget(componentWhite, viewsWhite);
				manager.updateAppWidget(componentBlack, viewsBlack);
			}
		} catch (Exception e) {
			// nothing
		}
	}
}
