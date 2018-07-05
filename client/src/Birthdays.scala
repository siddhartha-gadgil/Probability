package probability

import org.scalajs.dom
import org.scalajs.dom._

import scala.scalajs.js
import scala.util.Random

object Birthdays {

  import mhtml._
  import scala.xml.Node

  val rnd: Random = new Random()

  val numV: Var[Int] = Var(0)

  val birthdaysV: Var[Vector[Int]] = Var(Vector())

  def randomBirthdays(n: Int): Unit =
    birthdaysV := (1 to n).toVector.map((_) => rnd.nextInt(365))

  val dupsR: Rx[Vector[Int]] = birthdaysV.map {
    (bds) =>
      val fs = bds.groupBy(identity).mapValues(_.size)
      bds.filter((n) => fs(n) > 1).sorted.distinct
  }

  val months : Vector[(String, Int)]= Vector(
    "Jan" -> 31,
    "Feb" -> 28,
    "Mar" -> 31,
    "Apr" -> 30,
    "May" -> 31,
    "Jun" -> 30,
    "Jul" -> 31,
    "Aug" -> 31,
    "Sep" -> 30,
    "Oct" -> 31,
    "Nov" -> 30,
    "Dec" -> 31
   )

  def monthDay(n: Int, m: Vector[(String, Int)]): (String, Int) = {
    val (mon, days) = m.head
    if (n < days) (mon, n + 1) else monthDay(n - days, m.tail)
  }

  def date(n: Int): String = {
    val (mon, day) = monthDay(n, months)
    s"$mon $day "
  }

  def distSeq(n: Int): Vector[Int] = (0 until n).map(365 - _).toVector

  def allSeq(n: Int) : Vector[Int] = Vector.fill(n)(365)

  def probDistinct(n: Int): Double = distSeq(n).zip(allSeq(n)).map{case (a, b) => a.toDouble/ b.toDouble}.fold(1.0)(_ * _)

  val showProb : Var[Boolean] = Var(false)

  val probClass : Rx[String] =
    showProb.map((b) => if (b) "collapse show" else "collapse")

  val iconClass: Rx[String] =
    showProb.map((b) => if (b) "glyphicon glyphicon-minus" else "glyphicon glyphicon-plus")

  def main(): Unit = {
    val bdyDiv: Node =
      <div class="panel panel-primary">
        <div class="panel-heading">Birthday Paradox</div>
        <div class="panel-body">
          <p>We randomly choose birthdays of different persons. How likely do you think it is that two persons have the same birthday?</p>
          <div>
            <label>Number of persons:</label>
            <input type="number" min="0" max ="365" oninput={
          (e: js.Dynamic) =>
            numV := e.target.value.asInstanceOf[String].toInt
          }/>

          {numV.map{ (num) =>
            <button class="btn btn-primary" type="button" onclick={
            () => randomBirthdays(num)}>
              Choose random birthdays
            </button>
          }}
          </div>
          <p></p>
          <div class="panel panel-info">
            <div class="panel-heading">Random birthdays</div>
            <div class="panel-body">
              <ul class="list-inline">
                {birthdaysV.map((bdys) => bdys.map(
                (d) => if (bdys.count(_ == d) >1)
                  <li class="bg-primary">{date(d)}</li>
                else <li>{date(d)}</li>))}
              </ul>

            </div>
          </div>
          <div class="panel panel-info">
            <div class="panel-heading">Birthdays of more than one person</div>
            <div class="panel-body">
              <ul class="list-inline">
                {dupsR.map((bdys) => bdys.map((d) => <li>{date(d)}</li>))}
              </ul>

            </div>
          </div>

          <h4>Probability of distinct birthdays:
          <button class="btn btn-primary" type="button" onclick={
          () => showProb.update((b) => !b)}>
             <span class={iconClass}></span>
          </button>
          </h4>
          <div class={probClass}>
          <div class="panel panel-warning">
            <div class="panel-heading">Probability of the event:

             </div>
            <div class="panel-body">
              <ul>
                <li>
                  <h4>Number of sequences of distinct brithdays: </h4>
                  {numV.map((n) => distSeq(n).mkString(" \u00D7 "))}
                </li>
                <li>
                  <h4>Number of sequences of birthdays:</h4>
                  {numV.map((n) => allSeq(n).mkString(" \u00D7 "))}
                </li>
                <li>
                  <h4>Probability of birthdays being all distinct:</h4>
                  {numV.map((n) => probDistinct(n))}
                </li>
              </ul>

            </div>
          </div>
          </div>

        </div>
      </div>


    val positionOpt = Option(dom.document.querySelector("#birthdays"))
    positionOpt.foreach { (position) =>
      val div = document.createElement("div")
      // position.parentNode.insertBefore(div, position.nextSibling)
      position.appendChild(div)
      mount(div, bdyDiv)
    }
  }
}
