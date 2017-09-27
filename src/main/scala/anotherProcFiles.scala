package com.andyr
import java.nio.file.{Path, Paths}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink, Source}
import akka.util.ByteString

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

  def procFiles2(f:List[String]) : Unit = {
    val lines = Flow[ByteString].via(Framing.delimiter(ByteString("\n"),10000,true))
        .map(_.utf8String)
    val readFile = Flow[Path].flatMapConcat{path => FileIO.fromPath(path).via(lines)}
    Source(f).map(Paths.get(_)).via(readFile).runWith(Sink.foreach(println(_)))
  }
  procFiles(List("src/main/resources/another1.txt","src/main/resources/another2.txt"))
  procFiles2(List("src/main/resources/another1.txt","src/main/resources/another2.txt"))

}

