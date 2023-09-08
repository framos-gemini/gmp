package edu.gemini.aspen.gmp.tcsoffset.model;

import edu.gemini.aspen.giapi.offset.OffsetType;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

import javax.jms.Destination;

/**
 * A simple interface to apply the offset to TCS ioc.
 */
public interface TcsOffsetIOC {

    /**
     * Get the TCS Context as an array of doubles
     *
     * @return an array of double values with the TCS context, or
     *         <code>an empty array</code> if the context cannot be read
     * @throws TcsOffsetException in case there is an exception
     *                             trying to get the TCS Context.
     */

    void setTcsOffset(double p, double q,
                      OffsetType typeOffset) throws CAException, TimeoutException, TcsOffsetException;

}
