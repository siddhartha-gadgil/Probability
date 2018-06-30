package probability

import org.scalajs.dom
import org.scalajs.dom._

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.util.Random

object Birthdays {

  import mhtml._
  import scala.xml.Node

  val rnd: Random = new Random()

  val numV: Var[Int] = Var(0)

  val birthdaysV: Var[Vector[Int]] = Var(Vector())

  def randomBirthdays(n: Int): Unit =
    birthdaysV := (1 to n).toVector.map((_) => rnd.nextInt(365))

  val dupsR = birthdaysV.map {
    (bds) =>
      val fs = bds.groupBy(identity).mapValues(_.size)
      bds.filter((n) => fs(n) > 1).sorted.distinct
  }

  def main(): Unit = {
    val bdyDiv: Node =
      <div class="panel panel-primary">
        <div class="panel-heading">Birthday Paradox</div>
        <div class="panel-body bg-info">
          <p>We randomly choose birthdays of different persons. How likely do you think it is that two persons have the same birthday?</p>
          <div>
            <label>Number of persons:</label>
            <input type="number" min="0" max ="365" oninput={
          (e: js.Dynamic) =>
            numV := e.target.value.asInstanceOf[String].toInt
          }/>
            <span>({numV}) </span>
          {(numV).map{(num) =>
            <button class="btn btn-primary" type="button" onclick={
            () => randomBirthdays(num)}>
              Choose random birthdays
            </button>
          }}
          </div>
          <h4> Birthdays:</h4>
          <ul class="list-inline bg-light">
            {birthdaysV.map((bdys) => bdys.map((d) => <li>{d}</li>))}
          </ul>
          <h4> Birthdays of more than one person:</h4>
          <ul class="list-inline">
            {dupsR.map((bdys) => bdys.map((d) => <li>{d}</li>))}
          </ul>

        </div>
      </div>


    val positionOpt = Option(dom.document.querySelector("#theorem-14"))
    positionOpt.foreach { (position) =>
      val div = document.createElement("div")
      position.parentNode.insertBefore(div, position.nextSibling)
      mount(div, bdyDiv)
    }
  }
}