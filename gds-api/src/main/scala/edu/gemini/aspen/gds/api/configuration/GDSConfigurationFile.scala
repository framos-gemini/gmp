package edu.gemini.aspen.gds.api.configuration

import edu.gemini.aspen.gds.api.GDSConfiguration
import java.io.{FileWriter, BufferedWriter, File}
import edu.gemini.aspen.gds.api.Predef._

/**
 * This object provides utility methods to manipulate a configuration file
 */
object GDSConfigurationFile {
  def getConfiguration(configurationFile: String): List[GDSConfiguration] = {
    getConfiguration(getFullConfiguration(configurationFile))
  }

  def getConfiguration(contents: List[ConfigItem[_]]): List[GDSConfiguration] = {
    contents filter {
      _.isInstanceOf[ConfigItem[_]]
    } map {
      _.value
    } filter {
      _.isInstanceOf[GDSConfiguration]
    } map {
      _.asInstanceOf[GDSConfiguration]
    }
  }

  def getFullConfiguration(configurationFile: String): List[ConfigItem[_]] = {
    var results = new GDSConfigurationParser().parseFileRawResult(configurationFile).get
    if (results.last.isEmpty) {
      results = results.reverse.tail.reverse
    }

    results map {
      case Some(x: GDSConfiguration) => new ConfigItem(x)
      case Some(x: Comment) => new ConfigItem(x)
      case None => new ConfigItem(new BlankLine())
    }
  }

  def saveConfiguration(configurationFile: String, contents: List[ConfigItem[_]]) {
    val newFile = new File(configurationFile)
    use(new BufferedWriter(new FileWriter(newFile))) {
      writer: BufferedWriter => for (configLine <- contents) {
        Some(configLine) map {
          case x => writer.write(x._type.ConfigTypeToString(x))
        }
        writer.newLine()
      }
    }

  }

}