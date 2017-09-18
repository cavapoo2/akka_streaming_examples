package com.andyr
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object TransformStages extends App {

  implicit val actorSystem = ActorSystem("CustomStages")
  implicit val actorMaterializer = ActorMaterializer()

  case class Start(data:Int, info:String )
  case class Middle(data:Int,info: String)
  case class Last(data:Int,info:String)

  val MaxGroups = 1000
  //data
  val x = (1 to 10).toVector
  val y = x zip ('A' to 'J')
  val z = y.map(v => Start(v._1,v._2.toString))
  //source
  val source = Source(z)
  //processing
  val doubleLower = Flow[Start].map(s => Middle(s.data*2,s.info.toLowerCase()))
  val minusAppend = Flow[Middle].map(m => Last(m.data - 1,m.info + m.info))
  //sink
  val sink = Sink.foreach((v:Last) => println(v))

  val stream = source
    .via(doubleLower)
    .via(minusAppend)
    .to(sink)

  stream.run()
}