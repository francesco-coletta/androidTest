package it.cf.android.smsListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Sms
	{

		private final String phoneNumber;
		private String nameContact = "UNKNOWN";
		private final long timestamp;
		private final String text;
		private final SmsDirection direction;

		public enum SmsDirection
			{
				Incoming, Outgoing
			}

		public Sms(SmsDirection directionType, String phoneNumber,
		           long timestamp, String text)
			{
				super();
				this.phoneNumber = StringUtils.trimToEmpty(phoneNumber);
				this.timestamp = timestamp;
				this.text = StringUtils.trimToEmpty(text);
				this.direction = directionType;
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

		public SmsDirection getDirection()
			{
				return direction;
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
				toString.append("Direction", direction.name());
				toString.append(" Time", Utils.formatDatetime(timestamp));
				toString.append(" Phone", phoneNumber);
				toString.append(" Contact", nameContact);
				toString.append(" Text", text);
				return toString.build().concat("\n");
			}

	}
