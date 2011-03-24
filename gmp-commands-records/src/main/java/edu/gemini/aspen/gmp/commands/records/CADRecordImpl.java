package edu.gemini.aspen.gmp.commands.records;

import edu.gemini.aspen.giapi.commands.*;
import edu.gemini.cas.ChannelAccessServer;
import edu.gemini.cas.ChannelListener;
import gov.aps.jca.dbr.DBR;
import org.apache.felix.ipojo.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class CADRecordImpl
 *
 * @author Nicolas A. Barriga
 *         Date: 3/24/11
 */
@Component
@Provides
public class CADRecordImpl implements CADRecord {
    private static final Logger LOG = Logger.getLogger(CADRecordImpl.class.getName());


    private CadState state = CadState.CLEAR;


    private static String[] letters = new String[]{"A", "B", "C", "D", "E"};
    private CommandSender cs;
    private SequenceCommand seqCom;
    private EpicsCad epicsCad;
    private ChannelAccessServer cas;

    private String prefix;
    private String name;
    private Integer numAttributes;
    private List<String> attributeNames = new ArrayList<String>();
    private CARRecord car;

    protected CADRecordImpl(@Requires ChannelAccessServer cas,
                            @Requires CommandSender cs,
                            @Property(name = "prefix", value = "INVALID", mandatory = true) String prefix,
                            @Property(name = "name", value = "INVALID", mandatory = true) String name,
                            @Property(name = "numAttributes", value = "0", mandatory = true) Integer numAttributes) {
        if (numAttributes > letters.length) {
            throw new IllegalArgumentException("Number of attributes must be less or equal than " + letters.length);
        }
        this.cs = cs;
        this.numAttributes = numAttributes;
        this.cas = cas;
        this.prefix = prefix.toLowerCase();
        this.name = name.toLowerCase();
        seqCom = SequenceCommand.valueOf(name.toUpperCase());
        for (int i = 0; i < numAttributes; i++) {
            attributeNames.add(letters[i]);
        }
        epicsCad = new EpicsCad();
        car = new CARRecord(cas, prefix.toLowerCase() + ":" + name.toLowerCase() + "C");
        LOG.info("Constructor");
    }

    @Validate
    public synchronized void start() {
        LOG.info("Validate");

        epicsCad.start(cas, prefix, name, new AttributeListener(), new DirListener(), attributeNames);
        car.start();
    }

    @Invalidate
    public synchronized void stop() {
        LOG.info("InValidate");
        epicsCad.stop(cas);
        car.stop();

    }

    @Override
    public EpicsCad getEpicsCad() {
        return epicsCad;
    }

    @Override
    public CARRecord getCAR() {
        return car;
    }

    private synchronized CadState processDir(Dir dir) {
        LOG.info("State: " + state + " Directive: " + dir);
        CadState newState = state.processDir(dir, epicsCad, cs, seqCom, car);
        LOG.info("State: " + newState);
        return newState;
    }

    private class AttributeListener implements ChannelListener {
        @Override
        public void valueChange(DBR dbr) {
            LOG.info("Attribute Received: " + ((String[]) dbr.getValue())[0]);
            state = processDir(Dir.MARK);
        }
    }

    private class DirListener implements ChannelListener {
        @Override
        public void valueChange(DBR dbr) {
            state = processDir(Dir.values()[((short[]) dbr.getValue())[0]]);
        }
    }

    CadState getState() {
        return state;
    }

}
