package com.andyr
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.io.Source._
object AnotherProcFiles extends App {

  implicit val actorSystem = ActorSystem("Another")
  implicit val actorMaterializer = ActorMaterializer()

  def procFiles(f:List[String]):Unit = {
    Source(f)
      .map(fromFile(_).getLines())
      .map(_.mkString("\n"))
      .runWith(Sink.foreach(println(_)))
  }
  procFiles(List("src/main/resources/another1.txt","src/main/resources/another2.txt"))
}

