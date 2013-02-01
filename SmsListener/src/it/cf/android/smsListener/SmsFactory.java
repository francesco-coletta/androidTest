package it.cf.android.smsListener;

import it.cf.android.smsListener.Sms.SmsDirection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.telephony.SmsMessage;

public class SmsFactory
	{
		static private final Logger LOG = LoggerFactory.getLogger(SmsFactory.class);

		static public Sms newIncomingSmsFromSmsMessage(SmsMessage smsMessage)
			{
				Sms sms;
				if (smsMessage == null)
					{
						LOG.error("smsMessage == null");
						sms = new Sms(SmsDirection.Incoming, "", 0, "");
					}
				else
					{
						sms = new Sms(SmsDirection.Incoming, smsMessage.getOriginatingAddress(), smsMessage.getTimestampMillis(), smsMessage.getMessageBody());
					}
				LOG.debug("Sms created: " + sms.toString());

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
				return new Sms(SmsDirection.Outgoing, fromPhoneNumber, timestamp, text);
			}

	}
