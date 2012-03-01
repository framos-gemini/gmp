package edu.gemini.aspen.gds.staticheaderreceiver


import org.scalatest.junit.AssertionsForJUnit
import edu.gemini.aspen.gds.api.Conversions._
import org.junit.{Before, Test}
import edu.gemini.aspen.gds.keywords.database.impl.ProgramIdDatabaseImpl
import edu.gemini.aspen.gds.staticheaderreceiver.TemporarySeqexecKeywordsDatabaseImpl.{Store, Retrieve, Clean}
import scala.Some
import edu.gemini.aspen.gds.keywords.database.RetrieveProgramId

class RequestHandlerTest extends AssertionsForJUnit {
  val db = new TemporarySeqexecKeywordsDatabaseImpl
  val pdb = new ProgramIdDatabaseImpl
  val requestHandler = new RequestHandler(db, pdb)

  @Before
  def setup() {
    requestHandler.start()
  }

  @Test
  def testDB() {
    db ! Store("label", "KEY", 1.asInstanceOf[AnyRef])
    (db !? (1000, Retrieve("label", "KEY"))) match {
      case Some(Some(1)) =>
      case _ => fail()
    }
    (db !? (1000, Retrieve("wronglabel", "KEY"))) match {
      case Some(None) =>
      case _ => fail()
    }
    (db !? (1000, Retrieve("label", "WRONGKEY"))) match {
      case Some(None) =>
      case _ => fail()
    }
    db ! Clean("label")
    (db !? (1000, Retrieve("label", "KEY"))) match {
      case Some(None) =>
      case _ => fail()
    }
    db ! Clean("wronglabel")
  }

  @Test
  def testRequestHandler() {
    requestHandler ! StoreKeyword("label", "KEY", 1.asInstanceOf[AnyRef])
    Thread.sleep(100) //allow for messages to arrive
    (db !? (1000, Retrieve("label", "KEY"))) match {
      case Some(Some(1)) =>
      case _ => fail()
    }

    requestHandler ! InitObservation("programId", "label")
    Thread.sleep(100)
    (pdb !? (1000, RetrieveProgramId("label"))) match {
      case Some(Some("programId")) =>
      case _ => fail()
    }
  }

  @Test
  def testRequestHandlerStop() {
    requestHandler ! ExitRequestHandler()
    requestHandler ! StoreKeyword("label", "KEY", 1.asInstanceOf[AnyRef])
    Thread.sleep(100) //allow for messages to arrive
    // There should be no KEYs in the db
    (db !? (1000, Retrieve("label", "KEY"))) match {
      case Some(Some(1)) => fail()
      case _ =>
    }

  }

  @Test
  def testXmlRpcReceiver() {
    val xml = new XmlRpcReceiver(requestHandler)
    xml.storeKeyword("label", "KEY", 1)
    xml.storeKeywords("label2", ("KEY,INT,1" :: "KEY2,DOUBLE,1.0" :: "KEY3,STRING,uno" :: Nil).toArray)
    xml.initObservation("id", "label")
    Thread.sleep(100) //allow for messages to arrive
    (db !? (1000, Retrieve("label", "KEY"))) match {
      case Some(Some(1)) =>
      case _ => fail()
    }
    (db !? (1000, Retrieve("label2", "KEY"))) match {
      case Some(Some(1)) =>
      case _ => fail()
    }
    (db !? (1000, Retrieve("label2", "KEY2"))) match {
      case Some(Some(1.0)) =>
      case _ => fail()
    }
    (db !? (1000, Retrieve("label2", "KEY3"))) match {
      case Some(Some("uno")) =>
      case _ => fail()
    }
    (pdb !? (1000, RetrieveProgramId("label"))) match {
      case Some(Some("id")) =>
      case _ => fail()
    }
  }

}