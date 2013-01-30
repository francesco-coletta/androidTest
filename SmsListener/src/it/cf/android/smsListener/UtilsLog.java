package it.cf.android.smsListener;

public class UtilsLog
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

	}
