package it.cf.android.smsListener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils
	{

		public static String getTab(final int numTabs)
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

		public static String formatDatetime(Date datetime)
			{
				String datetimeFormatted = "";

				if (datetime != null)
					{
						SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						datetimeFormatted = sdfDate.format(datetime);
					}
				return datetimeFormatted;
			}

		public static String formatDatetime(long datetime)
			{
				String datetimeFormatted = "";
				if (datetime > 0)
					{
						datetimeFormatted = formatDatetime(new Date(datetime));
					}
				return datetimeFormatted;
			}

		public static void copy(String filenameSrc, String filenameDest) throws IOException
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
