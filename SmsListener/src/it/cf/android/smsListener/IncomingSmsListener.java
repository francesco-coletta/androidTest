package it.cf.android.smsListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
import android.util.Log;

public class IncomingSmsListener
        extends BroadcastReceiver
	{

		private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
		private static final String TAG = "IncomingSmsListener";
		private static final String FILE_NAME = "SmsLog";
		private static final int MAX_SIZE_FILE_BYTE = 1 * 1024 * 1024; // 1MB
		// private static final int MAX_SIZE_FILE_BYTE = 1 * 1024; // x Test 1KB
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

				Log.v(TAG, getTab(numTabs) + "Activity State: onReceive()");
				Log.i(TAG, getTab(numTabs) + "Intent received: " + intent.getAction());

				// verifico il tipo di intent, ossia azione
				if (isSmsReceived(intent))
					{
						Log.d(TAG, getTab(numTabs) + "SMS Message Received.");
						SmsMessage[] messages = getIncomingSms(intent);
						Log.d(TAG, getTab(numTabs) + "Num SMS Message Received = " + String.valueOf(messages.length));

						try
							{
								FileOutputStream outputStream;
								outputStream = openOutputFile(context, FILE_NAME);
								for (SmsMessage smsMessage : messages)
									{
										String strSms = "";
										strSms += "IN SMS: ";
										strSms += getSmsTimestamp(smsMessage);
										strSms += ", FROM: ";
										strSms += getSmsFromNumber(smsMessage);
										strSms += " (";
										strSms += getContactNameFromNumber(context, getSmsFromNumber(smsMessage));
										strSms += "), TEXT: ";
										strSms += getSmsText(smsMessage);
										strSms += "\n";

										if (outputStream != null)
											{
												outputStream.write(strSms.getBytes());
											}
										Log.d(TAG, getTab(numTabs) + " " + strSms);
									}
								if (outputStream != null)
									{
										outputStream.close();
									}
							}
						catch (Exception e)
							{
								Log.e(TAG, getTab(numTabs) + e.getMessage());
							}

					}
				numTabs--;

			}

		private boolean isSmsReceived(Intent intent)
			{
				return (intent != null && intent.getAction() != null && ACTION_SMS_RECEIVED.compareToIgnoreCase(intent.getAction()) == 0);
			}

		private SmsMessage[] getIncomingSms(Intent intent)
			{
				numTabs++;
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
				numTabs--;
				return msgs;
			}

		private String getSmsFromNumber(SmsMessage sms)
			{
				String number = "";
				if (sms != null)
					{
						number = sms.getOriginatingAddress();
					}
				return number;
			}

		private String getSmsText(SmsMessage sms)
			{
				String text = "";
				if (sms != null)
					{
						text = sms.getMessageBody().toString();
					}
				return text;
			}

		private String getSmsTimestamp(SmsMessage sms)
			{
				String time = "";
				if (sms != null)
					{
						// time = DateFormat.getDateTimeInstance((DateFormat.LONG, DateFormat.LONG).format(newDate(sms.getTimestampMillis()));
						time = (String) DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date(sms.getTimestampMillis()));
					}
				return time;
			}

		private String getContactNameFromNumber(Context context, String phoneNumber) throws Exception
			{
				numTabs++;
				String contactName = "";
				// for read ALL (phone + sim) contact is necessary uses-permission="android.permission.READ_CONTACTS"
				// Cursor contactCursor = getContactsCursor(context);
				Cursor contactCursor = getCursor4ContactsWithPhoneNumber(context);
				Log.d(TAG, getTab(numTabs) + "Num conctact with phone number = " + contactCursor.getCount());
				while (contactCursor.moveToNext())
					{
						String contactId = getContactId(contactCursor);
						Log.d(TAG, getTab(numTabs) + "contactId = " + contactId);
						Log.d(TAG, getTab(numTabs) + "ContactName=" + getContactName(contactCursor));

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
				Log.d(TAG, getTab(numTabs) + "ContactName = " + contactName);
				numTabs--;
				return contactName;
			}

		private Cursor getCursor4ContactsWithPhoneNumber(Context context) throws Exception
			{
				if (context == null)
					{
						Log.e(TAG, getTab(numTabs) + "Il context non deve essere null");
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
						Log.e(TAG, getTab(numTabs) + "Il context non deve essere null");
						throw new Exception("Il context non deve essere null");
					}
				return context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
			}

		private boolean currentContactHaveAtLeastOnePhoneNumber(Cursor contactCursor) throws Exception
			{
				if ((contactCursor == null) || (contactCursor.isClosed()))
					{
						Log.e(TAG, getTab(numTabs) + "Il cursore non deve essere null o  chiuso");
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
						Log.e(TAG, getTab(numTabs) + "Il context non deve essere null");
						throw new Exception("Il context non deve essere null");
					}
				if ((contactId == null) || (contactId.length() == 0))
					{
						Log.e(TAG, getTab(numTabs) + "Il contactId non deve essere null");
						throw new Exception("Il context non deve essere null o vuoto");
					}

				numTabs++;
				List<String> phoneNumbers = new ArrayList<String>();

				Cursor phonesCursor = getCursor4PhoneNumberCursorForContactId(context, contactId);
				while (phonesCursor.moveToNext())
					{
						String phoneNumber = getPhoneNumber(phonesCursor);
						phoneNumbers.add(phoneNumber);

						Log.v(TAG, getTab(2) + "PhoneNumber=" + phoneNumber);
					}
				phonesCursor.close();

				numTabs--;
				return phoneNumbers;

			}

		private Cursor getCursor4PhoneNumberCursorForContactId(Context context, String contactId) throws Exception
			{
				if (context == null)
					{
						Log.e(TAG, getTab(numTabs) + "Il context non deve essere null");
						throw new Exception("Il context non deve essere null");
					}
				if ((contactId == null) || (contactId.length() == 0))
					{
						Log.e(TAG, getTab(numTabs) + "Il contactId non deve essere null");
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
						Log.e(TAG, getTab(numTabs) + "Il nome della colonna non deve essere null o vuoto");
						throw new Exception("Il nome della colonna non deve essere null o vuoto");
					}
				if ((contactCursor == null) || (contactCursor.isClosed()))
					{
						Log.e(TAG, getTab(numTabs) + "Il cursore non deve essere null o  chiuso");
						throw new Exception("Il cursore non deve essere null o  chiuso");
					}

				numTabs++;
				String stringValue = "";
				try
					{
						int indexColumn = contactCursor.getColumnIndexOrThrow(columnName);
						Log.d(TAG, getTab(numTabs) + "Nome colonna <" + columnName + "> ha indice = " + String.valueOf(indexColumn));

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

		private FileOutputStream openOutputFile(Context context, String filename) throws Exception
			{
				if (context == null)
					{
						Log.e(TAG, getTab(numTabs) + "Il context non deve essere null");
						throw new Exception("Il context non deve essere null");
					}
				if ((filename == null) || (filename.length() == 0))
					{
						Log.e(TAG, getTab(numTabs) + "Il filename non deve essere null");
						throw new Exception("Il filename non deve essere null o vuoto");
					}

				numTabs++;
				if (fileSizeIsLowerThanMaxValue(context, filename))
					{
						Log.d(TAG, getTab(numTabs) + "Le dimensioni del file <" + filename + "> sono < al max (" + MAX_SIZE_FILE_BYTE + ")");
					}
				else
					{
						Log.d(TAG, getTab(numTabs) + "Le dimensioni del file <" + filename + "> sono > al max (" + MAX_SIZE_FILE_BYTE + ")");

						String fileNameWithPath = context.getFilesDir().getCanonicalPath() + File.separator + filename;
						String newFileNameWithPath = context.getFilesDir().getCanonicalPath() + File.separator + filename + "_" + (String) DateFormat.format("yyyyMMddhhmm", new Date(System.currentTimeMillis()));

						Log.e(TAG, getTab(numTabs) + "Copy file from <" + fileNameWithPath + "> to <" + newFileNameWithPath + ">");
						copy(fileNameWithPath, newFileNameWithPath);

						// resetto il file
						RandomAccessFile raf = new RandomAccessFile(fileNameWithPath, "rw");
						raf.setLength(0);
						raf.close();
					}

				FileOutputStream outputStream = null;
				try
					{
						outputStream = context.openFileOutput(filename, Context.MODE_APPEND);
					}
				catch (Exception e)
					{
						e.printStackTrace();
					}
				numTabs--;
				return outputStream;
			}

		private boolean fileSizeIsLowerThanMaxValue(Context context, String filename)
			{
				numTabs++;
				File file = new File(context.getFilesDir(), filename);
				Log.d(TAG, getTab(numTabs) + "Dimensioni file <" + filename + "> = " + file.length());
				numTabs--;
				return (!file.exists()) || (file.length() < MAX_SIZE_FILE_BYTE);
			}

		public void copy(String filenameSrc, String filenameDest) throws IOException
			{
				InputStream in = new FileInputStream(filenameSrc);
				OutputStream out = new FileOutputStream(filenameDest);

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0)
					{
						out.write(buf, 0, len);
					}
				in.close();
				out.close();
			}

		private String getTab(final int numTabs)
			{
				int nt = numTabs;
				if (nt < 0)
					{
						nt = 0;
					}
				if (nt > 9)
					{
						nt = 9;
					}

				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < nt; i++)
					{
						sb.append("   ");
					}
				return sb.toString();
			}
	}
