package it.cf.android.smsListener.activity;

import it.cf.android.smsListener.InitApp;
import it.cf.android.smsListener.R;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AccessCallSmsListenerActivity
        extends Activity
        implements OnClickListener
	{
		static public final String APP_PROP_NAME_PASSWORD = "password";

		static private final Logger LOG = LoggerFactory.getLogger(AccessCallSmsListenerActivity.class);
		TextView messaggioEsitoTask;

		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				this.setContentView(R.layout.activity_access_callsmslistener);
				Button buttonOk = (Button) findViewById(R.loginActvity.buttonOk);
				buttonOk.setOnClickListener(this);
				Button buttonCancel = (Button) findViewById(R.loginActvity.buttonCancel);
				buttonCancel.setOnClickListener(this);

				clearTypedPasword();
			}

		@Override
		public void onClick(View v)
			{
				LOG.debug("Chiave della view {}", v.getId());

				if (v.getId() == R.loginActvity.buttonOk)
					{
						LOG.debug("Cliccato sul button OK");
						if (isTypedPasswordCorrect(v))
							{
								this.startActivity(new Intent(this, ExportDataFileActivity.class));
								this.finish();
							}
						else
							{
								Toast toast = Toast.makeText(this, "La password inserita è sbagliata. Riprovare.", Toast.LENGTH_SHORT);
								toast.show();
								clearTypedPasword();
							}
					}
				else if (v.getId() == R.loginActvity.buttonCancel)
					{
						this.finish();
					}
			}

		private boolean isTypedPasswordCorrect(View v)
			{
				boolean isTypedPasswordCorrect = false;

				TextView password = (TextView) findViewById(R.loginActvity.textPassword);
				String typedPassword = password.getText().toString();
				LOG.debug("Password inserita = {}", typedPassword);

				SharedPreferences pref = v.getContext().getSharedPreferences(InitApp.APP_FILE_PREFERENCES, Context.MODE_PRIVATE);
				if (pref.contains(APP_PROP_NAME_PASSWORD))
					{
						LOG.debug("La password è presente nel file delle preferenze");
						String storedPassword = pref.getString(APP_PROP_NAME_PASSWORD, "");
						LOG.debug("Stored password = {}", storedPassword);
						isTypedPasswordCorrect = StringUtils.equals(typedPassword, storedPassword);
					}
				else
					{
						LOG.error("La password non è presente nel file delle preferenze. Impossibile accedere");
					}
				return isTypedPasswordCorrect;
			}

		private void clearTypedPasword()
			{
				TextView password = (TextView) findViewById(R.loginActvity.textPassword);
				password.setText("");

			}

	}
