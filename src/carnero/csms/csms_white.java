package carnero.csms;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import java.util.Locale;

public class csms_white extends AppWidgetProvider {
	private final base csmsBase = new base();
	private final Locale loc = Locale.getDefault();

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

			csmsBase.refresh(base.WHITE, context, address, text);
		} else if (intent.getAction().equalsIgnoreCase("csmsTouch") == true) {
			csmsBase.refresh(base.WHITE, context);

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
}
