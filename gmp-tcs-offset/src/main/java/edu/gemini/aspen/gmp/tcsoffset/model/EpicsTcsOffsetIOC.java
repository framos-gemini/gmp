package edu.gemini.aspen.gmp.tcsoffset.model;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import edu.gemini.aspen.giapi.offset.OffsetType;
import edu.gemini.epics.*;
import edu.gemini.epics.impl.ReadWriteEpicsEnumChannel;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class implements the logic to apply an offset in the IOC TCS.
 * This first version implements the logic to execute an Instrument offset
 * which is the same as gacq applies.
 */
public class EpicsTcsOffsetIOC implements TcsOffsetIOC {

    /**
     * TCS offset channel EPICS IOC
     */
    public static final String TCS_OFFSET_CHANNEL = "poAdjust";

    public static final String TCS_INPOSITION_CHANNEL = "sad:inPosition";

    public static final String TCS_TOP_SIM = "tc1";

    private static final Logger LOG = Logger.getLogger(EpicsTcsOffsetIOC.class.getName());
    private final EpicsObserver _eo;

    private  JsonObject _tcsChLoops = new JsonObject();

    private EpicsWriter _ew1;

    private EpicsReader _er;

    /**
     * Actual channel name to obtain the TCS Context from Gemini
     */
    private final String _tcsOffsetChannel;

    /**
     * The reference to the EPICS channel
     */
    private ReadWriteClientEpicsChannel<String> _trackingFrameChannel;
    private ReadWriteClientEpicsChannel<String> _offsetSizeChannel;
    private ReadWriteClientEpicsChannel<String> _angleChannel;
    private ReadWriteClientEpicsChannel<String> _virtualTelChannel;

    private HashMap<String, ReadWriteClientEpicsChannel<String>> _chLoops;


    private ReadWriteEpicsEnumChannel<Dir> _tcsApply;


    private Boolean _tcsIsInPosition;

    private CARSTATE _tcsState;

    private boolean _isWaitingForState = false;

    private boolean _isWaitingForInPos = false;

    private Pattern _pRegex = Pattern.compile("\\{\\w+\\}");

    private static final long  TCS_WAITING_IDLE = 5000; // time to wait for the TCS in IDLE position

    private static final long  TCS_WAITING_MIN_TIME = 500; // time to wait in a loop milliseconds

    private static final long  TCS_WAITING_INPOS = 60000; // time to wait for the TCS in IDLE position
    private TCSSTATUS _tcsStatus;
    private String _tcsErrorMsg;


    /**
     * Constructor. Takes as an argument the EPICS reader to get access
     * to EPICS, and the TCS Offset channel to use. If the TCS Offset
     * channel is <code>null</code> the default TCS_CONTEXT_CHANNEL is
     * used.
     *
     * @param ew1        The EPICS writer
     * @param tcsTop the channel containing the TCS Context
     * @throws TcsOffsetException in case there is a problem obtaining
     *                             the TCS Context
     */
    //protected EpicsTcsOffsetIOC(EpicsWriter ew2, ChannelAccessServer channelAccess, String tcsOffsetChannel)
    //protected EpicsTcsOffsetIOC(EpicsWriter ew2, String tcsOffsetChannel)
    protected EpicsTcsOffsetIOC(EpicsWriter ew1, EpicsReader er, EpicsObserver eo, String tcsTop, JsonObject tcsChLoops)  {

        if (tcsTop == null) {
            _tcsOffsetChannel = TCS_TOP_SIM + TCS_OFFSET_CHANNEL;

        } else {
            _tcsOffsetChannel = tcsTop + TCS_OFFSET_CHANNEL;
        }
        _ew1 = ew1;
        _er = er;
        _eo = eo;
        _tcsIsInPosition = false;
        _isWaitingForState = false;
        _tcsStatus = TCSSTATUS.OK;
        _tcsErrorMsg = null;
        parseJsonObj(tcsChLoops, _tcsChLoops, tcsChLoops);
        System.out.println(_tcsChLoops);
        initializeChannels();


    }

