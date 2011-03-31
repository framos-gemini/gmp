package edu.gemini.aspen.gmp.commands.records;

import edu.gemini.cas.Channel;
import edu.gemini.cas.ChannelAccessServer;
import edu.gemini.cas.ChannelListener;
import gov.aps.jca.CAException;
import gov.aps.jca.dbr.DBR;
import org.apache.felix.ipojo.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class ApplyRecord
 *
 * @author Nicolas A. Barriga
 *         Date: 3/17/11
 */
@Component
public class ApplyRecord {
    private static final Logger LOG = Logger.getLogger(ApplyRecord.class.getName());


    private Channel<Dir> dir;
    private Channel<Integer> val;
    private Channel<String> mess;
    private Channel<String> omss;
    private Channel<Integer> clid;

    private final CarRecord car;

    private final ChannelAccessServer cas;

    private final String prefix;
    private final String name;
    private final List<EpicsCad> cads = new ArrayList<EpicsCad>();
    private final List<CarRecord> cars = new ArrayList<CarRecord>();

    /**
     * Constructor
     *
     * @param cas Channel Access Server to use
     * @param prefix instrument prefix. ex.: "gpi"
     */
    protected ApplyRecord(@Requires ChannelAccessServer cas,
                          @Property(name = "prefix", value = "INVALID", mandatory = true) String prefix) {
        this.cas = cas;
        this.prefix = prefix;
        this.name = "apply";
        car = new CarRecord(cas, prefix + ":" + name + "C");
        LOG.info("Constructor");

    }

    /**
     * Create Channels and start CAR
     */
    @Validate
    public synchronized void start() {
        LOG.info("Validate");
        try {
            dir = cas.createChannel(prefix + ":" + name + ".DIR", Dir.CLEAR);
            dir.registerListener(new DirListener());
            val = cas.createChannel(prefix + ":" + name + ".VAL", 0);
            mess = cas.createChannel(prefix + ":" + name + ".MESS", "");
            omss = cas.createChannel(prefix + ":" + name + ".OMSS", "");
            car.start();

            clid = cas.createChannel(prefix + ":" + name + ".CLID", 0);

        } catch (CAException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Destroy Channels and stop CAR
     */
    @Invalidate
    public synchronized void stop() {
        LOG.info("InValidate");
        cas.destroyChannel(dir);
        cas.destroyChannel(val);
        cas.destroyChannel(mess);
        cas.destroyChannel(omss);
        car.stop();
        cas.destroyChannel(clid);
//        for(CadRecord cad:cads){
//            cad.stop();
//        }
    }

    /**
     * Add a CAD to the list of CADs to be notified of new directives
     *
     * @param cad
     */
    @Bind(aggregate = true, optional = true)
    public synchronized void bindCad(CadRecord cad) {
        LOG.info("Bind");

        cads.add(cad.getEpicsCad());
        cars.add(cad.getCar());
        LOG.info(cars.toString());
        cad.getCar().registerListener(new CarListener());
    }

    @Unbind(aggregate = true, optional = true)
    public synchronized void unBindCad(CadRecord cad) {
        LOG.info("Unbind");

        cads.remove(cad.getEpicsCad());
        cars.remove(cad.getCar());
    }

    /**
     * indicates that the record is currently processing a directive
     */
    volatile private boolean processing = false;
    private synchronized boolean processDir(Dir dir) throws CAException {
        processing = true;
        if (dir == Dir.START) {
            incAndGetClientId();
        }
        car.changeState(CarRecord.Val.BUSY, "", 0, getClientId());
        boolean retVal = processInternal(dir);

        if (retVal) {
            boolean idle = true;
            for (CarRecord otherCar : cars) {
                if (otherCar.getState() != CarRecord.Val.IDLE) {
                    idle = false;
                }
            }
            if (idle){
                car.changeState(CarRecord.Val.IDLE, "", 0, getClientId());
            }
        } else {
            car.changeState(CarRecord.Val.ERR, ((String[]) mess.getDBR().getValue())[0], ((int[]) val.getDBR().getValue())[0], getClientId());
        }
        processing = false;
        return retVal;
    }


    private boolean processInternal(Dir dir) throws CAException {
        if (dir == Dir.MARK) {
            return false;
        }
        setMessage("");
        if (dir == Dir.START) {
            if (!processInternal(Dir.PRESET)) {
                return false;
            }
        }
        //set clid and dir on all CADs
        int id = getClientId();
        boolean error = false;
        for (EpicsCad cad : cads) {
            UpdateListener listener = new UpdateListener();
            cad.registerValListener(listener);
            LOG.info("listener registered");

            cad.setDir(dir, id);
            LOG.info("commands sent");
            Integer value = -1;

            try {
                listener.await();
                LOG.info("latch released");

                cad.unRegisterValListener(listener);
                value = ((int[]) listener.getDBR().getValue())[0];

            } catch (InterruptedException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);  //To change body of catch statement use File | Settings | File Templates.
                cad.unRegisterValListener(listener);

            }
            LOG.info("all ok");

            if (value != 0) {
                val.setValue(value);
                setMessage(cad.getMess());
                error = true;
                break;
            } else {
                val.setValue(id);
            }
        }
        return !error;
    }


    private int getClientId() throws CAException {
        return clid.getFirst();
    }

    private void setMessage(String message) throws CAException {
        DBR dbr = mess.getDBR();
        String oldMessage = ((String[]) dbr.getValue())[0];
        if (setIfDifferent(mess, message)) {
            omss.setValue(oldMessage);
        }
    }

    /**
     * Set the Channel value if the new value is different than the old.
     *
     * @param ch
     * @param value
     * @param <T>
     * @return true if channel was written to, false otherwise
     * @throws CAException
     */
    static private <T> boolean setIfDifferent(Channel<T> ch, T value) throws CAException {
        if (!value.equals(ch.getFirst())) {
            ch.setValue(value);
            return true;
        } else {
            return false;
        }
    }


    private int incAndGetClientId() throws CAException {
        int value = getClientId();
        clid.setValue(++value);
        return value;

    }

    /**
     * This listener will be called when a directive is written to the DIR field
     */
    private class DirListener implements ChannelListener {
        @Override
        public void valueChange(DBR dbr) {
            try {
                processDir(Dir.values()[((short[]) dbr.getValue())[0]]);

            } catch (CAException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /**
     * This listener will be invoked when any CAR changes state, and will reflect the states in the main CAR
     */
    private class CarListener implements edu.gemini.aspen.gmp.commands.records.CarListener {
        @Override
        public void update(CarRecord.Val state, String message, Integer errorCode, Integer id) {
            synchronized (ApplyRecord.this) {
                try {
                    if (state == CarRecord.Val.ERR) {
                        car.changeState(state, message, errorCode, id);
                    }
                    if (!processing && state == CarRecord.Val.IDLE) {
                        for (CarRecord otherCar : cars) {
                            if (otherCar.getState() != CarRecord.Val.IDLE) {
                                return;
                            }
                        }
                        car.changeState(state, message, errorCode, id);
                    }
                } catch (CAException e) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }
}
