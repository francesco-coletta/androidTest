package it.cf.android.smsListener;

import java.util.List;

public interface RepositorySms
	{

		void writeSms(Sms sms);

		void writeSms(List<Sms> sms);

	}
