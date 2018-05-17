package probability

import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.html.Input

import scala.scalajs.js.annotation._
import scala.xml._

@JSExportTopLevel("FairCoin")
object FairCoin {
  import mhtml._
  import scala.xml.Node

  @JSExport
  def main(): Unit = {
    val count: Var[Int] = Var(0)

    val component =
      <div class="panel panel-primary">
        <div class="panel-heading">Is the coin fair? (dummy)</div>
        <div class="panel-body">
        <button class="btn btn-primary" onclick={() => count.update(_ + 1)}>Toss a coin</button>{
        count.map(i => if (i <= 0) <div></div>
        else <div><strong>Tossed</strong></div>)}
        {count.map(i => if (i <= 2) <div></div> else <div><strong>More tosses</strong></div>)}
        {count.map(i => if (i <= 5) <div></div> else <div><strong>Even more tosses</strong></div>)}
        </div>
      </div>

    val position = dom.document.querySelector("#theorem-2")
    val div = document.createElement("div")
    position.parentNode.insertBefore(div, position.nextSibling)
    mount(div, component)
  }
}
