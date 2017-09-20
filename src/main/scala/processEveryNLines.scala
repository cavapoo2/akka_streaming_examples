package com.andyr
import java.nio.file.{Paths,Files}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import scala.io.Source._
class Sum3Lines(grpSize:Int) {

  implicit val actorSystem = ActorSystem("FilterLogFiles1")
  implicit val actorMaterializer = ActorMaterializer()

  val logfiles = List(
    "src/main/resources/sums.txt")

  def toInteger(in:String):Option[Int] = {
    try {
      Some(in.toInt)
    } catch {
      case e: NumberFormatException => None
    }
  }

  def processLines(files:List[String]): Unit = {
    Source(logfiles)
      .filter(s => {
        val p = Paths.get(s)
        Files.exists(p) && Files.size(p) > 0
      })
      .map(fromFile(_).getLines().toList.map(s => toInteger(s).getOrElse(0)).grouped(grpSize))
      .fold(List.empty[Int])((a,b) => a ++ b.map(_.sum)) //b is the List, a is the iterator
      .map(_.mkString("\n"))
      .runWith(Sink.foreach(println(_)))
  }
}

object Sum3Lines{
  def apply(v:Int): Sum3Lines = new Sum3Lines(v)
}
object Test extends App {
  val s = Sum3Lines(3)
  s.processLines(s.logfiles)
}