package edu.gemini.aspen.gmp.tcsoffset.model;

/**
 * TCS Context exception
 */
public class TcsOffsetException extends Exception {

    public enum Error {
        TIMEOUT,
        BINDINGCHANNEL,
        TCS_NOT_INPOS,
        OUT_OF_LIMIT,
        ERROR_CONFIGURATION_FILE,
        TCS_ERROR_STATE
    }



    public TcsOffsetException(Error e, String  msg, Exception cause) {
        super(String.format("ERROR CODE %s. ", e).concat(msg), cause);
    }

    public TcsOffsetException(Error e, String  msg) {
        super(String.format("ERROR CODE %s. ", e).concat(msg));
    }
    public TcsOffsetException(String message, Exception cause) {
        super(message, cause);
    }

    public TcsOffsetException(String message) {
        super(message);
    }

}
