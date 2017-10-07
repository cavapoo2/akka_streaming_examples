package com.andyr
import java.nio.file.{Path, Paths}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink, Source}
import akka.util.ByteString

//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success,Failure}
import scala.io.Source._
object AnotherProcFiles extends App {

  implicit val actorSystem = ActorSystem("Another")
  implicit val actorMaterializer = ActorMaterializer()
  import actorSystem.dispatcher



  def procFiles(f:List[String]):Unit = {
    Source(f)
      .map(fromFile(_).getLines())
      .map(_.mkString("\n"))
      .runWith(Sink.foreach(println(_)))
  }
  def doSomething(s:Iterator[String]):Future[String] = {
    //val x = Future(s.mkString("\n"))
     Future(
       s.mkString("\n") + Thread.currentThread().getName)
    /*
      x onComplete {
      case Success(h) => println(h)
      case Failure(e) => println(e)
    }*/

  }
  //this does not work as intended (no parallel since output is in order
  def procFiles3(f:List[String]):Unit = {
    Source(f)
      .map(fl => fromFile(fl).getLines())
        .mapAsync(5)(doSomething)
      //.map(fromFile(_).getLines())
      //   fromFile(f).getLines()
      // Future[Unit](v.mkString("\n")))
      //.map(_.mkString("\n"))
      .runWith(Sink.foreach(println(_)))
  }
  //also not good
  def procString(s:String):Unit = {
    Source(s.toList).mapAsync(5)(s => Future{s.toLower})
      .runWith(Sink.foreach(println(_)))
  }
  def writeBatchToDatabase(batch: Seq[Int]): Future[Unit] =
    Future {
      println(s"Writing batch of $batch to database by ${Thread.currentThread().getName}")
    }
def writeBatchToDatabase2(batch: Int): Future[Unit] =
    Future {
      println(s"Writing batch of $batch to database by ${Thread.currentThread().getName}")
    }
  def writeBatchToDatabase3[A](batch: A): Future[Unit] =
    Future {
      println(s"Writing batch of $batch to database by ${Thread.currentThread().getName}")
    }
 def testAsync {
   Source(1 to 1000000)
     .grouped(10)
     .mapAsync(10)(writeBatchToDatabase)
     .runWith(Sink.ignore)
 }
  def testAsync2 {
   Source(1 to 1000000).async
     .mapAsync(10)(writeBatchToDatabase2)
     .runWith(Sink.ignore)
 }
   def testAsync3 {
   Source(('A' to 'Z').toList).async
     .mapAsync(10)(writeBatchToDatabase3)
     .runWith(Sink.ignore)
 }
  def fileR(in:String): Future[Unit] = Future {
    val x = fromFile(in).getLines().mkString(".")
    println(s"$x by ${Thread.currentThread().getName}")
  }
  def testAsync4: Unit = {
    Source(List("src/main/resources/letters.txt","src/main/resources/numbers.txt")).async
      .mapAsync(2)(fileR)
        .runWith(Sink.ignore)
  }
  def procFiles2(f:List[String]) : Unit = {
    val lines = Flow[ByteString].via(Framing.delimiter(ByteString("\n"),10000,true))
      .map(_.utf8String)
    val readFile = Flow[Path].flatMapConcat{path => FileIO.fromPath(path).via(lines)}
    Source(f).map(Paths.get(_)).via(readFile).runWith(Sink.foreach(println(_)))
  }

  // procFiles(List("src/main/resources/another1.txt","src/main/resources/another2.txt"))
  //procFiles2(List("src/main/resources/another1.txt","src/main/resources/another2.txt"))
  //procFiles3(List("src/main/resources/numbers.txt"))
 // procString("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
  //testAsync
  //testAsync2
  //testAsync3
  testAsync4

}

