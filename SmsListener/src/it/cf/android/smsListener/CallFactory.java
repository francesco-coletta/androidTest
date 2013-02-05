package it.cf.android.smsListener;

import it.cf.android.smsListener.Call.CallDirection;
import it.cf.android.smsListener.Call.CallState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;

public class CallFactory
	{
		static private final Logger LOG = LoggerFactory.getLogger(CallFactory.class);
		private final static CallFactory singleton = new CallFactory();

		private static Call call = null;

		private CallFactory()
			{

			}

		static public CallFactory getInstance()
			{
				return singleton;
			}

		public Call createNewCall(CallDirection direction, Intent intent) throws Exception
			{
				String callingPhoneNumber = getCallingPhoneNumber(intent);
				switch (direction)
					{
					case OUTGOING:
						call = new OutgoingCall(callingPhoneNumber);
						LOG.info("Outgoing call created: {}", call.toString());
						break;
					case INCOMING:
						call = new IncomingCall(callingPhoneNumber);
						LOG.info("Incoming call created: {}", call.toString());
						break;
					default:
						throw new Exception("La direzione deve essere incoming o outgoing");
					}
				return call;
			}

		public Call getCall(int telephonyCallState, Intent intent) throws Exception
			{
				if (call == null)
					{
						throw new Exception("Chiamare prima il metodo createNewCall()");
					}

				switch (telephonyCallState)
					{
					case TelephonyManager.CALL_STATE_RINGING:
						{
							// il telefono squilla, dovrei averlo già gestito nella creazione della chiamata
							LOG.debug("Call already created: {}", call.toString());
							break;
						}
					case TelephonyManager.CALL_STATE_OFFHOOK:
						{
							// la chiamata viene accettata
							if (call.getDirection() == CallDirection.OUTGOING)
								{
									((OutgoingCall) call).accepted();
								}
							else
								{
									((IncomingCall) call).accepted();
								}
							LOG.info("Call accepted: {}", call.toString());
							break;
						}
					case TelephonyManager.CALL_STATE_IDLE:
						{
							/*
							 * la chiamata viene:
							 * - conclusa, se il precedente stato è accettata
							 * - rifiutata, se il precedente stato è ringing, ossia appena creata
							 */
							if (call.getDirection() == CallDirection.OUTGOING)
								{
									OutgoingCall outgoinhCall = ((OutgoingCall) call);
									if (outgoinhCall.getState() == CallState.ACCEPTED)
										{
											outgoinhCall.finished();
										}
									if (outgoinhCall.getState() == CallState.RINGING)
										{
											outgoinhCall.refused();
										}
								}
							else
								{
									IncomingCall incomingCall = ((IncomingCall) call);
									if (incomingCall.getState() == CallState.ACCEPTED)
										{
											incomingCall.finished();
										}
									if (incomingCall.getState() == CallState.RINGING)
										{
											incomingCall.refused();
										}
								}
							LOG.info("Call finished or refused: {}", call.toString());
							break;
						}
					default:
						{
							LOG.error("Inconsistent situation");
							break;
						}
					}
				return call;
			}

		private static String getCallingPhoneNumber(Intent intent)
			{
				String phoneNumber = "UNKNOW";
				if (intent != null && intent.getExtras() != null)
					{
						Bundle bundle = intent.getExtras();
						String state = bundle.getString(TelephonyManager.EXTRA_STATE);
						if ((state != null) && (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)))
							{
								phoneNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
							}
						else if (state == null)
							{
								phoneNumber = bundle.getString(Intent.EXTRA_PHONE_NUMBER);
								// Here: do something with the number
							}
					}
				else
					{
						LOG.error("Intent is null");
					}
				return phoneNumber;
			}

	}
