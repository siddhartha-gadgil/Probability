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

  val converter = new TeXToHtml(header, text)
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
