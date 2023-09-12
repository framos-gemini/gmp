package edu.gemini.aspen.gmp.tcsoffset.jms;

import edu.gemini.jms.api.BaseMessageProducer;
//import edu.gemini.aspen.giapi.offset.OffsetType;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.logging.Logger;

/**
 * The message producer used to send the TCS Offset back to the requester.
 */
public class JmsTcsOffsetDispatcher extends BaseMessageProducer {
    private static final Logger LOG = Logger.getLogger(JmsTcsOffsetDispatcher.class.getName());

    public JmsTcsOffsetDispatcher(String clientName) {
        super(clientName, null);
    }

    public void sendOffsetResult(Destination destination, String offsetApplied) throws JMSException {
        TextMessage msg = _session.createTextMessage();
        msg.setText(offsetApplied);
        _producer.send(destination, msg);
    }
}
