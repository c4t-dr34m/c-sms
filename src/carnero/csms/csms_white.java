package carnero.csms;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class csms_white extends AppWidgetProvider {
	private final base csmsBase = new base();

	@Override
	public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
		try {
			csmsBase.refresh(base.WHITE, context, ids);
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

			(new refresh(context, address, text)).start();
		} else if (intent.getAction().equalsIgnoreCase("csmsUpdate") == true) {
			(new refresh(context)).start();
		} else if (intent.getAction().equalsIgnoreCase("csmsTouch") == true) {
			try { // start default android's messaging application (will work on pure android only)
				final Intent intentMsg = new Intent(Intent.ACTION_MAIN);
				intentMsg.setClassName("com.android.mms", "com.android.mms.ui.ConversationList");
				final PendingIntent intentPending = PendingIntent.getActivity(context, 0, intentMsg, 0);
				intentPending.send();
			} catch (Exception e) {
				// nothing
			}

			(new refresh(context)).start();
		}

		super.onReceive(context, intent);
	}

	private class refresh extends Thread {
		private Context ctx = null;
		private String add = null;
		private String txt = null;

		public refresh(Context context) {
			ctx = context;
		}

		public refresh(Context context, String address, String text) {
			ctx = context;
			add = address;
			txt = text;
		}

		@Override
		public void run() {
			if (add == null && txt == null) {
				csmsBase.refresh(base.WHITE, ctx);
			} else {
				csmsBase.refresh(base.WHITE, ctx, add, txt);
			}
		}
	}
}
