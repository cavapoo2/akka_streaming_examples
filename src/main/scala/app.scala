package com.andyr
import java.nio.file.{Paths,Files}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import scala.io.Source._
object FilterLogFiles extends App {

  implicit val actorSystem = ActorSystem("FilterLogFiles")
  implicit val actorMaterializer = ActorMaterializer()

  val logfiles = List(
    "src/main/resources/logfile1.log",
    "src/main/resources/logfilebad.log",
    "src/main/resources/logfile2.log",
    "src/main/resources/empty.log",
    "src/main/resources/logfile3.log",
    "src/main/resources/logfile4.log")

  //just a simple lower case, but could be something more complex
 def action(in:String):String = {
   in.toLowerCase()
 }

  def processFiles(files:List[String]): Unit = {
    Source(logfiles)
      .filter(s => {
        val p = Paths.get(s)
        Files.exists(p) && Files.size(p) > 0
      })
      .map(fromFile(_).getLines().toList.map(action(_)))
      .map(_.mkString("\n"))
      .runWith(Sink.foreach(println(_)))
  }
  processFiles(logfiles)
}
