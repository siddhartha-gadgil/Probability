package probability

import scala.scalajs.js
import scala.util.Random
import mhtml._
import org.scalajs.dom
import org.scalajs.dom.document

import scala.xml.{Elem, Node}
import annotation._

import Cantor._

case class Cantor(consts: Vector[ConstFunc], undefs: Vector[UndefinedFunc]){
  lazy val next =
    Cantor(
      (consts ++ undefs.map(_.nextConst)).sortBy(_.x1),
      undefs.flatMap(_.nextUndefs).sortBy(_.x1)
    )

  lazy val allLines: Vector[Elem] = consts.map(_.view) ++ undefs.map(_.view)

  lazy val view: Elem =
    <div>
      <svg viewBox={s"0 0 $xscale $yscale"} width="80%" height="600" xmlns="http://www.w3.org/2000/svg">
        <rect x="0" y="0" width="600" height="600" stroke="grey" fill="transparent" stroke-width="1"/>
        {allLines}
      </svg>
      <p></p>
    </div>
}


object Cantor{
  val xscale = 600
  val yscale = 600

  case class ConstFunc(x1: Double, x2: Double, y: Double){
    lazy val view: Elem =
      <line x1={(x1 * xscale).toInt.toString}
        y1={(yscale -  (y * yscale).toInt).toString}
        x2={(x2 * xscale).toInt.toString}
        y2={(yscale - (y * yscale).toInt).toString}
        stroke="blue" stroke-width="1" xmlns="http://www.w3.org/2000/svg"></line>
  }

  case class UndefinedFunc(x1: Double, x2: Double, y1: Double, y2: Double){
    val a : Double = (2 * x1 + x2)/3

    val b : Double =  (2 *x2 + x1)/3

    val y: Double = (y1 + y2)/2

    def nextConst = ConstFunc(a, b, y)

    def nextUndefs: Vector[UndefinedFunc] =
      Vector(
        UndefinedFunc(x1, a, y1, y),
        UndefinedFunc(b, x2, y, y2)
      )

    lazy val view: Elem =
      <line x1={(x1 * xscale).toInt.toString}
        y1={(yscale -  (y1 * yscale).toInt).toString}
        x2={(x2 * xscale).toInt.toString}
        y2={(yscale - (y2 * yscale).toInt).toString}
        stroke="grey" stroke-dasharray="1 2" xmlns="http://www.w3.org/2000/svg"></line>
  }

  val base: Cantor = Cantor(Vector(), Vector(UndefinedFunc(0, 1, 0, 1)))

  @tailrec
  def step(n: Int, accum: Cantor = base): Cantor =
    if (n < 1) accum
    else step(n -1 , accum.next)

  val nV : Var[Int] = Var(1)

  val cantorViewR: Rx[Elem] = nV.map((n) => step(n).view)

  def main(): Unit = {
    val cantorDiv =
      <div class="panel panel-primary">
        <div class="panel-heading">
          Approximating the Cantor Distribution
        </div>
        <div class="panel-body">
          <label>Number of steps:</label>
          <input type="number" value="1" onchange={
        (e: js.Dynamic) =>
          nV := e.target.value.asInstanceOf[String].toInt
        }/>
          {cantorViewR}
        </div>
      </div>

    val positionOpt = Option(dom.document.querySelector("#cantor"))
    positionOpt.foreach { (position) =>
      val div = document.createElement("div")
      position.appendChild(div)
      mount(div, cantorDiv)
    }

  }

}
