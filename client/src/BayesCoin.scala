package probability

import org.scalajs.dom
import org.scalajs.dom._

import scala.scalajs.js.annotation._
import scala.util.Random
import scalajs.js

@JSExportTopLevel("BayesCoin")
object BayesCoin {
  import mhtml._
  import scala.xml.Node

  /**
    * random number generator
    */
  val rnd: Random = new Random()

  /**
    * probability that the chosen coin is biased
    */
  val probOfBias: Var[Double] = Var(0.5)

  /**
    * probability of heads for a biased coin
    */
  val biasedHeadsProb: Var[Double] = Var(0.9)

  /**
    * uniform random variable based on which bias is chosen
    */
  val biasChooser: Var[Double] = Var(rnd.nextDouble())

  /**
    * generate a fresh coin, choosing whether biased
    */
  def freshCoin(): Unit = {
    biasChooser := rnd.nextDouble()
  }

  /**
    * probability of head
    */
  val pV: Rx[Double] =
    probOfBias.zip(biasedHeadsProb).zip(biasChooser).map {
      case ((pb, pbh), bc) =>
        if (bc < pb) pbh // biased coin
        else 0.5
    }

  /**
    * sequence of random variables, uniform
    */
  val tossesValV: Var[Vector[Double]] = Var(Vector())

  /**
    * sequence of random tosses
    */
  val tossesV: Rx[Vector[Boolean]] =
    tossesValV.zip(pV).map {
      case (v, p) => v.map(_ < p)
    }

  /**
    * guess, if any
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
    * whether the coin is fair
    */
  val fairR: Rx[Boolean] = pV.map(_ == 0.5)

  /**
    * render the view
    */
  @JSExport
  def main(): Unit = {

    val coinDiv: Node =
      <div class="panel panel-primary">
        <div class="panel-heading">Is the coin fair?</div>
        <div class="panel-body">
          <p>Try to figure out whether the coin is fair by tossing it several times. This time, you are told, and can change, both the
          probability that a coin is biased and the probability of heads for a biased coin.</p>
          <button class="btn btn-primary" onclick={() =>
          tossesValV.update(_ :+ rnd.nextDouble())}>Toss the coin</button>
          <p></p>
          <div> <strong>Heads:</strong> {headsR} </div>
          <div><strong>Tails:</strong> {tailsR} </div>
          <div>
            <strong>Your guess:</strong>
            <button class="btn btn-success" onclick = {() => guessOptV := Some(true)} >Fair</button>
            <button class="btn btn-danger" onclick = {() => guessOptV := Some(false)}>Biased</button>
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
                <div>The coin is <strong>fair.</strong></div>
              else <div> The coin is <strong>biased</strong> with probability of heads {p}.</div>
            ).getOrElse(<div></div>)

        }
          }
          <p></p>
          <div><button class="btn btn-primary" onclick = {() => {freshCoin(); guessOptV := None; tossesValV := Vector()}} >New coin</button></div>
          <p></p>
          <h3> Probabilities</h3>
          <form>
          <div  class="form-group">
            <label for="bias">Probability that the coin is biased:</label>
            <input type="text" id="bias" class="form-control" size="4" value={probOfBias.map(_.toString)} oninput={
          (e: js.Dynamic) =>
            probOfBias := e.target.value.asInstanceOf[String].toDouble
          }/>
              </div>
            <div  class="form-group">

            <label for="head">Probability of head for a biased coin:</label>
            <input type="text" id="head" class="form-control" size="4" value={biasedHeadsProb.map(_.toString)} oninput={
          (e: js.Dynamic) =>
            biasedHeadsProb := e.target.value.asInstanceOf[String].toDouble
          }/>
          </div>
          </form>

        </div>
      </div>

    val positionOpt = Option(dom.document.querySelector("#bayes-coin"))
    positionOpt.foreach { position =>
      val div = document.createElement("div")
      position.appendChild(div)
      mount(div, coinDiv)
    }
  }
}
