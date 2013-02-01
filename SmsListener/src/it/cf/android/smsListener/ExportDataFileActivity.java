package it.cf.android.smsListener;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class ExportDataFileActivity
        extends Activity
	{
		static private final Logger LOG = LoggerFactory.getLogger(ExportDataFileActivity.class);

		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);

				Button bottoneAvvia = new Button(this);
				bottoneAvvia.setText("Esporta file");
				bottoneAvvia.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
							{
								new ExportTask(v.getContext()).doInBackground();
							}
					});

				Button bottoneArresta = new Button(this);
				bottoneArresta.setText("Clean");
				bottoneArresta.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
							{
								LOG.debug("TODO");
							}
					});
				LinearLayout layout = new LinearLayout(this);
				layout.setOrientation(LinearLayout.VERTICAL);
				layout.addView(bottoneAvvia);
				layout.addView(bottoneArresta);
				setContentView(layout);
			}

		private class ExportTask
		        extends AsyncTask<String, Integer, String>
			{

				private final Context context;

				public ExportTask(Context context)
					{
						super();
						this.context = context;
					}

				@Override
				protected String doInBackground(String... arg0)
					{
						LOG.debug("Export Started");
						if (isExternalStorageAvailableAndWriteable())
							{
								cleanChacheDirIntoExternalStorage();
								copyDataFileIntoExternalStorage();
							}
						else
							{
								LOG.warn("External storage isn't writable. Skip export");
							}
						LOG.debug("Export finished");
						return null;
					}

				private void cleanChacheDirIntoExternalStorage()
					{
						LOG.debug("Pulisco la directory <{}>", context.getExternalCacheDir());
						File[] files = context.getExternalCacheDir().listFiles();
						for (File file : files)
							{
								file.delete();
							}
						LOG.debug("Eliminati {} file", files.length);
					}

				private void copyDataFileIntoExternalStorage()
					{
						try
							{
								String pathDataFiles = context.getFilesDir().getCanonicalPath() + File.separator;
								String pathWhereCopyDataFiles = context.getExternalCacheDir() + File.separator;
								LOG.debug("Copia dei file dei dati dalla dir <{}> nella dir <{}>", pathDataFiles, pathWhereCopyDataFiles);

								File[] files = context.getFilesDir().listFiles();
								String dataFileName;
								String fileNameWithPath;
								String newFileNameWithPath;
								for (File file : files)
									{
										dataFileName = file.getName();
										LOG.debug("Copy del file <{}>", dataFileName);
										fileNameWithPath = pathDataFiles + dataFileName;
										newFileNameWithPath = pathWhereCopyDataFiles + dataFileName;
										Utils.copy(fileNameWithPath, newFileNameWithPath);
									}
								LOG.debug("Copiati {} file", files.length);
							}
						catch (IOException e)
							{
								LOG.error(e.getMessage());
							}
					}

				private boolean isExternalStorageAvailableAndWriteable()
					{
						boolean mExternalStorageAvailable = false;
						boolean mExternalStorageWriteable = false;

						String state = Environment.getExternalStorageState();

						if (Environment.MEDIA_MOUNTED.equals(state))
							{
								// We can read and write the media
								mExternalStorageAvailable = mExternalStorageWriteable = true;
							}
						else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
							{
								// We can only read the media
								mExternalStorageAvailable = true;
								mExternalStorageWriteable = false;
							}
						else
							{
								// Something else is wrong. It may be one of many other states, but all we need
								// to know is we can neither read nor write
								mExternalStorageAvailable = mExternalStorageWriteable = false;
							}

						LOG.debug("State of external storage [available, writable]= [{}, {}]", mExternalStorageAvailable, mExternalStorageWriteable + "]");

						return mExternalStorageWriteable;
					}

			}

	}
