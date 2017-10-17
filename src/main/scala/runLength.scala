package com.andyr
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

object RunLength extends App {

  implicit val actorSystem = ActorSystem("RunLength")
  implicit val actorMaterializer = ActorMaterializer()

  class RunLength(delim:String) extends GraphStage[FlowShape[String, String]] {
    val in: Inlet[String] = Inlet("Incoming")
    val out: Outlet[String] = Outlet("Outgoing")
    override val shape: FlowShape[String, String] = FlowShape(in, out)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
      new GraphStageLogic(shape) {
        var store = Vector.empty[String]
        setHandler(in, new InHandler {
          override def onPush() = {
            val elem = grab(in)
            if (elem != delim)
            {
              store = store :+ elem
              pull(in)
            }
            else {
              push(out,store.length.toString + "d")
              store = store.drop(store.length)
            }
          }
        })
        setHandler(out, new OutHandler {
          override def onPull() = {
            pull(in)
          }
        })
      }
  }
  def processSource(in:String):Unit = {
    val x = Source(in).map(_.toString) //map each char to string
      x.via(new RunLength("d"))
      .runWith(Sink.foreach(println(_)))
  }
  processSource("1111111111d11111d1d11d1111111d11d111d111111111d111d111d1111d111d")

}