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
      case (x, n) => n -> x.toDouble / n
    }

  val normalized =
    indexedPartialSums.tail.map{
      case (x, n) => n -> (x.toDouble - n.toDouble/2) / math.sqrt(n)
    }

  def sumLines(colour: String) =
    polyLine(
      indexedPartialSums.map{case (s, n)  => (n * xscale, s * yscale)},
      ymax,
      colour
    )

  def proportionLines(colour: String) =
    polyLine(
      proportions.map{case (n, p)  => (n * xscale, p * pscale)},
      ymax,
      colour
    )

    def normalizedLines(colour: String) =
      polyLine(
        normalized.map{case (n, x)  => (n * xscale, (x + 4)/8 * pscale)},
        ymax,
        colour
      )

}


object CoinTosses {

  val rnd: Random = new Random()

  def random(n: Int) =
    CoinTosses((1 to n).map((_) => rnd.nextDouble() > 0.5).toVector)

  val colours = Vector("blue", "cyan", "green", "yellow", "orange", "red")

  val tossesV : Var[Vector[(String,CoinTosses)]] =
    Var(
      for {
        colour <- colours
      } yield (colour, random(400))
    )

  val fmla = """$\frac{X - n/2}{\sqrt{n}}$"""

  val fmlaK = Katex("""\frac{X - n}{\sqrt{n}}""")

  def retoss() : Unit = {
    tossesV :=
      {for {
        colour <- colours
      } yield (colour, random(400))
    }
  }

  def main(): Unit = {

    val coinDiv: Node =
      <div class="panel panel-primary">
      <div class="panel-heading">Repeated tosses of a fair coin</div>
      <div class="panel-body">
      We illustrate the behaviour when repeatedly tossing a coin. Suppose {colours.size.toString}
      coins are tossed several times. We shall see some associated figures, with the coins represented by different colours.
      <h3> Number of heads</h3>
      <p>In the first figure we see the number of heads in the first $n$ tosses. The black line corresponds to half the tosses resulting in heads.</p>
      <svg viewBox="0 0 400 400" width="100%" height="400" xmlns="http://www.w3.org/2000/svg">
      <rect width="400" height="400" stroke="grey" fill="none"></rect>
      <line x1="0" x2="400" y1="400" y2="200" stroke="black" xmlns="http://www.w3.org/2000/svg"></line>
        {tossesV.map(tossVec =>
          tossVec.flatMap{
            case (colour, tosses) =>
            tosses.sumLines(colour)
          }
          )
        }
      </svg>
      <button class="btn btn-primary" onclick={() => retoss()}> New tosses </button>

      <h3> Proportion of  heads </h3>
      <p>Next, we look at the proportion of tosses that are heads.
      The black line corresponds to half the tosses resulting in heads.</p>
      <svg viewBox="0 0 400 400" width="100%" height="400" xmlns="http://www.w3.org/2000/svg">
      <rect width="400" height="400" stroke="grey" fill="none"></rect>
      <line x1="0" x2="400" y1="200" y2="200" stroke="black" xmlns="http://www.w3.org/2000/svg"></line>
        {tossesV.map(tossVec =>
          tossVec.flatMap{
            case (colour, tosses) =>
            tosses.proportionLines(colour)
          }
          )
        }
      </svg>
      <button class="btn btn-primary" onclick={() => retoss()}> New tosses </button>

      <h3> Normalized proportion of  heads </h3>
      <p>Finally, we look at the <em>normalized</em> proportion of tosses that are heads, given by
        {fmla}.
      The black line corresponds to half the tosses resulting in heads.</p>
      <svg viewBox="0 0 400 400" width="100%" height="400" xmlns="http://www.w3.org/2000/svg">
      <rect width="400" height="400" stroke="grey" fill="none"></rect>
      <line x1="0" x2="400" y1="200" y2="200" stroke="black" xmlns="http://www.w3.org/2000/svg"></line>
        {tossesV.map(tossVec =>
          tossVec.flatMap{
            case (colour, tosses) =>
            tosses.normalizedLines(colour)
          }
          )
        }
      </svg>
      <button class="btn btn-primary" onclick={() => retoss()}> New tosses </button>
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
