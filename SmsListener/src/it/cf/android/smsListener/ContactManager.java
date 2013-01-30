package it.cf.android.smsListener;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

public class ContactManager
	{
		private static final String TAG = "ContactManager";
		private int numTabs = 0;

		private final Context context;

		public ContactManager(final Context context)
		        throws Exception
			{
				if (context == null)
					{
						Log.e(TAG, "context == null");
						throw new Exception("context == null");
					}
				this.context = context;
			}

		public String getContactNameFromNumber(String phoneNumber) throws Exception
			{
				numTabs++;
				String contactName = "";
				// for read ALL (phone + sim) contact is necessary uses-permission="android.permission.READ_CONTACTS"
				// Cursor contactCursor = getContactsCursor(context);
				Cursor contactCursor = getCursor4ContactsWithPhoneNumber(context);
				Log.d(TAG, UtilsLog.getTab(numTabs) + "Num conctact with phone number = " + contactCursor.getCount());
				while (contactCursor.moveToNext())
					{
						String contactId = getContactId(contactCursor);
						Log.d(TAG, UtilsLog.getTab(numTabs) + "contactId = " + contactId);
						Log.d(TAG, UtilsLog.getTab(numTabs) + "ContactName=" + getContactName(contactCursor));

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
				Log.d(TAG, UtilsLog.getTab(numTabs) + "ContactName = " + contactName);
				numTabs--;
				return contactName;
			}

		private Cursor getCursor4ContactsWithPhoneNumber(Context context) throws Exception
			{
				if (context == null)
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + "Il context non deve essere null");
						throw new Exception("Il context non deve essere null");
					}
				numTabs++;

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

				numTabs--;
				return cursor;
			}

		private Cursor getContactsCursor(Context context) throws Exception
			{
				if (context == null)
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + "Il context non deve essere null");
						throw new Exception("Il context non deve essere null");
					}
				return context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
			}

		private boolean currentContactHaveAtLeastOnePhoneNumber(Cursor contactCursor) throws Exception
			{
				if ((contactCursor == null) || (contactCursor.isClosed()))
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + "Il cursore non deve essere null o  chiuso");
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
						Log.e(TAG, e.getMessage());
						phoneNumberExists = false;
					}
				return phoneNumberExists;
			}

		private List<String> getPhoneNumbersByContactId(Context context, String contactId) throws Exception
			{
				if (context == null)
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + "Il context non deve essere null");
						throw new Exception("Il context non deve essere null");
					}
				if ((contactId == null) || (contactId.length() == 0))
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + "Il contactId non deve essere null");
						throw new Exception("Il context non deve essere null o vuoto");
					}

				numTabs++;
				List<String> phoneNumbers = new ArrayList<String>();

				Cursor phonesCursor = getCursor4PhoneNumberCursorForContactId(context, contactId);
				while (phonesCursor.moveToNext())
					{
						String phoneNumber = getPhoneNumber(phonesCursor);
						phoneNumbers.add(phoneNumber);

						Log.v(TAG, UtilsLog.getTab(2) + "PhoneNumber=" + phoneNumber);
					}
				phonesCursor.close();

				numTabs--;
				return phoneNumbers;

			}

		private Cursor getCursor4PhoneNumberCursorForContactId(Context context, String contactId) throws Exception
			{
				if (context == null)
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + "Il context non deve essere null");
						throw new Exception("Il context non deve essere null");
					}
				if ((contactId == null) || (contactId.length() == 0))
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + "Il contactId non deve essere null");
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
						Log.e(TAG, UtilsLog.getTab(numTabs) + "Il nome della colonna non deve essere null o vuoto");
						throw new Exception("Il nome della colonna non deve essere null o vuoto");
					}
				if ((contactCursor == null) || (contactCursor.isClosed()))
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + "Il cursore non deve essere null o  chiuso");
						throw new Exception("Il cursore non deve essere null o  chiuso");
					}

				numTabs++;
				String stringValue = "";
				try
					{
						int indexColumn = contactCursor.getColumnIndexOrThrow(columnName);
						Log.d(TAG, UtilsLog.getTab(numTabs) + "Nome colonna <" + columnName + "> ha indice = " + String.valueOf(indexColumn));

						stringValue = contactCursor.getString(indexColumn);
					}
				catch (Exception e)
					{
						Log.e(TAG, e.getMessage());
						stringValue = "";
					}
				numTabs--;
				return stringValue;
			}

	}
