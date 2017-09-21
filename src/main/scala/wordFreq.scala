package com.andyr
import java.nio.file.{Paths,Files}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import scala.io.Source._
object WordFreq extends App {

  implicit val actorSystem = ActorSystem("WordFreq")
  implicit val actorMaterializer = ActorMaterializer()

  val file = List(
    "src/main/resources/story.txt")
  val MaxWords = 10000
  def processFile(files:List[String]): Unit = {
    Source(file)
      .filter(s => {
        val p = Paths.get(s)
        Files.exists(p) && Files.size(p) > 0
      })
      .map(fromFile(_).getLines().filterNot(x => x.forall(_.isWhitespace)).mkString(" ")) //remove any lines that are just whitespace,
      //separate each line by a space, otherwise last word on line and first word on next line can end up joined
      .mapConcat(_.split(" ").toList)  //split each line of strings by spaces, convert array to list, concat each element in list
      //to iterable of strings
      .map(_.toUpperCase) // map each string to uppercase
      .collect { case w =>
      w.replaceAll("""[?.!:,]\s*""", "")} //remove punctuation
      .groupBy(MaxWords,identity)
      .map(_ -> 1)  //map to (word,1) tuple
      .reduce((l,r) => (l._1,l._2 + r._2))
      .mergeSubstreams
      .runWith(Sink.foreach(println(_)))
  }
  processFile(file)
}