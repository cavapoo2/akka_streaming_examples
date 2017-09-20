package com.andyr
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, FlowShape}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source,Broadcast}
import com.andyr.LotteryGen.{LuckyStars, MainNumbers, Ticket}
class LotteryChecker(numberOfTickets:Int) {

  implicit val sys = ActorSystem("LotteryChecker")
  implicit val mat = ActorMaterializer()

  case class Results(ticket: Ticket,matches: Int,winner:(MainNumbers,LuckyStars))

  val tickets = LotteryGen.genTickets(numberOfTickets)
  val winner = LotteryGen.draw

  def countMatches(a:Vector[Int],b:Vector[Int]):Int = {
    val m = a.filter(v => b.contains(v))
    m.size
  }

  def matchNOrMore(num:Int): Flow[Ticket, Ticket, NotUsed] = {
    Flow[Ticket].filter { t =>
      (countMatches(t.mainNumbers.nums, winner._1.nums) + countMatches(t.luckyStars.nums, winner._2.nums)) >= num
    }
  }
  def matchN(num:Int) = {
    Flow[Ticket].filter { t =>
      (countMatches(t.mainNumbers.nums, winner._1.nums) + countMatches(t.luckyStars.nums, winner._2.nums)) == num
    } .map(t => Results(t,num,(winner._1,winner._2)))
  }
  val lotteryCheckerGraph = Flow.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._
    //Flow
    val match2OrMore = matchNOrMore(2)
    val match3OrMore = matchNOrMore(3)
    val match4OrMore = matchNOrMore(4)
    val match5OrMore = matchNOrMore(5)
    val match6OrMore = matchNOrMore(6)
    val match2 = matchN(2)
    val match3 = matchN(3)
    val match4 = matchN(4)
    val match5 = matchN(5)
    val match6 = matchN(6)
    val match7 = matchN(7)

    //Junctions
    val broadCasta = builder.add(Broadcast[Ticket](2))
    val broadCastb = builder.add(Broadcast[Ticket](2))
    val broadCastc = builder.add(Broadcast[Ticket](2))
    val broadCastd = builder.add(Broadcast[Ticket](2))
    val broadCaste = builder.add(Broadcast[Ticket](2))
    val merge = builder.add(Merge[Results](6))

    //Graph
    broadCasta ~> match2 ~> merge
    broadCasta ~> match3OrMore ~> broadCastb
    broadCastb ~> match3 ~> merge
    broadCastb ~> match4OrMore ~> broadCastc
    broadCastc ~> match4 ~> merge
    broadCastc ~> match5OrMore ~> broadCastd
    broadCastd ~> match5 ~> merge
    broadCastd ~> match6OrMore ~> broadCaste
    broadCaste ~> match6 ~> merge
    broadCaste ~> match7 ~> merge

    FlowShape(broadCasta.in,merge.out)
  })

  def runGraph(flow: Flow[Ticket, Results, NotUsed]) = Source(tickets).via(flow).to(Sink.foreach(println)).run()

}
object LotteryChecker{
  def apply(m:Int) = new LotteryChecker(m)
}

object LotteryApp extends App {
  val lot = LotteryChecker(100)
  println(lot.winner)
  println("results")
  lot.runGraph(lot.lotteryCheckerGraph)

}
