package it.cf.android.smsListener;

import it.cf.android.smsListener.activity.AccessCallSmsListenerActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class InitApp
        extends BroadcastReceiver
	{
		static private final Logger LOG = LoggerFactory.getLogger(InitApp.class);

		public static final String APP_FILE_PREFERENCES = "smsListener";

		private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
		private static final String ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON";

		// costante che indica l'azione alarm che verra notificata ai receiver per gestire il check degli outgoing sms
		private static final String CHECK_OUTGOING_SMS = "it.cf.android.smsListener.CHECK_OUTGOING_SMS";

		private static final String TAG = "InitApp";

		//@formatter:off
		//file di configurazione di Logback
	    static final String LOGBACK_XML =
	    	    "<configuration>" +
	    	    "	<appender name='LOGCAT' class='ch.qos.logback.classic.android.LogcatAppender'>" +
	    	    "		<tagEncoder>" +
	    	    "			<pattern>%logger{0}</pattern>" +
	    	    "		</tagEncoder>" +
	    	    "		<encoder>" +
	    	    "			<pattern>%method \\(%line\\): %msg</pattern>" +
	    	    "		</encoder>" +
	    	    "	</appender>" +
	    	    " " +
	    	    "	<appender name='FILE' class='ch.qos.logback.core.rolling.RollingFileAppender'>" +
	    	    "		<file>${log_path:-}CallSmsListener.log</file>" +
	    	    "		<rollingPolicy class='ch.qos.logback.core.rolling.TimeBasedRollingPolicy'>" +
	    	    "			<!-- rollover monthly -->" +
	    	    "			<fileNamePattern>${log_path:-}CallSmsListener-%d{yyyy-MM}.%i.log</fileNamePattern>" +
	    	    "			<!-- or whenever the file size reaches 1MB -->" +
	    	    "			<timeBasedFileNamingAndTriggeringPolicy class='ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP'>" +
	    	    "				<maxFileSize>1MB</maxFileSize>" +
	    	    "			</timeBasedFileNamingAndTriggeringPolicy>" +
	    	    "			<!-- keep 12 months' worth of history -->" +
	    	    "			<maxHistory>12</maxHistory>" +
	    	    "		</rollingPolicy>" +
	    	    "		<encoder>" +
	    	    "			<pattern>%d{dd-MM-yyyy HH:mm:ss.SSS} [%-30.-30file] %-5level %-20.-20logger{0}.%-25.-25method \\(%4line\\): %msg%n</pattern>" +
	    	    "		</encoder>" +
	    	    "	</appender>" +	    	    
	    	    " " +
	    	    "	<root level='TRACE'>" +
	    	    "		<appender-ref ref='LOGCAT' />" +
	    	    "		<appender-ref ref='FILE' />" +
	    	    "	</root>" +
	    	    "</configuration>"
	    	    ;		
	    //@formatter:on

		@Override
		public void onReceive(final Context context, final Intent intent)
			{
				// Log.i(TAG, "Intent received: " + intent.getAction());
				LOG.info("Intent received: {}", intent.getAction());

				try
					{
						// verifico il tipo di intent, ossia azione
						if (isBootCompleted(intent))
							{
								Log.i(TAG, "Boot Completed");
								LOG.info("Boot Completed");

								configureDefaultPassword(context, intent);
								LOG.info("Default password configuration Completed");
								Log.i(TAG, "Default password configuration Completed");

								// configureLogging(context, intent);
								// Log.i(TAG, "Logback config Completed");
								configureAlarmManagerForCheckOutgoingSms(context, intent);
								// Log.i(TAG, "AlarmManager For Check Outgoing Sms config Completed");
								LOG.info("AlarmManager For Check Outgoing Sms config Completed");
								Log.i(TAG, "AlarmManager For Check Outgoing Sms config Completed");
							}
					}
				catch (final Exception e)
					{
						// Log.e(TAG, e.getMessage());
						LOG.error(e.getMessage());
						Log.e(TAG, e.getMessage());
					}
			}

		private void configureDefaultPassword(Context context, Intent intent)
			{

				SharedPreferences pref = context.getSharedPreferences(InitApp.APP_FILE_PREFERENCES, Context.MODE_PRIVATE);
				if (pref.contains(AccessCallSmsListenerActivity.APP_PROP_NAME_PASSWORD))
					{
						LOG.debug("La password esiste già nelle preferenze");
						Log.d(TAG, "La password esiste già nelle preferenze");
					}
				else
					{
						LOG.debug("La password non esiste nelle preferenze. Creo la password di default");
						Log.d(TAG, "La password non esiste nelle preferenze. Creo la password di default");
						final Editor editor = pref.edit();
						editor.putString(AccessCallSmsListenerActivity.APP_PROP_NAME_PASSWORD, "0123456789");
						editor.commit();
					}
			}

		private boolean isBootCompleted(final Intent intent)
			{
				return ((intent != null) && (intent.getAction() != null) && ((ACTION_BOOT_COMPLETED.compareToIgnoreCase(intent.getAction()) == 0) || (ACTION_QUICKBOOT_POWERON.compareToIgnoreCase(intent.getAction()) == 0)));
			}

		private void configureAlarmManagerForCheckOutgoingSms(final Context context, final Intent intent) throws Exception
			{
				if (context == null || intent == null)
					{
						throw new Exception("(context == null || intent == null)");
					}

				storeTimestampLastCheck(context);

				final PendingIntent outgoingSmsLogger = PendingIntent.getBroadcast(context, 0, new Intent(CHECK_OUTGOING_SMS), 0);
				final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 120000L, 60000L, outgoingSmsLogger);
			}

		private void storeTimestampLastCheck(final Context context)
			{
				final long currentTime = System.currentTimeMillis();
				final Editor editor = context.getSharedPreferences(InitApp.APP_FILE_PREFERENCES, Context.MODE_PRIVATE).edit();
				editor.putLong(OutgoingSmsListener.APP_PROP_NAME_TIMESTAMP_LASTCHECK, currentTime);
				editor.commit();

				// Log.d(TAG, "Update timestamp last check: " + currentTime + " = " + Utils.formatDatetime(currentTime));
				LOG.debug("Update timestamp last check: {}  = {}", currentTime, Utils.formatDatetime(currentTime));
			}

		// private void configureLogging(final Context context, final Intent intent) throws Exception
		// {
		// if (context == null || intent == null)
		// {
		// throw new Exception("(context == null || intent == null)");
		// }
		//
		// String logPathIntoInternalStorage = context.getFilesDir().getCanonicalPath() + File.separator;
		// Log.d(TAG, "Path of internal storage fro log: " + logPathIntoInternalStorage);
		//
		// String logbackXmlWithAbsolutePath = StringUtils.replace(LOGBACK_XML, "${log_path:-}", logPathIntoInternalStorage);
		// Log.d(TAG, "LOGBACK_XML: " + logbackXmlWithAbsolutePath);
		//
		// LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		// try
		// {
		// // load a specific logback.xml
		// JoranConfigurator configurator = new JoranConfigurator();
		// configurator.setContext(lc);
		// lc.reset(); // override default configuration
		// // configurator.doConfigure("/path/to/logback.xml");
		//
		// // hard-coded config
		// configurator.doConfigure(new ByteArrayInputStream(logbackXmlWithAbsolutePath.getBytes()));
		//
		// }
		// catch (JoranException je)
		// {
		// // StatusPrinter will handle this
		// }
		// StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
		// }

	}
