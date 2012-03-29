package edu.gemini.aspen.gds.performancemonitoring

import org.scala_tools.time.Imports._
import java.util.logging.Logger
import scala.{Some, Option}
import collection.mutable.{SynchronizedMap, HashMap}

//todo: Add javadoc to this class
/**
 * This event logger works by logging "event"'s starting and finishing times. The event are grouped in "sets".
 * For example, you can create a set for a datalabel, and log the starting and ending times of every observation event
 * received for said datalabel.
 *
 * You can then retrieve or log the data for a set, or average a certain event over all sets.
 */
class EventLogger[A, B] {
  //map: eventSet -> (event -> (startTime, endTime))
  private val map = new HashMap[A, collection.mutable.Map[B, (Option[DateTime], Option[DateTime])]] with SynchronizedMap[A, collection.mutable.Map[B, (Option[DateTime], Option[DateTime])]]

  def addEventSet(set: A) {
    map += set -> new HashMap[B, (Option[DateTime], Option[DateTime])] with SynchronizedMap[B, (Option[DateTime], Option[DateTime])]
  }

  def start(set: A, evt: B) {
    map.getOrElseUpdate(set, new HashMap[B, (Option[DateTime], Option[DateTime])] with SynchronizedMap[B, (Option[DateTime], Option[DateTime])]) += evt ->(Some(DateTime.now), map(set).getOrElse(evt, (None, None))._2)
  }

  def end(set: A, evt: B) {
    map.getOrElseUpdate(set, new HashMap[B, (Option[DateTime], Option[DateTime])] with SynchronizedMap[B, (Option[DateTime], Option[DateTime])]) += evt ->(map(set).getOrElse(evt, (None, None))._1, Some(DateTime.now))
  }

  def retrieve(set: A): scala.collection.Map[B, Option[Duration]] = {
    map.getOrElse(set, collection.mutable.Map.empty[B, (Option[DateTime], Option[DateTime])])
      .mapValues({
      case (Some(start), Some(end)) => Some((start to end).toDuration)
      case _ => None
    })
  }

  def retrieve(set: A, evt: B): Option[Duration] = {
    map.getOrElse(set, collection.mutable.Map.empty[B, (Option[DateTime], Option[DateTime])]).get(evt) flatMap {
      case (Some(start), Some(end)) => Some((start to end).toDuration)
      case _ => None
    }
  }

  def average(evt: B): Option[Duration] = {
    val values = for {
      (_, innerMap) <- map
      (_evt, times) <- innerMap
      if _evt == evt
    } yield times

    val durations = values.collect({
      case (Some(start), Some(end)) => (start to end).toDuration
    })

    case class Average(sum: Duration, count: Int) {
      def +(other: Duration): Average = {
        Average(sum + other, count + 1)
      }

      def average(): Option[Duration] = {
        count match {
          case 0 => None
          case x => Some(((sum.millis / x).toInt.millis).toDuration)
        }
      }
    }
    val avg = new Average(Duration.standardSeconds(0), 0)
    durations.foldLeft(avg) {
      (currentAvg, currentVal) => currentAvg + currentVal
    }.average()
  }

  def retrieveAll() = {
    map.mapValues({
      case m => m.mapValues({
        case (Some(start), Some(end)) => Some((start to end).toDuration)
        case _ => None
      })
    })
  }

  def check(set: A, evt: B, millis: Long): Boolean = retrieve(set, evt) match {
      case Some(x: Duration) => x.millis <= millis
      case _ => false
    }
}
