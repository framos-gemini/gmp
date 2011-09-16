package edu.gemini.aspen.gds.staticheaderreceiver

import edu.gemini.aspen.gds.api.Conversions._
import scala.collection.JavaConversions._
import java.util.logging.{Level, Logger}

case class IntKeyword(keyword: String, value: Int)

/**
 * XMLRPC server, forwards calls to a singleton actor, needed because this class
 * is instantiated by the XMLRPC library, so we cannot pass parameters to it.
 */
class XmlRpcReceiver {
  protected val LOG = Logger.getLogger(this.getClass.getName)

  def initObservation(programId: String, dataLabel: String) {
    RequestHandler ! InitObservation(programId, dataLabel)
  }

  def storeKeyword(dataLabel: String, keyword: String, value: String) {
    RequestHandler ! StoreKeyword(dataLabel, keyword, value)
  }

  def storeKeyword(dataLabel: String, keyword: String, value: Double) {
    RequestHandler ! StoreKeyword(dataLabel, keyword, value.asInstanceOf[AnyRef])
  }

  def storeKeyword(dataLabel: String, keyword: String, value: Int) {
    RequestHandler ! StoreKeyword(dataLabel, keyword, value.asInstanceOf[AnyRef])
  }

  def storeKeywords(dataLabel: String, keywords: Array[Object]) {
    for (keyword <- keywords) {
      val pieces = keyword.asInstanceOf[String].split(",")
      val key = pieces(0).trim()
      val dataType = pieces(1).trim()
      val value = pieces(2).trim()
      try {
        dataType match {
          case "INT" => RequestHandler ! StoreKeyword(dataLabel, key, value.toInt.asInstanceOf[AnyRef])
          case "DOUBLE" => RequestHandler ! StoreKeyword(dataLabel, key, value.toDouble.asInstanceOf[AnyRef])
          case "STRING" => RequestHandler ! StoreKeyword(dataLabel, key, value.toString.asInstanceOf[AnyRef])
          case x => LOG.severe("Wrong data type: " + x)
        }
      } catch {
        case ex: java.lang.NumberFormatException => LOG.log(Level.SEVERE, ex.getMessage, ex)
      }
    }
  }
}

