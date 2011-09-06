package edu.gemini.aspen.gds.web.ui.logs.model

import org.junit.Assert._
import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.ISODateTimeFormat
import com.google.common.collect.Maps
import org.specs2.mock.Mockito
import edu.gemini.aspen.gds.web.ui.logs.LogSource
import org.ops4j.pax.logging.spi.PaxLevel
import org.apache.log4j.Level
import scala.collection.JavaConversions._
import org.junit.{Ignore, Test}
import org.ops4j.pax.logging.service.internal.PaxLevelImpl
import com.vaadin.data.util.filter.{And, Compare}

class LoggingEventBeanQueryTest extends Mockito {
  val logSource = mock[LogSource]
  val definition = new LogSourceQueryDefinition(logSource, false, 50)

  definition.addProperty("timeStamp", classOf[java.lang.Long], 0L, true, true)
  definition.addProperty("level", classOf[String], "", true, true)
  definition.addProperty("loggerName", classOf[String], "", true, true)
  definition.addProperty("message", classOf[String], "", true, true)

  @Test
  def loadBeansEmpty {
    logSource.logEvents returns Nil

    val loader = new LoggingEventBeanQuery(definition, Maps.newHashMap(), Array(), Array())
    assertTrue(loader.loadBeans(0, 0).isEmpty)
  }

  @Test
  def loadBeansExactCount {
    val logEvents = List(buildEvent)
    logSource.logEvents returns logEvents

    val loader = new LoggingEventBeanQuery(definition, Maps.newHashMap(), Array(), Array())
    assertEquals(1, loader.loadBeans(0, 1).size)
  }

  def buildEvent(index: Long, level:Level): LogEventWrapper =
    new LogEventWrapper(new PaxLevelImpl(level), index, "message" + index, "logger", Array())

  def buildEvent(index: Long): LogEventWrapper = buildEvent(index, Level.ERROR)

  def buildEvent: LogEventWrapper = buildEvent(0)

  @Test
  def loadRequestTooBig {
    val logEvents = for (i <- 1 to 10) yield buildEvent
    logSource.logEvents returns logEvents

    val loader = new LoggingEventBeanQuery(definition, Maps.newHashMap(), Array(), Array())
    assertEquals(10, loader.loadBeans(0, 10).size)
  }

  @Test
  def shiftedRequestTooBig {
    val logEvents = for (i <- 1 to 10) yield buildEvent(i)
    logSource.logEvents returns logEvents

    val loader = new LoggingEventBeanQuery(definition, Maps.newHashMap(), Array(), Array())
    assertEquals(8, loader.loadBeans(2, 8).size)
    assertEquals(9, loader.loadBeans(2, 20).size)
  }

  @Test
  def loadSorted {
    val logEvents = for (i <- 0 to 10) yield buildEvent(i)
    logSource.logEvents returns logEvents.reverse

    val loader = new LoggingEventBeanQuery(definition, Maps.newHashMap(), Array("timeStamp"), Array(true))
    val logs = loader.loadBeans(0, 11)

    for (i <- 0 to 10) {
      assertEquals(LoggingEventBeanQuery.formatTimeStamp(i), logs(i).timeStamp)
    }
  }

  @Test
  def loadSortedReversed {
    val logEvents = for (i <- 0 to 10) yield buildEvent(i)
    logSource.logEvents returns logEvents.reverse

    val loader = new LoggingEventBeanQuery(definition, Maps.newHashMap(), Array("timeStamp"), Array(false))
    val logs = loader.loadBeans(0, 11)

    for (i <- 10 to 0) {
      assertEquals(LoggingEventBeanQuery.formatTimeStamp(i), logs(i).timeStamp)
    }
  } 

  @Test
  def loadWithTwoFilters {
    val logEvents = for (i <- 0 to 10) yield buildEvent(i)
    logSource.logEvents returns logEvents

    // Add a filter
    definition.addContainerFilter(new And(new Compare.Equal("level", "DEBUG"), new Compare.Equal("logger", "unknown")))
    val loader = new LoggingEventBeanQuery(definition, Maps.newHashMap(), Array("timeStamp"), Array(false))
    val logs = loader.loadBeans(0, 20)
    assertTrue(logs.isEmpty)
  }

  @Test
  def loadWithFilter {
    // Add two level of logs
    val debugLogEvents = (for (i <- 0 to 10) yield buildEvent(i, Level.DEBUG)) toList
    val errorLogEvents = (for (i <- 0 to 10) yield buildEvent(i, Level.ERROR)) toList
    val logEvents = debugLogEvents ::: errorLogEvents
    logSource.logEvents returns logEvents

    // Add a filter
    definition.addContainerFilter(new Compare.Equal("level", "DEBUG"))
    val loader = new LoggingEventBeanQuery(definition, Maps.newHashMap(), Array("timeStamp"), Array(false))
    val logs = loader.loadBeans(0, 20)
    assertEquals(11, logs.size)
  }

  @Test
  def testFormatTimeStamp {
    val dt = new DateTime()
    val formatted = LoggingEventBeanQuery.formatTimeStamp(dt.getMillis)
    val formatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC)
    assertEquals(formatter.print(dt), formatted)
  }

  @Test
  def testFormatLoggerName {
    assertEquals("log", LoggingEventBeanQuery.formatLoggerName("org.osgi.log"))
    assertEquals("GDSApp", LoggingEventBeanQuery.formatLoggerName("edu.gemini.aspen.gds.GDSApp"))
    assertEquals("", LoggingEventBeanQuery.formatLoggerName("edu.gemini.aspen.gds."))
  }

  @Test
  def testFormatMessage {
    assertEquals("message", LoggingEventBeanQuery.formatMessage("message"))
    val longMessage = for (i <- 0 to 200) yield i
    assertTrue(LoggingEventBeanQuery.formatMessage(longMessage.mkString).endsWith("..."))
  }
}