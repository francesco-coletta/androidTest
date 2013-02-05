package it.cf.android.smsListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

public class OutgoingSmsListener
        extends BroadcastReceiver
	{
		static private final Logger LOG = LoggerFactory.getLogger(OutgoingSmsListener.class);
		// private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
		// private static final String ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON";
		private static final String CHECK_OUTGOING_SMS = "it.cf.android.smsListener.CHECK_OUTGOING_SMS";

		public static final String APP_PROP_NAME_TIMESTAMP_LASTCHECK = "time_last_checked";

		@Override
		public void onReceive(final Context context, final Intent intent)
			{
				LOG.debug("Intent received: {}", intent.getAction());
				try
					{
						// onReceiveBootCompleted(context, intent);
						onReceiveCheckOutgoingSms(context, intent);
					}
				catch (final Exception e)
					{
						LOG.error(e.getMessage());
					}
			}

		// private void onReceiveBootCompleted(final Context context, final Intent intent) throws Exception
		// {
		// // verifico il tipo di intent, ossia azione
		// if (isBootCompleted(intent))
		// {
		// LOG.debug("Boot Completed");
		//
		// storeTimestampLastCheck(context);
		//
		// final PendingIntent outgoingSmsLogger = PendingIntent.getBroadcast(context, 0, new Intent(CHECK_OUTGOING_SMS), 0);
		// final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		// am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 120000L, 60000L, outgoingSmsLogger);
		// }
		// }

		private void onReceiveCheckOutgoingSms(final Context context, final Intent intent) throws Exception
			{
				// verifico il tipo di intent, ossia azione
				if (isCheckOutgoingSms(intent))
					{
						new OutgoingSmsLogger(context).execute();
					}
			}

		// private boolean isBootCompleted(final Intent intent)
		// {
		// return ((intent != null) && (intent.getAction() != null) && ((ACTION_BOOT_COMPLETED.compareToIgnoreCase(intent.getAction()) == 0) ||
		// (ACTION_QUICKBOOT_POWERON.compareToIgnoreCase(intent.getAction()) == 0)));
		// }

		private boolean isCheckOutgoingSms(final Intent intent)
			{
				return ((intent != null) && (intent.getAction() != null) && (CHECK_OUTGOING_SMS.compareToIgnoreCase(intent.getAction()) == 0));
			}

		// private void storeTimestampLastCheck(final Context context)
		// {
		// final long currentTime = System.currentTimeMillis();
		// final Editor editor = context.getSharedPreferences(APP_FILE_PREFERENCES, Context.MODE_PRIVATE).edit();
		// editor.putLong(APP_PROP_NAME_TIMESTAMP_LASTCHECK, currentTime);
		// editor.commit();
		//
		// LOG.debug("Update timestamp last check: {} = {}", currentTime, Utils.formatDatetime(currentTime));
		//
		// }

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
						this.prefs = context.getSharedPreferences(InitApp.APP_FILE_PREFERENCES, Context.MODE_PRIVATE);
						this.context = context;
					}

				@Override
				protected Void doInBackground(Void... params)
					{
						timeLastChecked = prefs.getLong(APP_PROP_NAME_TIMESTAMP_LASTCHECK, -1L);

						LOG.debug("SMS Message Sended.");
						List<Sms> messages;
						try
							{
								messages = getOutgoingSms();
								LOG.debug("Num SMS Message sended = {}", String.valueOf(messages.size()));

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
								LOG.error(e.getMessage());
							}

						try
							{
								Repository repoSms = new RepositoryFile(context);
								repoSms.writeSms(messages);
							}
						catch (Exception e)
							{
								LOG.error(e.getMessage());
							}
						return null;
					}

				private List<Sms> getOutgoingSms() throws Exception
					{
						List<Sms> outgoingSms = new ArrayList<Sms>();

						// get all sent SMS records from the date last checked, in descending order
						Cursor smsCursor;
						smsCursor = getSmsOutgoingCursor(context);
						LOG.debug("Num {} SMS have been sended after {}", smsCursor.getCount(), Utils.formatDatetime(timeLastChecked));

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
						return outgoingSms;
					}

				private Cursor getSmsOutgoingCursor(Context context) throws Exception
					{
						if (context == null)
							{
								LOG.error("Il context non deve essere null");
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
								LOG.error("Il nome della colonna non deve essere null o vuoto");
								throw new Exception("Il nome della colonna non deve essere null o vuoto");
							}
						if ((smsCursor == null) || (smsCursor.isClosed()))
							{
								LOG.error("Il cursore non deve essere null o  chiuso");
								throw new Exception("Il cursore non deve essere null o  chiuso");
							}

						String stringValue = "";
						try
							{
								int indexColumn = smsCursor.getColumnIndexOrThrow(columnName);
								LOG.debug("Nome colonna <{}> ha indice = {}", columnName, indexColumn);

								stringValue = smsCursor.getString(indexColumn);
							}
						catch (Exception e)
							{
								LOG.error(e.getMessage());
								stringValue = "";
							}
						return stringValue;
					}
			}//

	}
