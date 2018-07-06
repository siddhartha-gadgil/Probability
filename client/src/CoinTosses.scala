package probability

import org.scalajs.dom
import org.scalajs.dom._

import scala.scalajs.js.annotation._
import scala.util.Random

import mhtml._
import scala.xml.Node

import Svg._


case class CoinTosses(tosses: Vector[Boolean]){
  val n = tosses.size

  val xmax = 400
  val ymax = 400

  val xscale = xmax.toDouble / n
  val yscale = ymax.toDouble / n
  val pscale = ymax.toDouble

  val zeroOne = tosses.map((b) => if (b) 1 else 0)
  val partialSums = zeroOne.scan(0)(_ + _)
  val indexedPartialSums = partialSums.zipWithIndex
  val proportions =
    indexedPartialSums.tail.map{
      case (x, n) => x.toDouble / n
    }

  val normalized =
    indexedPartialSums.tail.map{
      case (x, n) => (x.toDouble - n.toDouble/2) / math.sqrt(n)
    }

  val sumLines =
    polyLine(
      indexedPartialSums.map{case (y, x) => (x * xscale, y * yscale)},
      ymax
    )

    val sumView =
        <svg viewBox="0 0 400 400" width="800" height="400" xmlns="http://www.w3.org/2000/svg">
        <rect width="400" height="400" stroke="grey" fill="none"></rect>
          {sumLines}
        </svg>
}


object CoinTosses {

  val rnd: Random = new Random()

  def random(n: Int) =
    CoinTosses((1 to n).map((_) => rnd.nextDouble() > 0.5).toVector)

  val tossesV : Var[CoinTosses] = Var(random(100))


  def main(): Unit = {

    val coinDiv: Node =
      <div class="panel panel-primary">
      <div class="panel-heading">Heads in repeated tosses</div>
      <div class="panel-body">
      Plot of heads
        {tossesV.map(_.sumView)}
      </div>
    </div>


    val positionOpt = Option(dom.document.querySelector("#coin-tosses"))
    positionOpt.foreach{(position)=>
      val div = document.createElement("div")
      position.appendChild(div)
      mount(div, coinDiv)
  }
  }
}
