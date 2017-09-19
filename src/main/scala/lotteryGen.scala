package com.andyr
import scala.util.Random
object LotteryGen {
  //constants
  val MaxMainSet = 50
  val MaxLuckySet = 12
  val MainSet =(1 to MaxMainSet).map(_.toInt).toVector
  val MainSetSize = 5
  //val LuckySet = (1 to MaxLuckySet).map(_.toInt)

  //data
  case class LuckyStars(nums:Vector[Int])
  case class MainNumbers(nums:Vector[Int])
  case class Ticket(id:Int,mainNumbers: MainNumbers,luckyStars: LuckyStars)

  //helpers
  def getLuckyStars:LuckyStars = {
    val first = Random.nextInt(MaxLuckySet) + 1
    val second = Random.nextInt(MaxLuckySet) + 1
    if (first != second) LuckyStars(Vector(first,second).sorted)
    else {
      getLuckyStars
    }
  }
  def getMainNumbers: MainNumbers = {
    def loop(currentSet:Vector[Int],acc:Vector[Int]):MainNumbers = {
      if (acc.size != MainSetSize) {
        val num = currentSet(Random.nextInt(currentSet.length))
        //remove num from current set and the new num to accumulator
        loop(currentSet.filterNot(_ == num),acc :+ num)
      }
      else {
        MainNumbers(acc.sorted)
      }
    }
    loop(MainSet,Vector())
  }
  def genTickets(num:Int) = (1 to num).map(id => Ticket(id,getMainNumbers,getLuckyStars))
  def draw = (getMainNumbers,getLuckyStars)
  def showTickets(tickets:Seq[Ticket]) = tickets.map(println(_))
}