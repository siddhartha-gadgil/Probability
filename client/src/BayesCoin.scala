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

  val rnd: Random = new Random()

  val probOfBias: Var[Double] = Var(0.5)

  val biasedHeadsProb: Var[Double] = Var(0.9)

  def getP: Double = if (rnd.nextBoolean()) 0.5 else rnd.nextDouble()

  val biasChooser: Var[Double] = Var(rnd.nextDouble())

  val headChooser: Var[Double] = Var(rnd.nextDouble())

  def freshCoin(): Unit = {
    biasChooser := rnd.nextDouble()
    headChooser := rnd.nextDouble()
  }

  val pV: Rx[Double] =
    probOfBias.zip(biasedHeadsProb).zip(biasChooser).zip(headChooser).map {
      case (((pb, pbh), bc), hc) =>
        if (bc < pb) pbh // biased coin
        else 0.5
    }

  val tossesValV: Var[Vector[Double]] = Var(Vector())

  val tossesV: Rx[Vector[Boolean]] =
    tossesValV.zip(pV).map {
      case (v, p) => v.map(_ < p)
    }

  val guessOptV: Var[Option[Boolean]] = Var(None)

  val headsR: Rx[Int] = tossesV.map((tosses) => tosses.count(identity))

  val tailsR: Rx[Int] = tossesV.map((tosses) => tosses.count(!_))

  val fairR: Rx[Boolean] = pV.map(_ == 0.5)

  @JSExport
  def main(): Unit = {

    val coinDiv: Node =
      <div class="panel panel-primary">
        <div class="panel-heading">Is the coin fair?</div>
        <div class="panel-body">
          <p>Try to figure out whether the coin is fair by tossing it several times.</p>
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
            guessOpt.map((guess) =>
              if (guess == fair)
                <div>That's correct!</div>

              else
                <div>Sorry!</div>
            ).getOrElse(<div></div>)
            }
          }
          {guessOptV.zip(fairR).zip(pV).map{
          case ((guessOpt, fair), p) =>
            guessOpt.map((_) =>
              if (fair)
                <div>The coin is <strong>fair.</strong></div>
              else <div> The coin is <strong>biased</strong> with probability of heads {p}.</div>
            ).getOrElse(<div></div>)

        }
          }
          <p></p>
          <div><button class="btn btn-primary" onclick = {() => {freshCoin(); guessOptV := None; tossesValV := Vector()}} >New coin</button></div>
          <p></p>
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
    positionOpt.foreach { (position) =>
      val div = document.createElement("div")
      position.appendChild(div)
      mount(div, coinDiv)
    }
  }
}
