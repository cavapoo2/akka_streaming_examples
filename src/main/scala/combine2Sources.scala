package com.andyr
import akka.{Done, NotUsed}
import akka.stream.scaladsl.{GraphDSL, Sink, Source}
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape, UniformFanInShape}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import akka.actor.ActorSystem
//its a bit OTT this , could also do something like this
//val result = Source(List(1,2,3,4,5,6)).zip(Source(Stream.continually(60))).runForeach(println) :)
object Combine2Sources extends App {
  implicit val sys = ActorSystem("Combiner")
  implicit val mat = ActorMaterializer()

  def repeatedSource(num:Int):Source[Int, NotUsed] = {
    Source.repeat(num)
  }
  def otherSource(in:List[Int]):Source[Int,NotUsed] = {
    Source(in)
  }
  val resultSink = {
    Sink.foreach[Any](println(_))
  }
  val zipSources = GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._
    val zip1 = b.add(Zip[Int,Int]())
    UniformFanInShape(zip1.out,zip1.in0,zip1.in1)
  }
  val g = RunnableGraph.fromGraph(GraphDSL.create(resultSink) { implicit b =>
    sink =>
    import GraphDSL.Implicits._
    val z = b.add(zipSources)
    val s1 = repeatedSource(60)
    val s2 = otherSource(List(1,2,3,4,5,6,7,8,9,10))
    s1 ~> z
    s2 ~> z
    z.out ~> sink.in
    ClosedShape
  })
  val max: Future[Done] = g.run()
  Await.result(max, 10000.millis)
}