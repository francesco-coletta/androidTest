package it.cf.android.smsListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

public class RepositorySmsFile
        implements RepositorySms
	{
		private static final String FILE_NAME = "SmsLog";
		private static final int MAX_SIZE_FILE_BYTE = 1 * 1024 * 1024; // 1MB
		// private static final int MAX_SIZE_FILE_BYTE = 1 * 1024; // x Test 1KB

		private final Context context;

		private static final String TAG = "RepositorySmsFile";
		private int numTabs = 0;

		public RepositorySmsFile(Context context)
		        throws Exception
			{
				super();

				if (context == null)
					{
						throw new Exception("context == null");
					}
				this.context = context;
			}

		@Override
		public void writeSms(Sms sms)
			{
				numTabs++;
				if (sms == null)
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + "Sms null");
					}
				else
					{
						writeSingleSmsToFile(sms);
					}
				numTabs--;
			}

		@Override
		public void writeSms(List<Sms> smss)
			{
				numTabs++;
				if (smss == null)
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + "Sms null");
					}
				else
					{
						writeMultipleSmsToFile(smss);
					}
				numTabs--;
			}

		private void writeMultipleSmsToFile(List<Sms> smss)
			{
				FileOutputStream outputStream;
				try
					{
						outputStream = openOutputFile(FILE_NAME);
						if (outputStream != null)
							{
								Log.d(TAG, UtilsLog.getTab(numTabs) + "File <" + FILE_NAME + "> open");
								for (Sms smsMessage : smss)
									{
										writeSingleSmsToFile(outputStream, smsMessage);
									}
								outputStream.close();
								Log.d(TAG, UtilsLog.getTab(numTabs) + "File <" + FILE_NAME + "> closed");
							}
					}
				catch (Exception e)
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + e.getMessage());
					}
			}

		private void writeSingleSmsToFile(Sms sms)
			{
				try
					{
						FileOutputStream outputStream;
						outputStream = openOutputFile(FILE_NAME);
						if (outputStream != null)
							{
								Log.d(TAG, UtilsLog.getTab(numTabs) + "File <" + FILE_NAME + "> opened");
								writeSingleSmsToFile(outputStream, sms);
								outputStream.close();
								Log.d(TAG, UtilsLog.getTab(numTabs) + "File <" + FILE_NAME + "> closed");
							}
					}
				catch (Exception e)
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + e.getMessage());
					}
			}

		private void writeSingleSmsToFile(FileOutputStream outputStream, Sms sms)
			{
				numTabs++;
				if (outputStream != null && sms != null)
					{
						try
							{
								outputStream.write(sms.toString().getBytes());
								Log.d(TAG, UtilsLog.getTab(numTabs) + "Writed " + sms.toString());
							}
						catch (Exception e)
							{
								Log.e(TAG, UtilsLog.getTab(numTabs) + e.getMessage());
							}
					}
				else
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + "(outputStream == null) oppure (sms == null)");
					}
				numTabs--;
			}

		private FileOutputStream openOutputFile(String filename) throws Exception
			{
				if (context == null)
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + "Il context non deve essere null");
						throw new Exception("Il context non deve essere null");
					}
				if ((filename == null) || (filename.length() == 0))
					{
						Log.e(TAG, UtilsLog.getTab(numTabs) + "Il filename non deve essere null");
						throw new Exception("Il filename non deve essere null o vuoto");
					}

				numTabs++;
				if (fileSizeIsLowerThanMaxValue(context, filename))
					{
						Log.d(TAG, UtilsLog.getTab(numTabs) + "Le dimensioni del file <" + filename + "> sono < al max (" + MAX_SIZE_FILE_BYTE + ")");
					}
				else
					{
						Log.d(TAG, UtilsLog.getTab(numTabs) + "Le dimensioni del file <" + filename + "> sono > al max (" + MAX_SIZE_FILE_BYTE + ")");

						String fileNameWithPath = context.getFilesDir().getCanonicalPath() + File.separator + filename;
						String newFileNameWithPath = context.getFilesDir().getCanonicalPath() + File.separator + filename + "_" + (String) DateFormat.format("yyyyMMddhhmm", new Date(System.currentTimeMillis()));

						Log.e(TAG, UtilsLog.getTab(numTabs) + "Copy file from <" + fileNameWithPath + "> to <" + newFileNameWithPath + ">");
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
						Log.e(TAG, UtilsLog.getTab(numTabs) + e.getMessage());
					}
				numTabs--;
				return outputStream;
			}

		private boolean fileSizeIsLowerThanMaxValue(Context context, String filename)
			{
				numTabs++;
				File file = new File(context.getFilesDir(), filename);
				Log.d(TAG, UtilsLog.getTab(numTabs) + "Dimensioni file <" + filename + "> = " + file.length());
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

	}
