package probability

import scala.util.Random
import annotation.tailrec
import ProbDist._

case class ProbDist[S](pmf: Vector[(S, Double)]) {
  def pick(chooser: Double): S = ProbDist.pick(pmf, chooser)

  def prob(s: S): Double = pmf.find(_._1 == s).map(_._2).getOrElse(0.0)
}

object ProbDist {
  val rnd = new Random()

  def uniform(n: Int): ProbDist[Int] = ProbDist((1 to n).toVector.map((j) => j -> (1.0/n)))

  def randomSupport(n: Int, p: Double): Vector[Boolean] = {
    val attempt = Vector.fill(n)(rnd.nextDouble() < p)
    if (attempt.contains(true)) attempt else randomSupport(n, p)
  }

  def sparseUniform(n: Int, p: Double): ProbDist[Int] = {
   val raw = randomSupport(n, p).zipWithIndex.map {
     case (b, n) => if (b) n + 1 ->rnd.nextDouble() else n + 1 -> 0.0
    }
    val total = raw.map(_._2).sum
    ProbDist(raw.map{case (n, p) => (n, p/total)})
  }

  @tailrec
  def pick[S](pmf: Vector[(S, Double)], chooser: Double): S = {
    val (s, p) = pmf.head
    if (p > chooser) s else pick(pmf.tail, chooser - p)
  }

  def chooserVec(n: Int): Vector[Double] =
    Vector.fill(n)(()).map((_) => rnd.nextDouble())

  def chooserStream() : Stream[Double] =
    Stream.continually(rnd.nextDouble())
}



class MarkovProcess[S](nextDist: S => ProbDist[S]) {
  @tailrec
  final def getChain(accum: Vector[S], choosers: Vector[Double]): Vector[S] =
    choosers match {
      case Vector() => accum
      case head +: tail =>
        val nextState = nextDist(accum.last).pick(head)
        getChain(accum :+ nextState, tail)
    }

  def randomChain(accum: Vector[S], n: Int): Vector[S] =
    getChain(accum, Vector.fill(n)(rnd.nextDouble))

  def getStream(init: S): Stream[S] =
    Stream.iterate(init)((s) => nextDist(s).pick(rnd.nextDouble()))
}

object MarkovProcess {
  case class FiniteMarkovProcess[S](transition: Map[S, ProbDist[S]]) extends  MarkovProcess(transition){
    val states: Set[S] = transition.keySet

    def accessStep(s: Set[S]): Set[S] =
      s union {
        states.filter((j) => s.exists((i) => transition(i).prob(j) > 0))
      }

    @tailrec
    final def accessSet(s: Set[S]): Set[S] = {
      val next = accessStep(s)
      if (next == s) s else accessSet(next)
    }

    val accessMap: Map[S, Set[S]] = states.map((i) =>  i -> accessSet(Set(i))).toMap

    def accessible(i: S, j: S): Boolean = accessMap(i).contains(j)
  }

  def sparseRandom(n: Int, p: Double): FiniteMarkovProcess[Int] = {
    val succMap =  (1 to n).map{
      (j) => j -> ProbDist.sparseUniform(n, p)
    }.toMap
    FiniteMarkovProcess(succMap)
  }

  def frequencies[S](vec: Vector[S]): Map[S, Int] = vec.groupBy(identity).mapValues(_.size)

  def proportions[S](vec: Vector[S]): Option[Map[S, Double]] = if (vec.isEmpty) None else Some(frequencies(vec).mapValues((f) => f.toDouble / vec.size))
}
