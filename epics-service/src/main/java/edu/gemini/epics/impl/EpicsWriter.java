package edu.gemini.epics.impl;

import edu.gemini.epics.IEpicsWriter;
import edu.gemini.epics.EpicsException;
import gov.aps.jca.*;

/**
 * Implementation of the EpicsWritter interface using JCA
 */
public class EpicsWriter extends EpicsBase implements IEpicsWriter {

    public EpicsWriter(Context ctx) throws CAException {
        super(ctx);
    }

    public void write(String channelName, Double value) throws EpicsException {
        Channel channel = getChannel(channelName);
        if (channel == null) {
            return;
        }

        try {
            channel.put(value);
            channel.getContext().flushIO();
        } catch (CAException e) {
            throw new EpicsException("Problem writting to channel " + channelName, e);
        }
    }

    public void write(String channelName, Double[] value) throws EpicsException {

        double val[] = new double[value.length];
        for (int i = 0; i < val.length; i++) {
            val[i] = value[i];
        }
        write(channelName, val);
    }

    public void write(String channelName, double[] value) throws EpicsException {
        Channel channel = getChannel(channelName);

        if (channel == null) {
            return;
        }
        try {
            channel.put(value);
            channel.getContext().flushIO();
        } catch (CAException e) {
            throw new EpicsException("Problem writting to channel " + channelName, e);
        }
    }
}
