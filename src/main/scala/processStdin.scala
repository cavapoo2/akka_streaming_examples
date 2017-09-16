package com.andyr
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import scala.io.StdIn.readLine
object ProcessStdin {

  implicit val actorSystem = ActorSystem("ProcessStdin")
  implicit val actorMaterializer = ActorMaterializer()

  //just a simple lower case, but could be something more complex
  def action(in:String):String = {
    in.toLowerCase()
  }

  def process(in:String): Unit = {
    Source(in)
      .fold(""){case (v,acc) => v + acc}
      .map(action)
      .runWith(Sink.foreach(println(_)))
  }
  def main(args:Array[String]) {
    var ok = true
    while (ok) {
      val ln = readLine()
      ok = ln != null
      if (ok) {
        process(ln)
      }
    }
  }
}