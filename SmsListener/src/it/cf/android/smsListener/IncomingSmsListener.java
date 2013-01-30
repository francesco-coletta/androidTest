package it.cf.android.smsListener;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class IncomingSmsListener
        extends BroadcastReceiver
	{

		private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
		private static final String TAG = "IncomingSmsListener";

		private int numTabs = 0;

		@Override
		public void onReceive(Context context, Intent intent)
			{

				try
					{
						onReceiveSmsIncoming(context, intent);
					}
				catch (Exception e)
					{
						Log.e(TAG, e.getMessage());
					}
			}

		private void onReceiveSmsIncoming(Context context, Intent intent) throws Exception
			{
				numTabs++;

				Log.v(TAG, UtilsLog.getTab(numTabs) + "Activity State: onReceive()");
				Log.i(TAG, UtilsLog.getTab(numTabs) + "Intent received: " + intent.getAction());

				// verifico il tipo di intent, ossia azione
				if (isSmsReceived(intent))
					{
						Log.d(TAG, UtilsLog.getTab(numTabs) + "SMS Message Received.");
						List<Sms> messages = getIncomingSms(intent);
						Log.d(TAG, UtilsLog.getTab(numTabs) + "Num SMS Message Received = " + String.valueOf(messages.size()));

						// valorizzo il nome del contatto associato al numero di telefono da cui giunge l'SMS
						ContactManager contactManager = new ContactManager(context);
						for (Sms sms : messages)
							{
								sms.setNameContact(contactManager.getContactNameFromNumber(sms.getPhoneNumber()));
							}

						try
							{
								RepositorySms repoSms = new RepositorySmsFile(context);
								repoSms.writeSms(messages);
							}
						catch (Exception e)
							{
								Log.e(TAG, UtilsLog.getTab(numTabs) + e.getMessage());
							}
					}
				numTabs--;

			}

		private boolean isSmsReceived(Intent intent)
			{
				return (intent != null && intent.getAction() != null && ACTION_SMS_RECEIVED.compareToIgnoreCase(intent.getAction()) == 0);
			}

		private List<Sms> getIncomingSms(Intent intent)
			{
				numTabs++;

				List<Sms> incomingSms = new ArrayList<Sms>();
				if (intent != null)
					{
						Bundle bundle = intent.getExtras();
						if (bundle != null)
							{
								// ---retrieve the SMS message received---
								Sms sms;
								Object[] pduArray = (Object[]) bundle.get("pdus");
								for (int i = 0; i < pduArray.length; i++)
									{
										sms = SmsFactory.newIncomingSmsFromPdu((byte[]) pduArray[i]);
										incomingSms.add(sms);
									}
							}
					}
				numTabs--;
				return incomingSms;
			}

	}
