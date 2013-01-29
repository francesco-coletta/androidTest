package it.cf.android.smsListener;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class IncomingSmsListener
        extends BroadcastReceiver
	{

		private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
		private static final String TAG = "IncomingSmsListener";

		@Override
		public void onReceive(Context context, Intent intent)
			{
				Log.i(TAG, "Intent received: " + intent.getAction());

				// verifico il tipo di intent, ossia azione
				if (isSmsReceived(intent))
					{
						Log.d(TAG, "SMS Message Received.");
						SmsMessage[] messages = getIncomingSms(intent);
						Log.d(TAG, "Num SMS Message Received = " + String.valueOf(messages.length));

						String strAllSms = "";
						for (SmsMessage smsMessage : messages)
							{
								String strSms = "";
								strSms += "SMS ";
								strSms += getTimestampFromSmsMessage(smsMessage);
								strSms += ", from ";
								strSms += getSenderFromSmsMessage(smsMessage);
								strSms += ": ";
								strSms += getTextFromSmsMessage(smsMessage);
								strSms += "\n";
								strAllSms += strSms;
								Log.d(TAG, strSms);
							}
						Toast.makeText(context, strAllSms, Toast.LENGTH_SHORT).show();
					}
			}

		private boolean isSmsReceived(Intent intent)
			{
				return (intent != null && intent.getAction() != null && ACTION_SMS_RECEIVED.compareToIgnoreCase(intent.getAction()) == 0);
			}

		private SmsMessage[] getIncomingSms(Intent intent)
			{
				SmsMessage[] msgs = new SmsMessage[0];
				if (intent != null)
					{
						Bundle bundle = intent.getExtras();
						if (bundle != null)
							{
								// ---retrieve the SMS message received---
								Object[] pduArray = (Object[]) bundle.get("pdus");
								msgs = new SmsMessage[pduArray.length];
								for (int i = 0; i < msgs.length; i++)
									{
										msgs[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
									}
							}
					}
				return msgs;
			}

		private String getSenderFromSmsMessage(SmsMessage sms)
			{
				String sender = "";
				if (sms != null)
					{
						sender = sms.getOriginatingAddress();
					}
				return sender;
			}

		private String getTextFromSmsMessage(SmsMessage sms)
			{
				String text = "";
				if (sms != null)
					{
						text = sms.getMessageBody().toString();
					}
				return text;
			}

		private String getTimestampFromSmsMessage(SmsMessage sms)
			{
				String time = "";
				if (sms != null)
					{
						// time = DateFormat.getDateTimeInstance((DateFormat.LONG, DateFormat.LONG).format(newDate(sms.getTimestampMillis()));
						time = (String) DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date(sms.getTimestampMillis()));
					}
				return time;
			}

	}
