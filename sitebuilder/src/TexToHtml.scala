package probability

import ammonite.ops._

import scala.util.matching._

object TeXToHtml {
  val defReg = "(\\\\def)(\\\\[a-zA-Z0-9]+)([^\n%]+)".r

  val newCommReg = "(\\\\newcommand\\{)(\\\\[a-zA-Z0-9]+)(\\}[^\n%]+)".r

  val renewCommReg = "(\\\\renewcommand\\{)(\\\\[a-zA-Z0-9]+)(\\}[^\n%]+)".r
  val wd = pwd / 'sitebuilder / "resources"

  val preamble = read(wd / "mypreamble_2017.tex")

  val texFile = read(wd / "stat-and-prob.tex")

  val fullText = texFile.replace("\\input{mypreamble_2017}", preamble)

  val Array(header, text) = fullText.split("\\\\begin\\{document\\}")

  val begReg = """\\begin\{([^\}]+)\}""".r

  val converter = new TeXToHtml(header, text)
}

class TeXToHtml(header: String, text: String) {
  import TeXToHtml._

  val defs = defReg.findAllMatchIn(header).toVector

  val newCommands = newCommReg.findAllMatchIn(header).toVector
    .filter((c) => !c.group(2).startsWith("\\para"))

  val renewCommands = renewCommReg
    .findAllMatchIn(header)
    .toVector ++ renewCommReg
    .findAllMatchIn(header)
    .toVector

  val allSubs: Vector[Regex.Match] =
    defs ++ newCommands ++ renewCommands

  val defSubs =
    allSubs.map((m) => m.group(2) -> m.group(3).trim.drop(1).dropRight(1))

  lazy val defReplaced = defSubs.foldLeft[String](text) {
    case (t, (x, y)) =>
      new Regex(x.replace("\\", "\\\\") + "([^a-zA-Z0-9])")
        .replaceAllIn(t, (m) => Regex.quoteReplacement(y + m.group(1)))
  }

  lazy val newFile = header + """\begin{document}""" + defReplaced

  def replace = write.over(wd / "repl.tex", newFile)
}

object NewTex extends App {
  TeXToHtml.converter.replace
}
/*
The cases for begin{_} :
Vector(
  "center",
  "example",
  "question",
  "definition",
  "itemize",
  "cases",
  "enumerate",
  "remark",
  "align*",
  "exercise",
  "lemma",
  "problem",
  "proposition",
  "proof",
  "equation*",
  "aligned",
  "figure",
  "theorem",
  "array",
  "equation",
  "tabular",
  "thebibliography"
)

 */
