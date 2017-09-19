package com.andyr
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, FlowShape}
import akka.stream.scaladsl.{Balance, Flow, GraphDSL, Merge, Sink, Source}
import com.andyr.LotteryGen.{LuckyStars, MainNumbers, Ticket}
class LotteryChecker(numberOfTickets:Int) {

  implicit val sys = ActorSystem("LotteryChecker")
  implicit val mat = ActorMaterializer()

  case class Results(ticket: Ticket,winner:(MainNumbers,LuckyStars))

  val tickets = LotteryGen.genTickets(numberOfTickets)
  val winner = LotteryGen.draw

  def countMatches(a:Vector[Int],b:Vector[Int]):Int = {
    val m = a.filter(v => b.contains(v))
    m.size
  }

  def matchNOrMore(num:Int) = {
    Flow[Ticket].filter { t =>
      (countMatches(t.mainNumbers.nums, winner._1.nums) + countMatches(t.luckyStars.nums, winner._2.nums)) >= num
    }
  }
  def matchN(num:Int) = {
    Flow[Ticket].filter { t =>
      (countMatches(t.mainNumbers.nums, winner._1.nums) + countMatches(t.luckyStars.nums, winner._2.nums)) == num
    }.map(t => Results(t,(winner._1,winner._2)))
  }
  def match2OrMore = matchNOrMore(2)

  def runGraph(flow: Flow[Ticket, Results, NotUsed]) = Source(tickets).via(flow).to(Sink.foreach(println)).run()

}
object LotteryChecker{
  def apply(m:Int) = new LotteryChecker(m)
}

object LotteryApp extends App {
  val lot = LotteryChecker(10)

}
