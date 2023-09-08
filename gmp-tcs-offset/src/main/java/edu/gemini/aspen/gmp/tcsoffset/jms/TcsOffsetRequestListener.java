package edu.gemini.aspen.gmp.tcsoffset.jms;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.gemini.aspen.giapi.util.jms.JmsKeys;
import edu.gemini.aspen.giapi.offset.OffsetType;
import edu.gemini.aspen.gmp.tcsoffset.model.EpicsTcsOffsetIOC;
import edu.gemini.aspen.gmp.tcsoffset.model.TcsOffsetException;
import edu.gemini.aspen.gmp.tcsoffset.model.TcsOffsetIOC;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

import javax.jms.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * The Listener to receive TCS Offset requests
 */
public class TcsOffsetRequestListener implements MessageListener {
    private static final Logger LOG = Logger.getLogger(TcsOffsetRequestListener.class.getName());
    public static final String DESTINATION_NAME = JmsKeys.GMP_TCS_OFFSET_DESTINATION;

    /**
     * Message producer used to send the TCS Offset back to the requester
     */
    private final JmsTcsOffsetDispatcher _dispatcher;
    private final JsonObject _offsetConfig;

    /**
     * TCS Offset fetcher, used to obtain the TCS Offset.
     */
    private TcsOffsetIOC _epicsTcsOffsetIOC;

    /**
     * Constructor. Takes as an argument the JMS dispatcher that will
     * be used to reply back to the requester.
     *
     * @param dispatcher JMS Dispatcher that sends back the TCS Offset
     */
    public TcsOffsetRequestListener(JmsTcsOffsetDispatcher dispatcher, JsonObject offsetConfig) {
        if (dispatcher == null) {
            throw new IllegalArgumentException("Cannot construct TcsOffsetRequestListener with a null dispatcher");
        }
        System.out.println("TcsOffsetRequestListener created");
        _dispatcher = dispatcher;
        _offsetConfig = offsetConfig;
    }

    /**
     * Register the TCS Offset Fetcher that will be used by this listener.
     * If <code>null</code>, this listener won't reply back.
     *
     * @param epicsTcsOffsetIOC is used to apply the new offset to the TCS.
     *
     */
    public void registerTcsOffsetFetcher(TcsOffsetIOC epicsTcsOffsetIOC) {
        _epicsTcsOffsetIOC = epicsTcsOffsetIOC;
    }

    /**
     * Receives the request. Gets the destination to reply,
     * obtains the TCS Offset (if possible) and send it
     * back to the requester.
     *
     * @param message A message with a TCS Offset request.
     */
    public void onMessage(Message message) {

        LOG.log(Level.FINER, "Message received");
        boolean sendResponse = processApplyOffset(message);

        try {
            LOG.log(Level.FINER, "Sending reapply");
            _dispatcher.sendOffsetResult(message.getJMSReplyTo(), sendResponse);
        }catch (JMSException e) {
            LOG.log(Level.WARNING, "Error sending the response to the client", e);
        }
    }

    private boolean processApplyOffset(Message message)  {
        // Reading the message parameters sent by the Instrument.
        // The first parameter is the P (arcseconds units)
        // The second parameter is the Q (arseconds units)
        // The third parameter is the type of offset which is related with the Sequence Stage. (ACQ or SLOW Guiding Correction).
        try {
            Destination replyDestination = message.getJMSReplyTo();
            BytesMessage msg = (BytesMessage) message;

            // Apply the offset to the TCS
            sendOffset( msg.readDouble(),
                        msg.readDouble(),
                        OffsetType.getFromInt(msg.readInt()),
                        message.getStringProperty("instName"));
            return true;
        } catch (JMSException e) {
            LOG.log(Level.WARNING, "Error reading the msg from the client", e);
        } catch (TcsOffsetException e) {
            LOG.log(Level.WARNING, "Problem applying TCS Offset", e);
        }
        return false;
    }

    private void checkLimits(JsonObject obj, double value) throws TcsOffsetException {
        double max = obj.get("max").getAsDouble();
        double min = obj.get("min").getAsDouble();
        System.out.println("maxC: "+ max + " minC: "+ min + " value: "+ value);
        if (  value > max || value < min)
            throw new TcsOffsetException(TcsOffsetException.Error.OUT_OF_LIMIT,
                                         "The "+ value + " is out the limit defined for the instrument. Max: "
                                         + max + " . Min: " + min);
    }

    private void sendOffset(double p, double q, OffsetType offsetType, String instName) throws TcsOffsetException {
        try {
            JsonObject obj = _offsetConfig.getAsJsonObject(instName).getAsJsonObject(offsetType.getText()).getAsJsonObject("offset");
            System.out.println("OffsetType: "+ offsetType);
            System.out.println(obj);
            System.out.println(obj.getAsJsonObject("p"));
            System.out.println(obj.getAsJsonObject("q"));
            checkLimits(obj.getAsJsonObject("p"), p);
            checkLimits(obj.getAsJsonObject("q"), q);
            _epicsTcsOffsetIOC.setTcsOffset(p,q,offsetType);
        } catch (CAException e) {
            LOG.log(Level.WARNING, "It was not possible to communicate with the TCS, please check the network configuration");
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            LOG.log(Level.WARNING, "");
            throw new TcsOffsetException("Timeout TCS connection, try again to send the offset", e);
        }
    }

    private boolean canDispatchOffset(Destination d) throws TcsOffsetException {
        return _epicsTcsOffsetIOC != null && d!= null;
    }
}
