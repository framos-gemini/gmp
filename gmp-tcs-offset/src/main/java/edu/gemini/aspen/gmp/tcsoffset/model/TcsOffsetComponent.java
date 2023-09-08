package edu.gemini.aspen.gmp.tcsoffset.model;

import com.google.gson.JsonObject;
import edu.gemini.aspen.gmp.tcsoffset.jms.JmsTcsOffsetDispatcher;
import edu.gemini.aspen.gmp.tcsoffset.jms.TcsOffsetRequestListener;
import edu.gemini.epics.EpicsObserver;
import edu.gemini.epics.EpicsReader;
import edu.gemini.jms.api.*;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

import javax.jms.JMSException;
import java.util.logging.Logger;
import edu.gemini.epics.EpicsWriter;
/**
 * Interface to define a composite of several TCS Offset objects
 */
public class TcsOffsetComponent implements JmsArtifact {
    public static final Logger LOG = Logger.getLogger(TcsOffsetComponent.class.getName());

    private final Boolean simulation;

    private final String tcsTop;
    private final EpicsObserver _eo;
    private final JsonObject _tcsChLoops;


    /**
     * The JMS Offset Dispatcher is a JMS Producer message
     * that will send the TCS Offset to the requester
     */
    private JmsTcsOffsetDispatcher _dispatcher;

    /**
     * JMS Listener to process the TCS Offset requests.
     */
    private TcsOffsetRequestListener _listener;

    /**
     * Message consumer used to receive TCS Offset requests
     */
    private BaseMessageConsumer _messageConsumer;
    private final EpicsWriter _ew1;

    private final EpicsReader _er;
    private TcsOffsetIOC _tcsOffsetIOC;

    public static String TCS_TOP_PROPERTY = "tcsTop";

    public static String SIMULATION = "simulation";

    public static String OFFSETCONFIG = "offsetConfig";

    public static String OFFSETLOOKUP = "mcsOffsetLookUp";

    public JsonObject _offsetConfig;


    public TcsOffsetComponent(EpicsWriter ew1, EpicsReader er, 
                              EpicsObserver eo, String tcsTop, 
                              Boolean simulation, JsonObject offsetConfig,
                              JsonObject jsonTcsChLoops) {

        this.tcsTop = tcsTop;
        this.simulation = simulation;
        _ew1 = ew1;
        _er = er;
        _eo = eo;
        _dispatcher = new JmsTcsOffsetDispatcher("TCS Offset Replier");
        _offsetConfig = offsetConfig;
        _tcsChLoops = jsonTcsChLoops;
        _listener = new TcsOffsetRequestListener(_dispatcher, _offsetConfig);
        //Creates the TCS Offset Request Consumer
        _messageConsumer = new BaseMessageConsumer("JMS TCS Offset Request Consumer",
                                                    new DestinationData(TcsOffsetRequestListener.DESTINATION_NAME, DestinationType.TOPIC),
                                                    _listener
        );

    }

    public void start() throws JMSException, CAException, TimeoutException {
        LOG.info("Starting service, simulation is: " + simulation);
        if (!simulation) {
                _tcsOffsetIOC = new EpicsTcsOffsetIOC(_ew1,_er, _eo, tcsTop, _tcsChLoops);
                _listener.registerTcsOffsetFetcher(_tcsOffsetIOC);
        } else {
            LOG.warning("TCS in simulation mode");
        }
    }


    public void stop() {
        if (!simulation) {
            removeOldTcsOffsetFetcher();
        }
    }

    private void removeOldTcsOffsetFetcher() {
        if (_tcsOffsetIOC != null) {
            LOG.info("Removed old instance of EPICS writer");
            _listener.registerTcsOffsetFetcher(null);
        }
    }

    @Override
    public void startJms(JmsProvider provider) throws JMSException {
        LOG.info("TCS Offset validated, starting... ");
        _dispatcher.startJms(provider);
        _messageConsumer.startJms(provider);

        LOG.info("TCS Offset Service started");
    }

    @Override
    public void stopJms() {
        LOG.info("TCS Offset stopped, disconnecting jms... ");
        _dispatcher.stopJms();
        _messageConsumer.stopJms();
        LOG.info("TCS Offset Service Stopped");
    }
}
