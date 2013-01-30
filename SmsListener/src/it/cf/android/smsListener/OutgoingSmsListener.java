package it.cf.android.smsListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;

public class OutgoingSmsListener
        extends BroadcastReceiver
	{

		private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
		private static final String ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON";
		private static final String CHECK_OUTGOING_SMS = "it.cf.android.smsListener.CHECK_OUTGOING_SMS";

		private static final String APP_FILE_PREFERENCES = "smsListener";
		private static final String APP_PROP_NAME_TIMESTAMP_LASTCHECK = "time_last_checked";
		private static final String TAG = "OutgoingSmsListener";
		private static int numTabs = 0;

		@Override
		public void onReceive(final Context context, final Intent intent)
			{

				try
					{
						Log.v(TAG, getTab(numTabs) + "Activity State: onReceive()");

						onReceiveBootCompleted(context, intent);
						onReceiveCheckOutgoingSms(context, intent);
					}
				catch (final Exception e)
					{
						Log.e(TAG, e.getMessage());
					}
			}

		private void onReceiveBootCompleted(final Context context, final Intent intent) throws Exception
			{
				numTabs++;

				// verifico il tipo di intent, ossia azione
				if (isBootCompleted(intent))
					{
						Log.v(TAG, getTab(numTabs) + "onReceiveBootCompleted()");
						Log.d(TAG, getTab(numTabs) + "Intent received: " + intent.getAction());

						Log.d(TAG, getTab(numTabs) + "Boot Completed");

						storeTimestampLastCheck(context);

						final PendingIntent outgoingSmsLogger = PendingIntent.getBroadcast(context, 0, new Intent(CHECK_OUTGOING_SMS), 0);
						final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
						am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 120000L, 120000L, outgoingSmsLogger);
					}
				numTabs--;
			}

		private void onReceiveCheckOutgoingSms(final Context context, final Intent intent) throws Exception
			{
				numTabs++;

				// verifico il tipo di intent, ossia azione
				if (isCheckOutgoingSms(intent))
					{
						Log.v(TAG, getTab(numTabs) + "onReceiveCheckOutgoingSms()");
						Log.i(TAG, getTab(numTabs) + "Intent received: " + intent.getAction());
						new OutgoingSmsLogger(context).execute();

					}
				numTabs--;
			}

		private boolean isBootCompleted(final Intent intent)
			{
				return ((intent != null) && (intent.getAction() != null) && ((ACTION_BOOT_COMPLETED.compareToIgnoreCase(intent.getAction()) == 0) || (ACTION_QUICKBOOT_POWERON.compareToIgnoreCase(intent.getAction()) == 0)));
			}

		private boolean isCheckOutgoingSms(final Intent intent)
			{
				return ((intent != null) && (intent.getAction() != null) && (CHECK_OUTGOING_SMS.compareToIgnoreCase(intent.getAction()) == 0));
			}

		private void storeTimestampLastCheck(final Context context)
			{
				final long currentTime = System.currentTimeMillis();
				final Editor editor = context.getSharedPreferences(APP_FILE_PREFERENCES, Context.MODE_PRIVATE).edit();
				editor.putLong(APP_PROP_NAME_TIMESTAMP_LASTCHECK, currentTime);
				editor.commit();

				Log.d(TAG, getTab(numTabs) + "Update timestamp last check: " + String.valueOf(currentTime) + " = " + getTimestamp(currentTime));

			}

		private String getTimestamp(final long millisec)
			{
				String time = "";
				if (millisec > 0)
					{
						time = (String) DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date(millisec));
					}
				return time;
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

				final StringBuilder sb = new StringBuilder();
				for (int i = 0; i < nt; i++)
					{
						sb.append("   ");
					}
				return sb.toString();
			}

		private class OutgoingSmsLogger
		        extends AsyncTask<Void, Void, Void>
			{
				private final SharedPreferences prefs;
				private final Context context;
				private long timeLastChecked;
				private final String smsColumnName4Date = "date";
				private final String smsColumnName4Address = "address";
				private final String smsColumnName4Type = "type";
				private final String smsColumnName4Body = "body";

				public OutgoingSmsLogger(Context context)
					{
						this.prefs = context.getSharedPreferences(APP_FILE_PREFERENCES, Context.MODE_PRIVATE);
						this.context = context;
					}

				@Override
				protected Void doInBackground(Void... params)
					{
						numTabs++;

						timeLastChecked = prefs.getLong(APP_PROP_NAME_TIMESTAMP_LASTCHECK, -1L);

						Log.d(TAG, UtilsLog.getTab(numTabs) + "SMS Message Received.");
						List<Sms> messages;
						try
							{
								messages = getOutgoingSms();
								Log.d(TAG, UtilsLog.getTab(numTabs) + "Num SMS Message Received = " + String.valueOf(messages.size()));

								// valorizzo il nome del contatto associato al numero di telefono da cui giunge l'SMS
								ContactManager contactManager = new ContactManager(context);
								for (Sms sms : messages)
									{
										sms.setNameContact(contactManager.getContactNameFromNumber(sms.getPhoneNumber()));
									}
							}
						catch (Exception e)
							{
								messages = new ArrayList<Sms>();
								Log.e(TAG, UtilsLog.getTab(numTabs) + e.getMessage());
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
						numTabs--;
						return null;
					}

				private List<Sms> getOutgoingSms() throws Exception
					{
						numTabs++;

						List<Sms> outgoingSms = new ArrayList<Sms>();

						// get all sent SMS records from the date last checked, in descending order
						Cursor smsCursor;
						smsCursor = getSmsOutgoingCursor(context);
						Log.d(TAG, getTab(numTabs) + String.valueOf(smsCursor.getCount()) + " SMS sended after " + (String) DateFormat.format("yyyy-MM-dd hh:mm:ss", timeLastChecked));

						// if there are any new sent messages after the last time we checked
						if (smsCursor.moveToNext())
							{
								Set<String> sentSms = new HashSet<String>();
								timeLastChecked = getTimestampLikeLong(smsCursor);
								do
									{
										long timestamp = getTimestampLikeLong(smsCursor);
										String address = getPhoneNumber(smsCursor);
										String text = getText(smsCursor);
										Sms sms = SmsFactory.newOutgoingSms(address, timestamp, text);

										if (sentSms.contains(sms.toString()))
											{
												continue; // skip that thing
											}
										// else, add it to the set
										sentSms.add(sms.toString());
										outgoingSms.add(sms);
									}
								while (smsCursor.moveToNext());
								Editor editor = prefs.edit();
								editor.putLong(APP_PROP_NAME_TIMESTAMP_LASTCHECK, timeLastChecked);
								editor.commit();
							}
						smsCursor.close();

						numTabs--;
						return outgoingSms;
					}

				private Cursor getSmsOutgoingCursor(Context context) throws Exception
					{
						if (context == null)
							{
								Log.e(TAG, getTab(numTabs) + "Il context non deve essere null");
								throw new Exception("Il context non deve essere null");
							}
						// get all sent SMS records from the date last checked, in descending order
						Uri uri = Uri.parse("content://sms");
						String[] projection = new String[] { smsColumnName4Date, smsColumnName4Address, smsColumnName4Body, smsColumnName4Type };
						String selection = smsColumnName4Type + " = ? AND " + smsColumnName4Date + " > ?";
						String[] selectionArgs = new String[] { "2", String.valueOf(timeLastChecked) };
						String sortOrder = smsColumnName4Date + " DESC";

						return context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
					}

				private String getPhoneNumber(Cursor smsCursor) throws Exception
					{
						return getStringValueFromColumn(smsColumnName4Address, smsCursor);
					}

				private String getTimestamp(Cursor smsCursor) throws Exception
					{
						return getStringValueFromColumn(smsColumnName4Date, smsCursor);
					}

				private long getTimestampLikeLong(Cursor smsCursor) throws Exception
					{
						return Long.parseLong(getTimestamp(smsCursor));
					}

				private String getText(Cursor smsCursor) throws Exception
					{
						return getStringValueFromColumn(smsColumnName4Body, smsCursor);
					}

				private String getStringValueFromColumn(String columnName, Cursor smsCursor) throws Exception
					{
						if ((columnName == null) || (columnName.length() == 0))
							{
								Log.e(TAG, getTab(numTabs) + "Il nome della colonna non deve essere null o vuoto");
								throw new Exception("Il nome della colonna non deve essere null o vuoto");
							}
						if ((smsCursor == null) || (smsCursor.isClosed()))
							{
								Log.e(TAG, getTab(numTabs) + "Il cursore non deve essere null o  chiuso");
								throw new Exception("Il cursore non deve essere null o  chiuso");
							}

						numTabs++;
						String stringValue = "";
						try
							{
								int indexColumn = smsCursor.getColumnIndexOrThrow(columnName);
								Log.d(TAG, getTab(numTabs) + "Nome colonna <" + columnName + "> ha indice = " + String.valueOf(indexColumn));

								stringValue = smsCursor.getString(indexColumn);
							}
						catch (Exception e)
							{
								Log.e(TAG, e.getMessage());
								stringValue = "";
							}
						numTabs--;
						return stringValue;
					}
			}//

	}
