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

  /**
    * random number generator
    */
  val rnd: Random = new Random()

  /**
    * probability that tosses are dependents
    */
  val probOfDependence: Var[Double] = Var(0.5)

  /**
    * probability of a toss being different from the previous, if the tosses are dependent
    */
  val flipProb: Var[Double] = Var(0.7) // in the case of dependent tosses

  /**
    * uniform random number between 0 and 1 based on which it is decided whether tosses are dependent
    */
  val dependenceChooser: Var[Double] = Var(rnd.nextDouble())

//  val flipChooser: Var[Double] = Var(rnd.nextDouble())

  /**
    * the first toss
    */
  val firstToss : Var[Boolean] = Var(rnd.nextBoolean())

  /**
    * reset the coin, choosing the first toss and whe
    */
  def freshCoin(): Unit = {
    dependenceChooser := rnd.nextDouble()
//    flipChooser := rnd.nextDouble()
    firstToss := rnd.nextBoolean()
  }

  /**
    * probability that a toss is different from the previous
    */
  val pV: Rx[Double] =
    probOfDependence.zip(flipProb).zip(dependenceChooser).map {
      case ((pb, pbh), bc) =>
        if (bc < pb) pbh // dependent tosses
        else 0.5
    }

  /**
    * determines sequences of heads and tails from first toss and sequence of flips
    * @param first the first toss
    * @param flips vector of whether tosses coincide with the previous
    * @return sequence of booleans corresponding to heads
    */
  def tosses(first: Boolean, flips: Vector[Boolean]): Vector[Boolean] =
    flips match {
      case Vector() => Vector()
      case Vector() :+ head => Vector(first)
      case init :+ fl =>
        val prev = tosses(first, init)
        if (fl) prev :+ !prev.last else prev :+ prev.last
    }

  /**
    * sequence of uniform random variables to decide whether to flip
    */
  val flipsValV: Var[Vector[Double]] = Var(Vector())

  /**
    * sequence of booleans for whether we flip
    */
  val flipsV: Rx[Vector[Boolean]] =
    flipsValV.zip(pV).map {
      case (v, p) => v.map(_ < p)
    }

  /**
    * sequence of tosses, as a boolean
    */
  val tossesV: Rx[Vector[Boolean]] = firstToss.zip(flipsV).map{case (x, ys) => tosses(x, ys)}

  /**
    * view of sequence of tosses
    */
  val tossesView: Rx[String] = tossesV.map(v => v.map(t => if (t) "H" else "T").mkString(","))

  /**
    * guess, if made
    */
  val guessOptV: Var[Option[Boolean]] = Var(None)

  /**
    * number of heads
    */
  val headsR: Rx[Int] = tossesV.map(tosses => tosses.count(identity))

  /**
    * number of tails
    */
  val tailsR: Rx[Int] = tossesV.map(tosses => tosses.count(!_))

  /**
    * whether tosses are independent
    */
  val independentR: Rx[Boolean] = pV.map(_ == 0.5)

  /**
    * render the view in a div based on id
    */
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
          {guessOptV.zip(independentR).map{
          case (guessOpt, fair) =>
            guessOpt.map(guess =>
              if (guess == fair)
                <div>That's correct!</div>

              else
                <div>Sorry!</div>
            ).getOrElse(<div></div>)
        }
          }
          {guessOptV.zip(independentR).zip(pV).map{
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

              <label for="head">Probability of change for dependent tosses:</label>
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
