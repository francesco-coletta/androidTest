package it.cf.android.smsListener;

import it.cf.android.smsListener.Sms.SmsType;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsFactory
	{
		private static final String TAG = "SmsFactory";

		static public Sms newIncomingSmsFromSmsMessage(SmsMessage smsMessage)
			{
				Sms sms;
				if (smsMessage == null)
					{
						Log.e(TAG, "smsMessage == null");
						sms = new Sms(SmsType.Incoming, "", 0, "");
					}
				else
					{
						sms = new Sms(SmsType.Incoming, smsMessage.getOriginatingAddress(), smsMessage.getTimestampMillis(), smsMessage.getMessageBody());
					}
				Log.e(TAG, "Sms created: " + sms.toString());

				return sms;
			}

		public static Sms newIncomingSmsFromPdu(byte[] pdu)
			{
				Sms sms;
				if (pdu.length > 0)
					{
						sms = newIncomingSmsFromSmsMessage(SmsMessage.createFromPdu(pdu));
					}
				else
					{
						sms = newIncomingSmsFromSmsMessage(null);
					}
				return sms;
			}

		public static Sms newOutgoingSms(String fromPhoneNumber, long timestamp, String text)
			{
				return new Sms(SmsType.Outgoing, fromPhoneNumber, timestamp, text);
			}

	}