    private void parseJsonObj(JsonObject tcsChLoops, JsonObject newObj, JsonObject objConfig) {
        Iterator<String> keys = tcsChLoops.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (tcsChLoops.get(key) instanceof  JsonObject) {
                newObj.add(createNewEntryKey(key, objConfig), new JsonObject());
                parseJsonObj(tcsChLoops.getAsJsonObject(key), newObj.get(key).getAsJsonObject(), objConfig);
            }
            else {
                newObj.add(createNewEntryKey(key, objConfig), tcsChLoops.get(key));
            }
        }
    }

    private String createNewEntryKey(String key, JsonObject objConfig)  {
        Matcher m = _pRegex.matcher(key);
        String key1 = null;
        String newKey = key;
        while (m.find( )) {
            for (int i = 0; i <= m.groupCount(); i++) {
                key1 = m.group(i).replace("{", "").replace("}", "");
                if (objConfig.get(key1) != null) {
                    newKey = newKey.replace((CharSequence) m.group(i), (CharSequence) objConfig.get(key1).getAsString());
                }
                else {
                    LOG.warning("Not found the "+key1 + " key declared previously");
                }
            }
        }
        System.out.println("NewKey: "+ newKey);
        return newKey;
    }

    private void setChannelsNull() {
        _trackingFrameChannel = null;
        _offsetSizeChannel = null;
        _angleChannel = null;
        _virtualTelChannel = null;
        _tcsApply = null;
        _chLoops.clear();
        _chLoops.clear();
    }

    private void createLoopChannels(String loopKey,
                                    HashMap<String,  ReadWriteClientEpicsChannel<String>> map) throws TcsOffsetException {
        if (_tcsChLoops.get(loopKey) == null)
            throw new TcsOffsetException(TcsOffsetException.Error.CONFIGURATION_FILE,
                                   "Error, There is not the " + loopKey +
                                   " declared in the tcsChLoops json configuration");

        Iterator<String> keysLoop = _tcsChLoops.get(loopKey).getAsJsonObject().keySet().iterator();
        while (keysLoop.hasNext()) {
            String key = keysLoop.next();
            if (!key.contains("$"))
                map.put(key, _ew1.getStringChannel(key));
        }
    }

    private void initMaps() throws TcsOffsetException{
        _chLoops = new HashMap<>();
        createLoopChannels("openLoop", _chLoops);
        createLoopChannels("closeLoop", _chLoops);
        Set<String> keys = _chLoops.keySet();
        for (String key : keys) {
            ReadWriteClientEpicsChannel<String> epicsChannel = _chLoops.get(key);
        }
    }

    private boolean initializeChannels() {
        try {
            LOG.fine("initializeChannels init");
            // Instrument offset -> 2
            _trackingFrameChannel = _ew1.getStringChannel(_tcsOffsetChannel + ".A");
            _offsetSizeChannel = _ew1.getStringChannel(_tcsOffsetChannel +".B");
            _angleChannel = _ew1.getStringChannel(_tcsOffsetChannel +".C");
            // -14 is the mask in the TCS which is the SOURCE_A.
            _virtualTelChannel = _ew1.getStringChannel(_tcsOffsetChannel + ".D");
            _tcsApply = (ReadWriteEpicsEnumChannel<Dir>) _ew1.getEnumChannel( "tc1:apply.DIR", Dir.class);
            // Monitor Channels
            _eo.registerEpicsClient(new ChannelAccessSubscribe(this::setTcsInPos),
                                    ImmutableList.of("tc1:inPosCombine"));
            _eo.registerEpicsClient(new ChannelAccessSubscribe(this::setTcsStatus),
                    ImmutableList.of("tc1:applyC"));

            _eo.registerEpicsClient(new ChannelAccessSubscribe(this::tcsError),
                    ImmutableList.of("tc1:ErrorVal.VAL"));

            _eo.registerEpicsClient(new ChannelAccessSubscribe(this::tcsErrorMsg),
                    ImmutableList.of("tc1:ErrorMess.VAL"));

            // Create the channels for the open and close sequence loop
            initMaps();
            LOG.fine("FRRR initializeChannels end well. _tcsOffsetChannel: " + _tcsOffsetChannel);
            return true;
        } catch (EpicsException e) {
            LOG.warning("Problem binding "+ _tcsOffsetChannel +
                         " channel. Check the EPICS configuration and your network settings or check if the TCS is running");
            e.printStackTrace();
            setChannelsNull();
        } catch (TcsOffsetException e) {
            LOG.warning("Error in the configuration file, please fix the problem");
            e.printStackTrace();
            setChannelsNull();
        }
        LOG.fine("Not initialized the EPICS channels and monitors");
        return false;
    }

    private void tcsErrorMsg(List<String> lMsg) {
        for (String e : lMsg) {
            _tcsErrorMsg = e;
            System.out.println("TCS error msg: " + _tcsErrorMsg);
        }
    }

    private void tcsError(List<Short> lStatus) {
        for (Short e : lStatus) {
            _tcsStatus = TCSSTATUS.getFromInt(e);
        }
    }

    public void setTcsInPos(List<Double> values) {
        for (Double e : values) {
            synchronized (this) {
                _tcsIsInPosition = (e == 1.0);
                if (_isWaitingForInPos)
                    this.notify();
                System.out.println(System.currentTimeMillis() + "  FR4.0 setTcsInPos val: " + e + " isInPosition: " + _tcsIsInPosition);
            }
        }
    }

    public void setTcsStatus(List<Short> lStatus) {
        for (Short e : lStatus) {
            synchronized (this) {
                _tcsState = CARSTATE.getFromInt(e);
                System.out.println("NEW TCS state: " + CARSTATE.getFromInt(e));
                if (_isWaitingForState)
                    this.notify();
            }
        }
    }

    private boolean areChannelsInit() {
        if (_trackingFrameChannel != null  &&  _offsetSizeChannel != null
            && _angleChannel != null && _virtualTelChannel != null
            && _tcsApply != null && (!_chLoops.isEmpty())) {
            return true;
        }

        return initializeChannels();
    }


    private boolean waitChange(int timeout) {
        try {
            this.wait(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean waitTcs(int timeout, CARSTATE wishState) {
        long rest, t1=0;

        if (wishState == _tcsState)
            return true;
        synchronized (this) {
            t1 = System.currentTimeMillis();
            _isWaitingForState = true;
            waitChange(timeout);
           _isWaitingForState = false;
        }
        rest = System.currentTimeMillis() - t1;

        if (_tcsState != wishState) {
            return false;
        }
        return true;
    }

    private boolean waitTcsInPos(int timeout,  Boolean inPosition) {
        long t1=0;
        long rest=0;
        System.out.println(System.currentTimeMillis() + " . wait for TCS in position : "+ timeout + " inPosition: " + inPosition);
        if (inPosition == _tcsIsInPosition)
            return true;
        synchronized (this) {
            _isWaitingForInPos = true;
            t1 = System.currentTimeMillis();
            waitChange(timeout);
            rest = System.currentTimeMillis() - t1;
            _isWaitingForInPos = false;
        }
        System.out.println(System.currentTimeMillis() + " . currentTcsInPos: "+ _tcsIsInPosition + " timeSpent: "+ rest + " milliseconds");
        if (inPosition != _tcsIsInPosition)
            return false;
        return true;
    }

    private boolean waitTcsInPosBlinking () throws TcsOffsetException {
        System.out.println(System.currentTimeMillis() + " 1. The TCS inPosition is: " +_tcsIsInPosition);
        waitTcsInPos(1000, false);
        System.out.println("2. The TCS inPosition is: " +_tcsIsInPosition);
        if (!_tcsIsInPosition && (!waitTcsInPos(60000, true)))
            throw new TcsOffsetException(TcsOffsetException.Error.TCS_NOT_INPOS, "Tcs is not in position after applying the offset ");
        long t1=0;
        long rest=0;
        System.out.println("3. The TCS inPosition is: " +_tcsIsInPosition);
        boolean inposOld = _tcsIsInPosition;
        if (_tcsIsInPosition && waitTcsInPos(1000, false)) {
            // Blinking the tcsInPosition
            t1 = System.currentTimeMillis();
            System.out.println("4. The TCS inPosition is: " +_tcsIsInPosition);
            int i=0;
            while ((inposOld != _tcsIsInPosition) && ( (System.currentTimeMillis()-t1) > 5000 )) {
                inposOld = _tcsIsInPosition;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
                i++;
            }
            System.out.println("Time consumed: "+ (System.currentTimeMillis()-t1) + " milliseconds. ");
            if ((System.currentTimeMillis()-t1) > 5000 ) {
                System.out.println("The system is obsilating for the las 5000 milliseconds. ");
                return false;
            }
        }
        System.out.println(System.currentTimeMillis() + " 5. The TCS inPosition is: " +_tcsIsInPosition);
        return true;
    }

    private void tcsApply() throws CAException, TimeoutException {
        _tcsApply.setValue(Dir.START);
        if (!waitTcs(1000, CARSTATE.BUSY))
            new TcsOffsetException(TcsOffsetException.Error.TIMEOUT,
                    "TCS was not reached the BUSY state after applying the apply");
        if (!waitTcs(1000, CARSTATE.IDLE))
            new TcsOffsetException(TcsOffsetException.Error.TIMEOUT,
                    "TCS was not reached the IDLE state after applying the apply");
    }

    private void applyOffset(String val, String offsetAngle) throws CAException, TimeoutException {
        _offsetSizeChannel.setValue(val);
        _angleChannel.setValue(offsetAngle);
        tcsApply();
    }

    private void iterateSequence(String loopKey) throws CAException, TimeoutException, TcsOffsetException {
        Iterator<String> keysLoop = _tcsChLoops.get(loopKey).getAsJsonObject().keySet().iterator();
        LOG.fine("Starting the " + loopKey +" sequence");
        int indexCallFunc = -1;
        while (keysLoop.hasNext()) {
            String key = keysLoop.next();
            String val = _tcsChLoops.get(loopKey).getAsJsonObject().get(key).toString().replace("\"","");
            indexCallFunc = key.indexOf("$");
            if (indexCallFunc == -1) {
                if (_chLoops.get(key) != null)
                    _chLoops.get(key).setValue(val);
                else
                    LOG.warning("The next command "+key+" can not be applied");
            }
            else {
                String methodName = key.substring(indexCallFunc+1, key.length());
                try {
                    Method method = this.getClass().getDeclaredMethod(methodName);
                    method.invoke(this);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e ) {
                    e.printStackTrace();
                    throw new TcsOffsetException(TcsOffsetException.Error.CONFIGURATION_FILE,
                            "The tcsChLoops configuration is wrong. The "+ methodName+ " is not implemented.", e);
                }
            }
        }
        LOG.fine("End the : " + loopKey + " sequence. TCS_STATE: " + _tcsState + " TCS_STATUS: " + _tcsStatus);
    }

    @Override
    public void setTcsOffset(double p, double q,
                             OffsetType typeOffse) throws TcsOffsetException {
        LOG.fine("Setting offset  p: "+ p + " q: " + q +" -14");
        if (!areChannelsInit())
            throw new TcsOffsetException(TcsOffsetException.Error.BINDINGCHANNEL,
                                         "Problem binding " + _tcsOffsetChannel +
                                         ".[A|B|C|D] channel. Check the " + TcsOffsetComponent.class.getName()
                                         + "-default.cfg configuration file and your network settings");

        if (_tcsState == CARSTATE.ERROR || _tcsStatus == TCSSTATUS.ERR)
            throw new TcsOffsetException(TcsOffsetException.Error.TCS_STATE,
                                         "There is an error in the TCS, please clean the TCS before continue. ");

        if (!_tcsIsInPosition && (!waitTcsInPos(5000, true)))
            throw new TcsOffsetException(TcsOffsetException.Error.TCS_NOT_INPOS,
                                         "Tcs is not in position before applying the offset ");
        try {
            iterateSequence("openLoop");
            // Applying P offset
            applyOffset(Double.toString(p), "90.0");
            // Applying Q offset
            applyOffset(Double.toString(q), "180.0");
            waitTcsInPosBlinking();
            iterateSequence("closeLoop");
            if (_tcsState == CARSTATE.ERROR || _tcsStatus == TCSSTATUS.ERR)
                throw new TcsOffsetException(TcsOffsetException.Error.TCS_STATE, _tcsErrorMsg);

        } catch (CAException  e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            setChannelsNull();
            throw new TcsOffsetException(TcsOffsetException.Error.TIMEOUT,
                                         "Timeout Error trying to set the TCS epics channel. Please check the TCS status", e);
        } catch (TimeoutException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            throw new TcsOffsetException(TcsOffsetException.Error.TIMEOUT,
                                         "Timeout Error trying to set the TCS epics channel. Please check the TCS status", e);
        } catch (IllegalStateException e) {  // when the TCS is rebooted, it is necessary to force the Epics channels initialization.
                                             // Therefore, the channels are set to null and the next request the service will try
                                             // initialized them.
            LOG.log(Level.WARNING, e.getMessage(), e);
            setChannelsNull();
            throw new TcsOffsetException(TcsOffsetException.Error.TCS_WAS_REBOOTED,
                                          "It is necessary to apply the offset again", e);
        }

    }
}
