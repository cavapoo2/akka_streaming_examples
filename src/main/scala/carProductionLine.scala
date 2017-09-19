package com.andyr
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, FlowShape}
import akka.stream.scaladsl.{Balance, Flow, GraphDSL, Merge, Sink, Source}

import scala.util.Random

trait CarManufactureLine extends App{

  implicit val sys = ActorSystem("AssemblyLine")
  implicit val mat = ActorMaterializer()

  case class CarConfig(id:Int,engineId:Int,wheelsId:Int,chassisId: Int, colorId:Int, tested:Boolean=false)

  //Chassis -> Engine -> Wheels -> Paint -> Testing
  val carConfigs = (1 to 5).map(i => CarConfig(i.toInt,Random.nextInt(3),Random.nextInt(3),Random.nextInt(2),Random.nextInt(8)))

  def stage(stage:String,maxtimeseconds:Int, mintimeseconds:Int)  = Flow[CarConfig].map(car => {
    val r = Random.nextInt(maxtimeseconds+1)
    val timemilli = if (r > mintimeseconds) r*1000 else mintimeseconds*1000
    println(s"car ${car.id} at $stage will take $timemilli millseconds")
    Thread.sleep(timemilli)
    car
  })
  def chassisStage = stage("Chassis",6,5)
  def engineStage = stage("Engine",2,1)
  def wheelsStage = stage("Wheels",3,2)
  def paintStage = stage("Paint", 2,1)
  def testingStage = stage("Testing", 2,1).map(c => c.copy(tested = true))


  val parallelAssemblyLine = Flow.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val dispatch = builder.add(Balance[CarConfig](3))
    val merge = builder.add(Merge[CarConfig](3))

    dispatch.out(0) ~> chassisStage.async ~> engineStage.async ~> wheelsStage.async ~> paintStage.async ~> testingStage.async ~> merge.in(0)
    dispatch.out(1) ~> chassisStage.async ~> engineStage.async ~> wheelsStage.async ~> paintStage.async ~> testingStage.async ~> merge.in(1)
    dispatch.out(2) ~> chassisStage.async ~> engineStage.async ~> wheelsStage.async ~> paintStage.async ~> testingStage.async ~> merge.in(2)

    FlowShape(dispatch.in, merge.out)
  })

  def runGraph(flow: Flow[CarConfig, CarConfig, NotUsed]) = Source(carConfigs).via(flow).to(Sink.foreach(println)).run()
}
//Chassis -> Engine -> Wheels -> Paint -> Testing
object SynchronousCarAssembly extends CarManufactureLine {
  runGraph(Flow[CarConfig].via(chassisStage).via(engineStage).via(wheelsStage).via(paintStage).via(testingStage))
}

object AsynchronousCarAssembly extends CarManufactureLine {
  runGraph(Flow[CarConfig].via(chassisStage.async).via(engineStage.async).via(wheelsStage.async).via(paintStage.async).via(testingStage.async))
}

object ParallelizingCarAssembly extends CarManufactureLine {
  runGraph(Flow[CarConfig].via(parallelAssemblyLine))
}
