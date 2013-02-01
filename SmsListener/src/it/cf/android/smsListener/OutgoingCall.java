package it.cf.android.smsListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutgoingCall
        extends Call
	{
		static private final Logger LOG = LoggerFactory.getLogger(OutgoingCall.class);

		public OutgoingCall(String phoneNumber)
			{
				super(CallDirection.OUTGOING, phoneNumber);
				this.setState(CallState.RINGING);
				LOG.debug("Outgoing call created <ringing>");
			}

		public void refused()
			{
				if (this.getState() != CallState.RINGING)
					{
						LOG.warn("Setting in <refused> state e not <ringing> Outgoing call");
					}
				this.setState(CallState.REFUSED);
				this.setTimestampEndCall(System.currentTimeMillis());
				LOG.debug("Outgoing call refused");
			}

		public void accepted()
			{
				if (this.getState() != CallState.RINGING)
					{
						LOG.warn("Setting in <accepted> state e not <ringing> Outgoing call");
					}

				this.setState(CallState.ACCEPTED);
				this.setTimestampEndCall(System.currentTimeMillis());
				LOG.debug("Outgoing call accepted");
			}

		public void finished()
			{
				if (this.getState() != CallState.ACCEPTED)
					{
						LOG.warn("Setting in <finished> state e not <accepted> Outgoing call");
					}
				this.setState(CallState.FINISHED);
				this.setTimestampEndCall(System.currentTimeMillis());
				LOG.debug("Outgoing call finished");
			}

	}
