package com.andyr
import akka.{Done}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import akka.actor.ActorSystem
//note this does not form a tuple, it just takes from each source
object AnotherCombine2Sources extends App {
  implicit val sys = ActorSystem("Combiner")
  implicit val mat = ActorMaterializer()
  val sourceOne = Source(1 to 1000).filter(x => (x % 2 == 0))
  val sourceTwo = Source(1 to 1000).filter(x => (x % 2 != 0))
  val merged = Source.combine(sourceOne, sourceTwo)(Merge(_))
  val mergedResult: Future[Done] = merged.runWith(Sink.foreach(println(_)))
  Await.result(mergedResult,1000.millis)
}