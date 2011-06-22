package edu.gemini.aspen.gds.api

import java.util.logging.Logger

/**
 * Abstract class extending a KeywordValueActor adding some useful methods
 *
 * Actors reading from different sources can extend this class to simplify building those actors
 */
abstract class OneItemKeywordValueActor(private val config: GDSConfiguration) extends KeywordValueActor {
    protected val LOG = Logger.getLogger(this.getClass.getName)

    // Add some inner values to simplify code in the implementations
    protected val fitsKeyword = config.keyword
    protected val isMandatory = config.isMandatory
    protected val fitsComment = config.fitsComment.value
    protected val headerIndex = config.index.index
    protected val sourceChannel = config.channel.name
    protected val defaultValue = config.nullValue.value
    protected val dataType = config.dataType
    protected val arrayIndex = config.arrayIndex.value

    /**
     * Method to get the default value of the value or None if the value is mandatory
     *
     * @return An Option containing the default value or None if the Item is mandatory
     */
    protected def defaultCollectedValue: Option[CollectedValue[_]] = if (isMandatory) {
        Option(ErrorCollectedValue(fitsKeyword, CollectionError.MandatoryRequired, fitsComment, headerIndex))
    } else {
        Option(DefaultCollectedValue(fitsKeyword, defaultValue, fitsComment, headerIndex))
    }

    /**
     * Method to convert a value read from a given source to the type requested in the configuration
     *
     */
    protected def valueToCollectedValue(value: Any): CollectedValue[_] = dataType match {
        // Anything can be converted to a string
        case DataType("STRING") => CollectedValue(fitsKeyword, value.toString, fitsComment, headerIndex)
        // Any number can be converted to a double
        case DataType("DOUBLE") => newDoubleCollectedValue(value)
        // Any number can be converted to a int
        case DataType("INT") => newIntCollectedValue(value)
        // this should not happen
        case _ => newMismatchError
    }

    private def newMismatchError = ErrorCollectedValue(fitsKeyword, CollectionError.TypeMismatch, fitsComment, headerIndex)

    private def newIntCollectedValue(value: Any) = value match {
        case x: java.lang.Long => {
            LOG.warning("Possible loss of precision converting " + x + " to integer" )
            collectedValueFromInt(x.intValue)
        }
        case x: java.lang.Integer => collectedValueFromInt(x.intValue)
        case x: java.lang.Short => collectedValueFromInt(x.intValue)
        case x: java.lang.Byte => collectedValueFromInt(x.intValue)
        case _ => newMismatchError
    }

    private def collectedValueFromInt(value: Int) = CollectedValue(fitsKeyword, value, fitsComment, headerIndex)

    private def newDoubleCollectedValue(value: Any) = value match {
        case x: java.lang.Number => CollectedValue(fitsKeyword, x.doubleValue, fitsComment, headerIndex)
        case _ => newMismatchError
    }

}