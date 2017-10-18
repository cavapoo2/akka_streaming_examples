package com.andyr
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Sink, Source}

object RunLength2 extends App {

  implicit val actorSystem = ActorSystem("RunLength")
  implicit val actorMaterializer = ActorMaterializer()

  def processSource(in:List[String],delim:String):Unit = {
    val x = Source(in).async.map(x => x.split("d").toList.map(_.length.toString + delim))
        .map(println(_))//map each char to string
      .runWith(Sink.ignore)
  }
  processSource(List("1111111111d11111d1d11d1111111d11d111d111111111d111d111d1111d111d","111d1d1d1d11111d"),"d")

}