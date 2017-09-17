package com.andyr
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, MergePreferred, Source}
import akka.stream.{ActorMaterializer, SourceShape}
import scala.concurrent.ExecutionContext.Implicits.global

object FibApp {

  case class Fib(value: Int, previous: Int) {
    def next() = Fib(value + previous, value)
  }

  object Fib {
    val one = Fib(1, 1)

    val source: Source[Fib, NotUsed] = Source.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val init = Source.single(Fib.one) // only once

      val nextval = Flow[Fib]
        .map(_.next())

      val bcast = builder.add(Broadcast[Fib](2))
      val merge = builder.add(MergePreferred[Fib](1)) // the number of secondary in ports

      init ~> merge  ~>  bcast
      bcast ~> nextval ~> merge.preferred

      SourceShape(bcast.out(1)) // note this is why bcast has 2 out ports
    })
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("FibSys")
    implicit val materializer = ActorMaterializer()

    Fib.source
      .takeWhile(_.value < 1000)
      .runForeach(x => println(x.value))
      .onComplete(_ => system.terminate())
  }
}