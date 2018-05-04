package probability

import ammonite.ops._

import scala.util.matching._

object TeXToHtml {
  val top =
    """
<html>
<head>

<!-- Latest compiled and minified CSS  for Bootstrap -->
<link rel="stylesheet" href="../css/bootstrap.min.css">

  <!-- mathjax config similar to math.stackexchange -->
  <script type="text/javascript" async
        src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-MML-AM_CHTML">
      </script>


  <script type="text/x-mathjax-config">
  MathJax.Hub.Config({
  jax: ["input/TeX", "output/HTML-CSS"],
  tex2jax: {
  inlineMath: [ ['$', '$'] ],
  displayMath: [ ['$$', '$$']],
  TeX: { equationNumbers: { autoNumber: "AMS" } },
  processEscapes: true,
  skipTags: ['script', 'noscript', 'style', 'textarea', 'pre', 'code']
  },
  messageStyle: "none",
  "HTML-CSS": { preferredFont: "TeX", availableFonts: ["STIX","TeX"] }
  });
  </script>
</head>
<body>
<div class="container">
<p>&nbsp;</p>

"""

  val foot =
    """
</div>
  </body>
</body>
</html>
"""


  val defReg = "(\\\\def|\\\\newcommand|\\\\renewcommand)(\\\\[a-zA-Z0-9]+)([^\n%]+)".r

  val newCommReg = "(\\\\newcommand\\{)(\\\\[a-zA-Z0-9]+)(\\}[^\n%]+)".r

  val renewCommReg = "(\\\\renewcommand\\{)(\\\\[a-zA-Z0-9]+)(\\}[^\n%]+)".r
  val wd = pwd / 'sitebuilder / "resources"

  val preamble = read(wd / "mypreamble_2017.tex")

  val texFile = read(wd / "stat-and-prob.tex")

  val fullText = texFile.replace("\\input{mypreamble_2017}", preamble)

  val Array(header, text) = fullText.split("\\\\begin\\{document\\}")

  val begReg = """\\begin\{([^\}]+)\}""".r

  val endReg = """\\end\{([^\}]+)\}""".r

  val converter = new TeXToHtml(header, text)

  val dolReg = "[^\\$]\\$[^\\$]".r

  val doldolReg = "\\$\\$".r

  val begItReg = """\\begin\{itemize\}""".r

  val endItReg = """\\end\{itemize\}""".r

  val begEnReg = """\\begin\{enumerate\}""".r

  val endEnReg = """\\begin\{enumerate\}""".r

  val blankLineReg = "\n([ \t]*\n)".r

  def maxOpt[B: Ordering](v: Vector[B]): Option[B] = if (v.isEmpty) None else Some(v.max)


  val thmEnvs = Map(
    "example" -> "Example",
    "question" -> "Question",
    "definition" -> "Definition",
    "remark" -> "Remark",
    "exercise" -> "Exercise",
    "lemma" -> "Lemma",
    "problem" -> "Problem",
    "proposition" -> "Proposition",
    "theorem" -> "Theorem"
  )

  val mathEnvs = Set(
    "align*",
    "equation*",
    "aligned",
    "array",
    "equation",
    "tabular"
  )



  def indices(r: Regex, txt: String): Vector[Int] = r.findAllMatchIn(txt).toVector.map(_.start)

  def dollarIndices(txt: String) : Vector[Int] = indices(dolReg, txt).map(_ + 1)

  def displayIndices(txt: String) : Vector[Int] = indices(doldolReg, txt)

  def itemizeBegs(txt: String): Vector[Int] = indices(begItReg, txt)


  def itemizeEnds(txt: String): Vector[Int] = indices(endItReg, txt)

  def enumerateBegs(txt: String): Vector[Int] = indices(begEnReg, txt)

  def enumerateEnds(txt: String): Vector[Int] = indices(begEnReg, txt)

  def items(txt: String): Vector[Int] = indices("""\\item""".r, txt)

  def firstItem(j: Int, txt: String) : Boolean = {
    val lastBeg = (enumerateBegs(txt) ++ itemizeBegs(txt)).filter(_ < j).max
    maxOpt(items(txt).filter(_ < j)).map{
      _ < lastBeg
    }.getOrElse(true)

  }

  def inMath(j: Int, txt: String) : Boolean = dollarIndices(txt).filter(_ < j).size % 2 == 1

  def inDisplayMath(j: Int, txt: String) : Boolean = displayIndices(txt).filter(_ < j).size % 2 == 1


  def replaceItems(txt: String): String = {
    val itemLess =
      """\\item""".r.replaceAllIn(txt,
        (m) => if (firstItem(m.start, txt)) "<li>" else "</li>\n<li>"
      )
    itemLess.replace("""\begin{itemize}""", "<ul>")
      .replace("""\end{itemize}""", "</ul>")
      .replace("""\begin{enumerate}""", "<ol>")
      .replace("""\end{enumerate}""", "</ol>")
  }

  def replaceBegins(txt: String) =
    begReg.replaceAllIn(txt,
      (m) =>
//        if (inMath(m.start, txt) || inDisplayMath(m.start, txt)) m.group(0)
//        else
        if (thmEnvs.keySet.contains(m.group(1))) s"""<div class="${m.group(1)}"> <strong>${thmEnvs(m.group(1))}</strong> """
        else if (mathEnvs.contains(m.group(1))) Regex.quoteReplacement("$$"+m.group(0))
        else s"""<div class="${m.group(1)}">"""
    )

  def replaceEnds(txt: String) =
    endReg.replaceAllIn(txt,
      (m) =>
//        if (inMath(m.start, txt) || inDisplayMath(m.start, txt)) {println(s"${m.group(0)} in math environment"); m.group(0)}
//        else
        if (mathEnvs.contains(m.group(1))) Regex.quoteReplacement(m.group(0)+"$$")
        else "</div>"
    )

  def replaceBlanks(txt: String) = blankLineReg.replaceAllIn(txt, "</p>\n<p>")


}

class TeXToHtml(header: String, text: String) {
  import TeXToHtml._

  val defs = defReg.findAllMatchIn(header).toVector

  val newCommands = newCommReg.findAllMatchIn(header).toVector

  val renewCommands = renewCommReg
    .findAllMatchIn(header)
    .toVector ++ renewCommReg
    .findAllMatchIn(header)
    .toVector


  val defSubs =
    (defs.map((m) => m.group(2) -> m.group(3).trim.drop(1).dropRight(1)) ++
      (newCommands ++ renewCommands).map((m) => m.group(2) -> m.group(3).trim.drop(2).dropRight(1))
      ).filterNot((c) => Set("\\I", "\\matrices", "\\para", "\\parag").contains(c._1))

  def defReplace(txt: String) = defSubs.foldLeft[String](txt) {
    case (t, (x, y)) =>
      new Regex(x.replace("\\", "\\\\") + "([^a-zA-Z0-9])")
        .replaceAllIn(t, (m) => Regex.quoteReplacement(y + m.group(1)))
  }

  def recDefReplace(txt: String): String = {
    val next = defReplace(txt)
    if (next == txt) next else recDefReplace(next)
  }

  lazy val defReplaced = recDefReplace(text)

    lazy val allReplaced = replaceBlanks(replaceEnds(replaceBegins(replaceItems(defReplaced))))

  lazy val newFile = header + """\begin{document}""" + defReplaced

  def replace = write.over(wd / "repl.tex", newFile)

  lazy val crudeHtml = top + allReplaced + foot

  def html = write.over(pwd / "docs" / "crude" / "index.html", crudeHtml)
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
