package it.cf.android.smsListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.text.format.DateFormat;

public class RepositoryFile
        implements Repository
	{
		static private final Logger LOG = LoggerFactory.getLogger(RepositoryFile.class);

		public static final String FILE_NAME = "SmsCallLog";
		private static final int MAX_SIZE_FILE_BYTE = 1 * 1024 * 1024; // 1MB
		// private static final int MAX_SIZE_FILE_BYTE = 1 * 1024; // x Test 1KB

		private final Context context;

		public RepositoryFile(Context context)
		        throws Exception
			{
				super();

				if (context == null)
					{
						throw new Exception("context == null");
					}
				this.context = context;
				LOG.debug("Max dimension allowed for data file <{}> is {} [B]", FILE_NAME, MAX_SIZE_FILE_BYTE);
			}

		@Override
		public void writeSms(Sms sms)
			{
				if (sms == null)
					{
						LOG.error("Sms null");
					}
				else
					{
						writeSingleSmsToFile(sms);
					}
			}

		@Override
		public void writeSms(List<Sms> smss)
			{
				if (smss == null)
					{
						LOG.error("Sms null");
					}
				else
					{
						writeMultipleSmsToFile(smss);
					}
			}

		@Override
		public void writeCall(Call call)
			{
				if (call == null)
					{
						LOG.error("Call null");
					}
				else
					{
						writeSingleCallToFile(call);
					}
			}

		private void writeSingleCallToFile(Call call)
			{
				try
					{
						FileOutputStream outputStream;
						outputStream = openOutputFile(FILE_NAME);
						if (outputStream != null)
							{
								LOG.debug("File <{}> opened", FILE_NAME);
								outputStream.write(call.toString().getBytes());
								LOG.info("Writed to file {} this call {}", FILE_NAME, call.toString());
								outputStream.close();
								LOG.debug("File <{}> closed", FILE_NAME);
							}
					}
				catch (Exception e)
					{
						LOG.error(e.getMessage());
					}
			}

		private void writeMultipleSmsToFile(List<Sms> smss)
			{
				FileOutputStream outputStream;
				try
					{
						outputStream = openOutputFile(FILE_NAME);
						if (outputStream != null)
							{
								LOG.debug("File <{}> opened", FILE_NAME);
								for (Sms smsMessage : smss)
									{
										writeSingleSmsToFile(outputStream, smsMessage);
									}
								outputStream.close();
								LOG.debug("File <{}> closed", FILE_NAME);
							}
					}
				catch (Exception e)
					{
						LOG.error(e.getMessage());
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
								LOG.debug("File <{}> opened", FILE_NAME);
								writeSingleSmsToFile(outputStream, sms);
								outputStream.close();
								LOG.debug("File <{}> closed", FILE_NAME);
							}
					}
				catch (Exception e)
					{
						LOG.error(e.getMessage());
					}
			}

		private void writeSingleSmsToFile(FileOutputStream outputStream, Sms sms)
			{
				if (outputStream != null && sms != null)
					{
						try
							{
								outputStream.write(sms.toString().getBytes());
								LOG.info("Writed to file {} this sms {}", FILE_NAME, sms.toString());
							}
						catch (Exception e)
							{
								LOG.error(e.getMessage());
							}
					}
				else
					{
						LOG.error("(outputStream == null) oppure (sms == null)");
					}
			}

		private FileOutputStream openOutputFile(String filename) throws Exception
			{
				if (context == null)
					{
						LOG.error("Il context non deve essere null");
						throw new Exception("Il context non deve essere null");
					}
				if ((filename == null) || (filename.length() == 0))
					{
						LOG.error("Il filename non deve essere null");
						throw new Exception("Il filename non deve essere null o vuoto");
					}

				if (fileSizeIsLowerThanMaxValue(context, filename))
					{
						LOG.debug("La dimensione del file <{}> [B] sono < al max ({})", filename, MAX_SIZE_FILE_BYTE);
					}
				else
					{
						LOG.debug("La dimensione del file <{}> [B] sono > al max ({})", filename, MAX_SIZE_FILE_BYTE);

						String fileNameWithPath = context.getFilesDir().getCanonicalPath() + File.separator + filename;
						String newFileNameWithPath = context.getFilesDir().getCanonicalPath() + File.separator + filename + "_" + (String) DateFormat.format("yyyyMMddhhmm", new Date(System.currentTimeMillis()));

						LOG.debug("Copy file from <{}> to ({})", fileNameWithPath, newFileNameWithPath);
						Utils.copy(fileNameWithPath, newFileNameWithPath);

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
						LOG.error(e.getMessage());
					}

				String fileNameWithPath = context.getFilesDir().getCanonicalPath() + File.separator + filename;
				LOG.debug("Opened file <{}>", fileNameWithPath);
				return outputStream;
			}

		private boolean fileSizeIsLowerThanMaxValue(Context context, String filename)
			{
				File file = new File(context.getFilesDir(), filename);
				LOG.debug("Dimensioni file <{}> = {}", filename, file.length());
				return (!file.exists()) || (file.length() < MAX_SIZE_FILE_BYTE);
			}
	}
