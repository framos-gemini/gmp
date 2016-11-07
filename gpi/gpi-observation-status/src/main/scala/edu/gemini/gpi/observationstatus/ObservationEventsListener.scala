package edu.gemini.gpi.observationstatus

import edu.gemini.aspen.giapi.status.setter.StatusSetter
import edu.gemini.aspen.gds.api._
import edu.gemini.gmp.top.Top
import edu.gemini.aspen.giapi.status.impl.BasicStatus
import edu.gemini.aspen.giapi.data.DataLabel
import edu.gemini.aspen.giapi.status.StatusDatabaseService
import edu.gemini.aspen.gds.api.GDSStartObservation
import edu.gemini.aspen.gds.api.GDSObservationError
import edu.gemini.aspen.gds.api.GDSEndObservation

import java.util.concurrent.TimeUnit._
import java.util.{Timer, TimerTask}
import java.util.concurrent.{Callable, TimeUnit}
import java.util.logging.Logger

import com.google.common.base.Stopwatch
import com.google.common.cache.{CacheBuilder, CacheLoader, RemovalListener, RemovalNotification}
import org.osgi.service.event.{Event, EventHandler}

import scalaz._
import Scalaz._

import scala.collection.JavaConversions._


/**
 * Intermediate class to convert GDS events into status items for GPI */
class ObservationEventsListener(gmpTop: Top, statusSetter: StatusSetter, statusDB: StatusDatabaseService) extends EventHandler {
  // expiration of 1 day by default but tests can override it
  def expirationMillis = 24 * 60 * 60 * 1000
  private val LOG = Logger.getLogger(classOf[ObservationEventsListener].getName)

  val readoutOverhead = 4
  val writeOverhead = 2
  val perCoaddOverhead = 2.7

  val massLastUpdate = gmpTop.buildStatusItemName("massLastUpdate")
  val dimmLastUpdate = gmpTop.buildStatusItemName("dimmLastUpdate")

  val massMinsSince = gmpTop.buildStatusItemName("massMinsSince")
  val dimmMinsSince = gmpTop.buildStatusItemName("dimmMinsSince")

  val coAddsStatus = gmpTop.buildStatusItemName("currentNumCoadds")
  val integrationTimeStatus = gmpTop.buildStatusItemName("currentIntegrationTime")
  val obsTimeStatus = gmpTop.buildStatusItemName("ifs:estimatedObservationTime")

  private val cache = CacheBuilder.newBuilder()
      .removalListener(TimerRemoval)
      .expireAfterWrite(expirationMillis, MILLISECONDS)
      .build[DataLabel, ObsTimerTask](new CacheLoader[DataLabel, ObsTimerTask]() {
        override def load(label: DataLabel) = ObsTimerTask()
      })

  val timer = new Timer()

  def invalidate() {
    cache.invalidateAll()
  }

  def gdsEvent(event: GDSNotification) {
    event match {
      case GDSStartObservation(dataLabel)     =>
        val coAdds = Option(statusDB.getStatusItem[Int](coAddsStatus)).map(_.getValue)
        val exposureTime = Option(statusDB.getStatusItem[Float](integrationTimeStatus)).map(_.getValue)
        (exposureTime |@| coAdds)((e, c) => (e + perCoaddOverhead) * c + readoutOverhead + writeOverhead).foreach { observationTime =>
          LOG.info(s"Exposure started with exposure time ${exposureTime.get} and coadds ${coAdds.get}")
          val estimatedTimeLeft = 1000 * observationTime
          LOG.info(s"Start counter to $estimatedTimeLeft")
          if (Option(cache.getIfPresent(dataLabel)).isEmpty) {
            val timerTask = cache.get(dataLabel, new Callable[ObsTimerTask] {
              override def call(): ObsTimerTask = {
                ObsTimerTask(estimatedTimeLeft.toLong)
              }
            })

            LOG.info(s"Set estimated observation time $estimatedTimeLeft on $obsTimeStatus")
            statusSetter.setStatusItem(new BasicStatus(obsTimeStatus, estimatedTimeLeft.toInt))
            timer.scheduleAtFixedRate(timerTask, 0, 200)
          }
        }
        LOG.info(s"Set observationDataLabel to $dataLabel")
        statusSetter.setStatusItem(new BasicStatus(gmpTop.buildStatusItemName("ifs:observationDataLabel"), dataLabel.getName))
        // Set the status item for time since last update of MASS/DIMM
        updateTimeSince(massLastUpdate, massMinsSince)
        updateTimeSince(dimmLastUpdate, dimmMinsSince)
      case GDSEndObservation(dataLabel, _, _) =>
        cache.get(dataLabel).endObservation()
        cache.invalidate(dataLabel)
      case GDSObservationError(dataLabel, _)  =>
        cache.get(dataLabel).endObservation()
        cache.invalidate(dataLabel)
      case _                                  => // Ignore
    }
  }

  /**
    * Updates a status item with minutes since a given value reading a timestamp
    */
  private def updateTimeSince(srcTimeStampStatus: String, destMinsSinceStatus: String) = {
    Option(statusDB.getStatusItem[Int](srcTimeStampStatus)).foreach { i =>
      Option(i.getValue).foreach { t=>
        val secs = System.currentTimeMillis() / 1000 - t
        statusSetter.setStatusItem(new BasicStatus[Int](destMinsSinceStatus, (secs / 60).toInt))
      }
    }
  }

  case class ObsTimerTask(observationTime: Double = 0) extends TimerTask{
    private val stopwatch = Stopwatch.createStarted()
    override def run() = {
      //
      val remainingTime = observationTime - stopwatch.elapsed(TimeUnit.MILLISECONDS)
      statusSetter.setStatusItem(new BasicStatus(gmpTop.buildStatusItemName("ifs:timeLeft"), Math.max(0, remainingTime / 1000)))
      statusSetter.setStatusItem(new BasicStatus(gmpTop.buildStatusItemName("ifs:timeLeftMS"), Math.max(0, remainingTime.toInt)))
      if (remainingTime <= 0 && stopwatch.isRunning) {
        cancel()
      }
    }

    def endObservation(): Unit = {
      if (stopwatch.isRunning) {
        stopwatch.stop()
      }
      cancel()
      statusSetter.setStatusItem(new BasicStatus(gmpTop.buildStatusItemName("ifs:timeLeft"), 0d))
      statusSetter.setStatusItem(new BasicStatus(gmpTop.buildStatusItemName("ifs:timeLeftMS"), 0))
    }

    def isRunning: Boolean = stopwatch.isRunning
  }

  case object TimerRemoval extends RemovalListener[DataLabel, ObsTimerTask] {
    override def onRemoval(removalNotification: RemovalNotification[DataLabel, ObsTimerTask]) {
      if (removalNotification.getValue.isRunning) {
        removalNotification.getValue.endObservation()
      }
    }
  }

  override def handleEvent(event: Event): Unit = {
    Option(event.getProperty("gdsevent")).foreach {
      case n: GDSNotification => gdsEvent(n)
      case _                  => //
    }
  }
}
