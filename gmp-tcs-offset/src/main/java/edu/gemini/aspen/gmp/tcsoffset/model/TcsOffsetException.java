package edu.gemini.aspen.gmp.tcsoffset.model;

/**
 * TCS Context exception
 */
public class TcsOffsetException extends Exception {

    private Error _errorType;
    public enum Error {
        TIMEOUT,
        BINDINGCHANNEL,
        TCS_NOT_INPOS,
        OUT_OF_LIMIT,
        CONFIGURATION_FILE,
        TCS_STATE,
        TCS_WAS_REBOOTED,
        READING_JMS_MESSAGE
    }

    public TcsOffsetException(Error errorType, String  msg, Exception cause) {
        super(String.format("ERROR CODE %s. ", errorType).concat(msg), cause);
        _errorType = errorType;
    }

    public TcsOffsetException(Error errorType, String  msg) {
        super(String.format("ERROR CODE %s. ", errorType).concat(msg));
        _errorType = errorType;
    }

    public Error getTypeError() {
        return _errorType;
    }

}