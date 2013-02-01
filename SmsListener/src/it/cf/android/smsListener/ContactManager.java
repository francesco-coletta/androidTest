package it.cf.android.smsListener;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ContactManager
	{
		static private final Logger LOG = LoggerFactory.getLogger(ContactManager.class);

		private final Context context;

		public ContactManager(final Context context)
		        throws Exception
			{
				if (context == null)
					{
						LOG.error("context == null");
						throw new Exception("context == null");
					}
				this.context = context;
			}

		public String getContactNameFromNumber(String phoneNumber) throws Exception
			{
				String contactName = "";
				// for read ALL (phone + sim) contact is necessary uses-permission="android.permission.READ_CONTACTS"
				// Cursor contactCursor = getContactsCursor(context);
				Cursor contactCursor = getCursor4ContactsWithPhoneNumber(context);
				LOG.trace("Num conctact with phone number = {}", contactCursor.getCount());
				while (contactCursor.moveToNext())
					{
						String contactId = getContactId(contactCursor);
						LOG.trace("contactId = {}, ContactName = {}", contactId, getContactName(contactCursor));
						List<String> contactPhoneNumbers = getPhoneNumbersByContactId(context, contactId);
						if (contactPhoneNumbers.contains(phoneNumber))
							{
								contactName = getContactName(contactCursor);
							}
					}
				contactCursor.close();
				if (contactName.length() == 0)
					{
						contactName = "UNKNOW";
					}
				LOG.trace("ContactName = {}", contactName);
				return contactName;
			}

		private Cursor getCursor4ContactsWithPhoneNumber(Context context) throws Exception
			{
				if (context == null)
					{
						LOG.error("Il context non deve essere null");
						throw new Exception("Il context non deve essere null");
					}

				Uri uri = ContactsContract.Contacts.CONTENT_URI;
				String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER };
				// String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
				String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + "= ?";
				// String[] selectionArgs = null;

				String[] selectionArgs = new String[] { "1" };
				String sortOrder = null; // ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

				Cursor cursor;
				cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

				/*
				 * Valid from Android 3.0.x (HONEYCOMB) Api Level = 11
				 * CursorLoader cl = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
				 * cursor=cl.loadInBackground();
				 */
				return cursor;
			}

		private Cursor getContactsCursor(Context context) throws Exception
			{
				if (context == null)
					{
						LOG.error("Il context non deve essere null");
						throw new Exception("Il context non deve essere null");
					}
				return context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
			}

		private boolean currentContactHaveAtLeastOnePhoneNumber(Cursor contactCursor) throws Exception
			{
				if ((contactCursor == null) || (contactCursor.isClosed()))
					{
						LOG.error("Il cursore non deve essere null o  chiuso");
						throw new Exception("Il cursore non deve essere null o  chiuso");
					}

				boolean phoneNumberExists = false;
				try
					{
						String hasPhone = getContactHasPhoneNumber(contactCursor);
						phoneNumberExists = Boolean.parseBoolean(hasPhone);
					}
				catch (Exception e)
					{
						LOG.error(e.getMessage());
						phoneNumberExists = false;
					}
				return phoneNumberExists;
			}

		private List<String> getPhoneNumbersByContactId(Context context, String contactId) throws Exception
			{
				if (context == null)
					{
						LOG.error("Il context non deve essere null");
						throw new Exception("Il context non deve essere null");
					}
				if ((contactId == null) || (contactId.length() == 0))
					{
						LOG.error("Il contactId non deve essere null");
						throw new Exception("Il context non deve essere null o vuoto");
					}

				List<String> phoneNumbers = new ArrayList<String>();

				Cursor phonesCursor = getCursor4PhoneNumberCursorForContactId(context, contactId);
				while (phonesCursor.moveToNext())
					{
						String phoneNumber = getPhoneNumber(phonesCursor);
						phoneNumbers.add(phoneNumber);

						LOG.trace("PhoneNumber = {}", phoneNumber);
					}
				phonesCursor.close();
				return phoneNumbers;

			}

		private Cursor getCursor4PhoneNumberCursorForContactId(Context context, String contactId) throws Exception
			{
				if (context == null)
					{
						LOG.error("Il context non deve essere null");
						throw new Exception("Il context non deve essere null");
					}
				if ((contactId == null) || (contactId.length() == 0))
					{
						LOG.error("Il contactId non deve essere null");
						throw new Exception("Il context non deve essere null o vuoto");
					}

				Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
				String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER };
				// String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId;
				String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "= ?";
				// String[] selectionArgs = null;
				String[] selectionArgs = new String[] { contactId };
				String sortOrder = null;

				Cursor cursor;
				cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

				/*
				 * Valid from Android 3.0.x (HONEYCOMB) Api Level = 11
				 * CursorLoader cl = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
				 * cursor=cl.loadInBackground();
				 */
				return cursor;
			}

		private String getContactId(Cursor contactCursor) throws Exception
			{
				return getStringValueFromColumn(ContactsContract.Contacts._ID, contactCursor);
			}

		private String getContactName(Cursor contactCursor) throws Exception
			{
				// return getStringValueFromColumn(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, contactCursor);
				return getStringValueFromColumn(ContactsContract.Contacts.DISPLAY_NAME, contactCursor);
			}

		private String getContactHasPhoneNumber(Cursor contactCursor) throws Exception
			{
				return getStringValueFromColumn(ContactsContract.Contacts.HAS_PHONE_NUMBER, contactCursor);
			}

		private String getPhoneNumber(Cursor contactCursor) throws Exception
			{
				return getStringValueFromColumn(ContactsContract.CommonDataKinds.Phone.NUMBER, contactCursor);
			}

		private String getStringValueFromColumn(String columnName, Cursor contactCursor) throws Exception
			{
				if ((columnName == null) || (columnName.length() == 0))
					{
						LOG.error("Il nome della colonna non deve essere null o vuoto");
						throw new Exception("Il nome della colonna non deve essere null o vuoto");
					}
				if ((contactCursor == null) || (contactCursor.isClosed()))
					{
						LOG.error("Il cursore non deve essere null o  chiuso");
						throw new Exception("Il cursore non deve essere null o  chiuso");
					}

				String stringValue = "";
				try
					{
						int indexColumn = contactCursor.getColumnIndexOrThrow(columnName);
						LOG.trace("Nome colonna <{}> ha indice {} ", columnName, String.valueOf(indexColumn));

						stringValue = contactCursor.getString(indexColumn);
					}
				catch (Exception e)
					{
						LOG.error(e.getMessage());
						stringValue = "";
					}
				return stringValue;
			}

	}
