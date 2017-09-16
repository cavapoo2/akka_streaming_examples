package com.andyr
import akka.stream.scaladsl._
import org.scalatest.Matchers._
import akka.stream.{ClosedShape, UniformFanInShape,ActorMaterializer}
import scala.concurrent.duration._
import scala.concurrent.{Await,Future}
import akka.actor.ActorSystem
object maxOfThree extends App {
  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  val pickMaxOfThree = GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val zip1 = b.add(ZipWith[Int, Int, Int](math.max _))
    val zip2 = b.add(ZipWith[Int, Int, Int](math.max _))
    zip1.out ~> zip2.in0

    UniformFanInShape(zip2.out, zip1.in0, zip1.in1, zip2.in1)
  }

  val resultSink = Sink.head[Int]

  val g = RunnableGraph.fromGraph(GraphDSL.create(resultSink) { implicit b =>
    sink =>
      import GraphDSL.Implicits._

      // importing the partial graph will return its shape (inlets & outlets)
      val pm3 = b.add(pickMaxOfThree)

      Source.single(1) ~> pm3.in(0)
      Source.single(2) ~> pm3.in(1)
      Source.single(3) ~> pm3.in(2)
      pm3.out ~> sink.in
      ClosedShape
  })
  val max: Future[Int] = g.run()
  Await.result(max, 300.millis) should equal(3) // note exception will be thrown here if not true
}
