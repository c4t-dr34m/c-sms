package carnero.csms;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class notify extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			// read message data
			String address = null;
			String text = null;
			Long date = null;

			final Bundle msgBundle = intent.getExtras();
			if (msgBundle != null) {
				final Object[] msgData = (Object[])msgBundle.get("pdus");
				final int msgCount = msgData.length;
				final SmsMessage[] messages = new SmsMessage[msgCount];
				for (int i=0; i < msgCount; i ++){
					messages[i] = SmsMessage.createFromPdu((byte[]) msgData[i]);
				}

				if (messages.length > 0) {
					for (SmsMessage msg : messages) {
						if (date == null || msg.getTimestampMillis() > date) {
							address = msg.getDisplayOriginatingAddress();
							text = msg.getDisplayMessageBody();
							
							date = msg.getTimestampMillis();
						}
					}
				}
			}

			// fire widget update
			final Intent intentWid = new Intent(context, csms.class);
			intentWid.setAction("csmsNewMessage");
			if (address != null && text != null) {
				intentWid.putExtra("address", address);
				intentWid.putExtra("text", text);
			}
			final PendingIntent pendingIntent = PendingIntent.getBroadcast(context,  0, intentWid, 0);
			pendingIntent.send();
		} catch (Exception e) {
			// nothing
		}
	}
}
