package com.andyr
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import scala.util.Random

object TweetStuff extends App {
  implicit val system = ActorSystem("reactive-tweets")
  implicit val materializer = ActorMaterializer()

  final case class Author(handle: String)
  final case class Hashtag(name: String)

  final case class Tweet(author: Author, timestamp: Long, body: String) {
    def hashtags: Set[Hashtag] = body.split(" ").collect {
      //\\w is match word characters. ^#\\w is match anything that is not # or w
      case t if t.startsWith("#") => Hashtag(t.replaceAll("[^#\\w]", ""))
    }.toSet
  }
  def createBody:String = {
    if (Random.nextBoolean())
      "#akka" + Random.nextString(10)
    else
      Random.nextString(10)
  }
  val akkaTag = Hashtag("#akka")
  val tweets: Source[Tweet, NotUsed] = Source((1 to 10).map(_ => Tweet(Author(Random.nextString(4)),Random.nextLong,createBody)))
  val authors: Source[Author, NotUsed] =
    tweets
      .filter(_.hashtags.contains(akkaTag))
      .map(_.author)
  val hashtags: Source[Hashtag, NotUsed] = tweets.mapConcat(_.hashtags.toList)

  authors.runForeach(x => println(s"Au $x"))
  hashtags.runForeach(x => println(s"fe: $x"))

}