package probability

import org.scalajs.dom
import org.scalajs.dom._

import scala.scalajs.js.annotation._
import scala.util.Random

@JSExportTopLevel("FairCoin")
object FairCoin {
  import mhtml._
  import scala.xml.Node

  val rnd: Random = new Random()

  def getP: Double = if (rnd.nextBoolean()) 0.5 else rnd.nextDouble()

  val pV: Var[Double] = Var(getP)

  val tossesV: Var[Vector[Boolean]] = Var(Vector())

  val guessOptV: Var[Option[Boolean]] = Var(None)

  val headsR: Rx[Int] = tossesV.map((tosses) => tosses.count(identity))

  val tailsR: Rx[Int] = tossesV.map((tosses) => tosses.count(!(_)))

  val fairR: Rx[Boolean] = pV.map(_ == 0.5)

  @JSExport
  def main(): Unit = {

    val coinDiv: Node =
      <div class="panel panel-primary">
      <div class="panel-heading">Is the coin fair?</div>
      <div class="panel-body">
        <p>Try to figure out whether the coin is fair by tossing it several times.</p>{pV.map
        {(p) => <button class="btn btn-primary" onclick={() =>
        tossesV.update(_ :+ (rnd.nextDouble() < p))}>Toss the coin</button>}}
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
        <div><button class="btn btn-primary" onclick = {() => {pV := getP; guessOptV := None; tossesV := Vector()}} >New coin</button></div>

      </div>
    </div>



    val position = dom.document.querySelector("#theorem-2")
    val div = document.createElement("div")
    position.parentNode.insertBefore(div, position.nextSibling)
    mount(div, coinDiv)
  }
}
