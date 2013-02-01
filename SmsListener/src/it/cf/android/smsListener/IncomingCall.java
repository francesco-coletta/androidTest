package it.cf.android.smsListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncomingCall
        extends Call
	{
		static private final Logger LOG = LoggerFactory.getLogger(IncomingCall.class);

		public IncomingCall(String phoneNumber)
			{
				super(CallDirection.INCOMING, phoneNumber);
				this.setState(CallState.RINGING);
				LOG.debug("Incoming call created <ringing>");
			}

		public void refused()
			{
				if (this.getState() != CallState.RINGING)
					{
						LOG.warn("Setting in <refused> state e not <ringing> incoming call");
					}
				this.setState(CallState.REFUSED);
				this.setTimestampEndCall(System.currentTimeMillis());
				LOG.debug("Incoming call refused");
			}

		public void accepted()
			{
				if (this.getState() != CallState.RINGING)
					{
						LOG.warn("Setting in <accepted> state e not <ringing> incoming call");
					}

				this.setState(CallState.ACCEPTED);
				this.setTimestampEndCall(System.currentTimeMillis());
				LOG.debug("Incoming call accepted");
			}

		public void finished()
			{
				if (this.getState() != CallState.ACCEPTED)
					{
						LOG.warn("Setting in <finished> state e not <accepted> incoming call");
					}
				this.setState(CallState.FINISHED);
				this.setTimestampEndCall(System.currentTimeMillis());
				LOG.debug("Incoming call finished");
			}

	}
