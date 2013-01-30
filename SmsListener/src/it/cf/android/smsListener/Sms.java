package it.cf.android.smsListener;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import android.text.format.DateFormat;

public class Sms
	{

		private final String phoneNumber;
		private String nameContact = "UNKNOWN";
		private final long timestamp;
		private final String text;
		private final SmsType directionType;

		public enum SmsType
			{
				Incoming, Outgoing
			}

		public Sms(SmsType directionType, String phoneNumber,
		           long timestamp, String text)
			{
				super();
				this.phoneNumber = StringUtils.trimToEmpty(phoneNumber);
				this.timestamp = timestamp;
				this.text = StringUtils.trimToEmpty(text);
				this.directionType = directionType;
			}

		public String getPhoneNumber()
			{
				return phoneNumber;
			}

		public long getTimestamp()
			{
				return timestamp;
			}

		public String getText()
			{
				return text;
			}

		public SmsType getDirectionType()
			{
				return directionType;
			}

		public String getNameContact()
			{
				return nameContact;
			}

		public void setNameContact(String nameContact)
			{
				if (StringUtils.isBlank(nameContact) == false)
					{
						this.nameContact = nameContact;
					}
			}

		@Override
		public String toString()
			{
				ToStringBuilder toString = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
				toString.append("Direction", directionType.name());
				toString.append(" Time", DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date(timestamp)));
				toString.append(" Phone", phoneNumber);
				toString.append(" Contact", nameContact);
				toString.append(" Text", text);
				return toString.build().concat("\n");
			}

	}
