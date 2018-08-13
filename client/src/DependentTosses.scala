package probability

import org.scalajs.dom
import org.scalajs.dom._

import scala.scalajs.js.annotation._
import scala.util.Random
import scalajs.js

@JSExportTopLevel("DependentTosses")
object DependentTosses {
  import mhtml._
  import scala.xml.Node

  val rnd: Random = new Random()

  val probOfDependence: Var[Double] = Var(0.5)

  val flipProb: Var[Double] = Var(0.7) // in the case of dependent tosses

  val biasChooser: Var[Double] = Var(rnd.nextDouble())

  val flipChooser: Var[Double] = Var(rnd.nextDouble())

  val firstToss : Var[Boolean] = Var(rnd.nextBoolean())

  def freshCoin(): Unit = {
    biasChooser := rnd.nextDouble()
    flipChooser := rnd.nextDouble()
    firstToss := rnd.nextBoolean()
  }

  val pV: Rx[Double] =
    probOfDependence.zip(flipProb).zip(biasChooser).zip(flipChooser).map {
      case (((pb, pbh), bc), hc) =>
        if (bc < pb) pbh // dependent tosses
        else 0.5
    }

  def tosses(first: Boolean, flips: Vector[Boolean]): Vector[Boolean] =
    flips match {
      case Vector() => Vector()
      case Vector() :+ head => Vector(first)
      case init :+ fl =>
        val prev = tosses(first, init)
        if (fl) prev :+ !prev.last else prev :+ prev.last
    }

  val flipsValV: Var[Vector[Double]] = Var(Vector())

  val flipsV: Rx[Vector[Boolean]] =
    flipsValV.zip(pV).map {
      case (v, p) => v.map(_ < p)
    }

  val tossesV: Rx[Vector[Boolean]] = firstToss.zip(flipsV).map{case (x, ys) => tosses(x, ys)}

  val tossesView: Rx[String] = tossesV.map(v => v.map(t => if (t) "H" else "T").mkString(","))

  val guessOptV: Var[Option[Boolean]] = Var(None)

  val headsR: Rx[Int] = tossesV.map(tosses => tosses.count(identity))

  val tailsR: Rx[Int] = tossesV.map(tosses => tosses.count(!_))

  val fairR: Rx[Boolean] = pV.map(_ == 0.5)

  @JSExport
  def main(): Unit = {

    val coinDiv: Node =
      <div class="panel panel-primary">
        <div class="panel-heading">Is the coin fair?</div>
        <div class="panel-body">
          <p>Try to figure out whether the coin tosses are independent. You can change both the
            probability that the tosses are dependent and the probability of <em>changing</em> in the dependent case.</p>
          <button class="btn btn-primary" onclick={() =>
            flipsValV.update(_ :+ rnd.nextDouble())}>Toss the coin</button>
          <p></p>
          <div> <strong>Heads:</strong> {headsR} </div>
          <div><strong>Tails:</strong> {tailsR} </div>
          <div><strong>Sequence of tosses: </strong> <h4>{tossesView}</h4></div>
          <div>
            <strong>Your guess:</strong>
            <button class="btn btn-success" onclick = {() => guessOptV := Some(true)} >Independent</button>
            <button class="btn btn-danger" onclick = {() => guessOptV := Some(false)}>Dependent</button>
          </div>
          {guessOptV.zip(fairR).map{
          case (guessOpt, fair) =>
            guessOpt.map(guess =>
              if (guess == fair)
                <div>That's correct!</div>

              else
                <div>Sorry!</div>
            ).getOrElse(<div></div>)
        }
          }
          {guessOptV.zip(fairR).zip(pV).map{
          case ((guessOpt, fair), p) =>
            guessOpt.map(_ =>
              if (fair)
                <div>The tosses are <strong>independent.</strong></div>
              else <div> The tosses are <strong>dependent</strong> with probability of changing {p}.</div>
            ).getOrElse(<div></div>)

        }
          }
          <p></p>
          <div><button class="btn btn-primary" onclick = {() => {freshCoin(); guessOptV := None; flipsValV := Vector()}} >New coin</button></div>
          <p></p>
          <h3> Probabilities</h3>
          <form>
            <div  class="form-group">
              <label for="bias">Probability that the tosses are dependent:</label>
              <input type="text" id="bias" class="form-control" size="4" value={probOfDependence.map(_.toString)} oninput={
            (e: js.Dynamic) =>
              probOfDependence := e.target.value.asInstanceOf[String].toDouble
            }/>
            </div>
            <div  class="form-group">

              <label for="head">Probability of flip for dependent tosses:</label>
              <input type="text" id="head" class="form-control" size="4" value={flipProb.map(_.toString)} oninput={
            (e: js.Dynamic) =>
              flipProb := e.target.value.asInstanceOf[String].toDouble
            }/>
            </div>
          </form>

        </div>
      </div>

    val positionOpt = Option(dom.document.querySelector("#dependent-tosses"))
    positionOpt.foreach { position =>
      val div = document.createElement("div")
      position.appendChild(div)
      mount(div, coinDiv)
    }
  }
}
