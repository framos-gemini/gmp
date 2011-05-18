package edu.gemini.aspen.gds.odb

import org.apache.felix.ipojo.annotations._
import edu.gemini.aspen.giapi.data.DataLabel
import java.util.logging.Logger
import edu.gemini.pot.spdb.IDBDatabaseService
import edu.gemini.aspen.gds.api.{KeywordValueActor, KeywordActorsFactory, GDSConfiguration}
import edu.gemini.aspen.gds.keywords.database.{RetrieveProgramId, ProgramIdDatabase}

/**
 * Factory of Actors that can retrieve values from the ODB
 *
 * Whereas other actor factories use the approach of one actor per keyword, the ODB
 * used one actor for all the keywords. This is to reduce the cost of ODB calls which
 * tend to be expensive
 */
@Component
@Instantiate
@Provides(specifications = Array(classOf[KeywordActorsFactory]))
class ODBActorsFactory(@Requires dbService: IDBDatabaseService, @Requires programIdDatabase:ProgramIdDatabase) extends KeywordActorsFactory {
    val LOG =  Logger.getLogger(classOf[ODBActorsFactory].getName)
    var actorsConfiguration: List[GDSConfiguration] = List()

    override def buildPrepareObservationActors(dataLabel: DataLabel): List[KeywordValueActor] ={
        val programID = programIdDatabase !? RetrieveProgramId(dataLabel)
        // Only produce actors if the programID has been already stored in the programIdDatabase
        programID match {
            case Some(id) if id.isInstanceOf[String] => buildODBActor(id.asInstanceOf[String])
                // TODO add log
            case None => List()
        }
    }

    def buildODBActor(programID:String)= {
        LOG.info("Building ODB Actor for program: " + programID)
        new ODBValuesActor(programID, dbService.getQueryRunner, actorsConfiguration) :: Nil
    }

    override def configure(configuration:List[GDSConfiguration]) {
        actorsConfiguration = configuration filter { _.subsystem.name == "ODB"}
    }

    @Validate
    def start() {
        LOG.info("ODBActorsFactory started")
    }
}