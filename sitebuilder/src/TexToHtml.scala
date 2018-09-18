package probability

import ammonite.ops._

import scala.util.matching._

/**
  * converting Manjunath's notes into html with embedded illustrations
  */
object TeXToHtml {
  /**
    * top of each notes page
    */
  val top: String =
    """<html>
      |<head>
      |<meta charset="utf-8">
      |   <meta http-equiv="X-UA-Compatible" content="IE=edge">
      |   <meta name="viewport" content="width=device-width, initial-scale=1">
      |
      |<title> Probability Models and Stastics</title>
      |<link rel="icon" href="../IIScLogo.jpg">
      |
      |<!-- Latest compiled and minified CSS  for Bootstrap -->
      |<link rel="stylesheet" href="../css/bootstrap.min.css">
      |<link rel="stylesheet" href="../css/extras.css">
      |
      |
      |
      |  <!-- mathjax config similar to math.stackexchange -->
      |
      |
      |
      |  <script type="text/x-mathjax-config">
      |  MathJax.Hub.Config({
      |  TeX: { equationNumbers: { autoNumber: "AMS" } },
      |  jax: ["input/TeX", "output/HTML-CSS"],
      |  tex2jax: {
      |  inlineMath: [ ['$', '$'] ],
      |  displayMath: [ ['$$', '$$'], ['\\[', '\\]' ]],
      |  processEscapes: true,
      |  skipTags: ['script', 'noscript', 'style', 'textarea', 'pre', 'code']
      |  },
      |  messageStyle: "none",
      |  "HTML-CSS": { preferredFont: "TeX", availableFonts: ["STIX","TeX"] }
      |  });
      |  </script>
      |  <script type="text/javascript" async
      |      src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-MML-AM_CHTML">
      |       </script>
      |</head>
      |<body>
    """.stripMargin

  /**
    * navigation bar
    * @param menus menus for chapters
    * @return
    */
  def nav(menus: String*): String =
    s"""
    |<nav class="navbar navbar-default navbar-fixed-bottom">
    |    <div class="container-fluid">
    |      <!-- Brand and toggle get grouped for better mobile display -->
    |      <span class="navbar-brand navbar-right">Probability and Statistics (notes by Manjunath Krishnapur)</span>
    |      <div class="navbar-header">
    |        <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
    |          <span class="sr-only">Toggle navigation</span>
    |          <span class="icon-bar"></span>
    |          <span class="icon-bar"></span>
    |          <span class="icon-bar"></span>
    |        </button>
    |
    |      </div>
    |
    |      <!-- Collect the nav links, forms, and other content for toggling -->
    |      <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
    |
    |        <ul class="nav navbar-nav">
    |          <li><a href="index.html">Table of Contents</a></li>
    |            ${menus.mkString("\n")}
    |
    |
    |        </ul>
    |      </div><!-- /.navbar-collapse -->
    |    </div><!-- /.container-fluid -->
    |  </nav>
    |
  """.stripMargin

  /**
    * the banner for the table of contents
    */
  val banner: String =
    """
      |<div class="container-fluid">
      |<div class="bg-primary">
      |     <div class="banner">
      |
      |     <center><h2> Probability and Statistics </h2></center>
      |     <center><h4> Notes by Manjunath Krishnapur </h4></center>
      |   </div>
      | </div>
      | </div>
      | <div class="container">
      | <section>
      |<p>&nbsp;</p>
    """.stripMargin

  /**
    * foot of notes pages
    */
  val foot: String =
    """
      |</div>
      |<script src="../js/jquery-3.2.1.min.js"></script>
      |<script src="../js/popper.js"></script>
      |<script src="../js/bootstrap.min.js"`></script>
      |<script type="text/javascript" src='../js/probability.js'></script>
      |<script type="text/javascript">
      |      Illustrations.main();
      |    </script>
      |
      |  </body>
      |</html>
    """.stripMargin

  /**
    * regular expression for simple definitions
    */
  val defReg: Regex =
    "(\\\\def|\\\\newcommand|\\\\renewcommand)(\\\\[a-zA-Z0-9]+)([^\n%]+)".r

  /**
    * regular expression for new commands
    */
  val newCommReg: Regex = "(\\\\newcommand\\{)(\\\\[a-zA-Z0-9]+)(\\}[^\n%]+)".r

  /**
    * regular expression for `renewcommand`
    */
  val renewCommReg: Regex =
    "(\\\\renewcommand\\{)(\\\\[a-zA-Z0-9]+)(\\}[^\n%]+)".r

  /**
    * remove latex comments from a line, unless the line is meant to mark a div to insert javascript
    * TODO have a more uniform marker, say `!!!` in line
    * @param l the original line
    * @return the trimmed line
    */
  def trimLine(l: String): String =
    if (l.startsWith("%"))
      if (l.trim.endsWith("</div>")) l.drop(1) else ""
    else
      """[^\\]%""".r
        .findFirstMatchIn(l)
        .map(m => m.before.toString + m.group(0).head)
        .getOrElse(l)

  val inputReg: Regex = """\\input\{([a-zA-Z0-9:+-_' ]+)\}""".r

  /**
    * read rex file recursively including input tex files
    * @param name filename without extension
    * @return the body of the file
    */
  def readTeXFile(name: String): String = {
    val source = read(wd / s"$name.tex")
    val fullSource = inputReg.replaceAllIn(source, m => {
      pprint.log(s"including ${m.group(1)}")
      val include = readTeXFile(m.group(1))
      pprint.log("include read")
      Regex.quoteReplacement(include)
    })
    fullSource.split("\n").map(trimLine).mkString("", "\n", "\n")
  }

  /**
    * sources, placed in resources for automating rebuilds
    */
  val wd: Path = pwd / 'sitebuilder / "resources"

  /**
    * bunch of replacements
    * @param t original string
    * @param rpl sequence of replacements
    * @return string with replacements
    */
  def replaceAll(t: String)(rpl: Seq[(String, String)]): String =
    rpl.foldLeft(t) { case (txt, (x, y)) => txt.replace(x, y) }

  /**
    * replacements to make in tex file before parsing
    */
  val replacements = Seq(
    ("<", " < "),
    (">", " > "),
    (" >  < /div >", "></div>"),
    ("< div", "<div"),
    ("~", "&nbsp;"),
    ("\\newpage", ""),
    ("\\vspace{4mm}", ""),
    ("\\vspace{2mm}", ""),
    ("""\"{o}""", "&ouml;"),
    ("}}", "} }"),
    ("""``""", "''"),
    ("\\medskip", "<p></p>"),
    ("\\bigskip", "<p></p>"),
    ("\\;", " "),
    ("\\ ", " ")
  )

  /**
    * class for converting from latex file to html
    * @param name filename for the latex file
    * @return class with conversions
    */
  def teXConvertor(name: String): TeXToHtml = {
    val fullText: String = readTeXFile(name)

    val Array(header: String, textPadded: String) =
      fullText.split("\\\\begin\\{document\\}")

    val text: String =
      replaceAll(
        textPadded
          .split("""\\maketitle""")
          .last
          .split("""\\end\{document\}""")
          .head)(replacements)

    new TeXToHtml(header, text)
  }

  /**
    * regular expression for beginning environment, including labels
    */
  val fullBegReg: Regex =
    """\\begin\{([a-zA-Z0-9\*]*)\}([\s]*\[[^\[\]]+\])?([\s]*\\label\{[a-zA-Z0-9:+-_' ]+\})?""".r

  val labelReg: Regex = """\\label\{([a-zA-Z0-9:+-_' ]+)\}""".r

  val endReg: Regex = """\\end\{([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r

  val dolReg: Regex = "\\$".r

  val doldolReg: Regex = "\\$\\$".r

  val begItReg: Regex = """\\begin\{itemize\}""".r

  val endItReg: Regex = """\\end\{itemize\}""".r

  val begEnReg: Regex = """\\begin\{enumerate\}""".r

  val endEnReg: Regex = """\\begin\{enumerate\}""".r

  val blankLineReg: Regex = "\n[ \t]*\n[\\s]*".r

  def maxOpt[B: Ordering](v: Vector[B]): Option[B] =
    if (v.isEmpty) None else Some(v.max)

  /**
    * environments numbered as theorems and made into panels
    */
  val thmEnvs: Map[String, (String, String)] = Map(
    "example" -> ("Example", "info"),
    "question" -> ("Question", "danger"),
    "definition" -> ("Definition", "primary"),
    "remark" -> ("Remark", "success"),
    "exercise" -> ("Exercise", "warning"),
    "lemma" -> ("Lemma", "primary"),
    "problem" -> ("Problem", "info"),
    "proposition" -> ("Proposition", "primary"),
    "theorem" -> ("Theorem", "primary")
  )

  /**
    * environments to be wrapped in display math so MathJax processes
    */
  val mathEnvs = Set(
    "align*",
    "align",
    "equation*",
    "equation",
    "eqnarray",
    "eqnarray*"
  )

  /**
    * environment to be replaced by `div` without special styling
    */
  val divEnvs = Set("proof")

  def indices(r: Regex, txt: String): Vector[Int] =
    r.findAllMatchIn(txt).toVector.map(_.start)

  def itemizeBegs(txt: String): Vector[Int] = indices(begItReg, txt)

  def itemizeEnds(txt: String): Vector[Int] = indices(endItReg, txt)

  def enumerateBegs(txt: String): Vector[Int] = indices(begEnReg, txt)

  def enumerateEnds(txt: String): Vector[Int] = indices(begEnReg, txt)

  /**
    * inidices of latex items
    * @param txt the text
    * @return vector of indices
    */
  def items(txt: String): Vector[Int] = indices("""\\item""".r, txt)

  /**
    * whether an item is the first in its environment
    * @param j index of the item
    * @param txt full text
    * @return boolean
    */
  def firstItem(j: Int, txt: String): Boolean = {
    val lastBeg = (enumerateBegs(txt) ++ itemizeBegs(txt)).filter(_ < j).max
    maxOpt(items(txt).filter(_ < j)).forall(_ < lastBeg)

  }

  /**
    * replace latex items by html items
    * @param txt the full text
    * @return partially html conversion
    */
  def replaceItems(txt: String): String = {
    val itemLess =
      """\\item""".r.replaceAllIn(
        txt,
        m => if (firstItem(m.start, txt)) "<li>" else "</li>\n<li>")
    itemLess
      .replace("""\begin{itemize}""", "<ul>")
      .replace("""\end{itemize}""", "</ul>")
      .replace("""\begin{enumerate}""", "<ol>")
      .replace("""\end{enumerate}""", "</ol>")
      .replace("""\begin{inparaenum}[(a)]""", "<ol>")
      .replace("""\begin{inparaenum}""", "<ol>")
      .replace("""\end{inparaenum}""", "</ol>")
  }

  def recRplBegins(
      txt: String,
      head: String = "",
      thmCounter: Int = 0,
      labels: Map[String, Int] = Map()): (String, Int, Map[String, Int]) = {
    fullBegReg
      .findFirstMatchIn(txt)
      .map { m =>
        val title = Option(m.group(2))
        val labelOpt =
          Option(m.group(3)).map(_.trim.drop("""\label{""".length).dropRight(1))
        val newCounter =
          if (thmEnvs.keySet.contains(m.group(1))) thmCounter + 1
          else thmCounter
        val newLabels =
          if (thmEnvs.keySet.contains(m.group(1)))
            labelOpt.map(l => labels + (l -> newCounter)).getOrElse(labels)
          else labels
        val newString =
          if (thmEnvs.keySet.contains(m.group(1)))
            s"""<p>&nbsp;</p>
                <div id="theorem-$newCounter"><div class="panel panel-${thmEnvs(
                 m.group(1))._2} ${m
                 .group(1)}"> <div class="panel-heading">${thmEnvs(m.group(1))._1} $newCounter ${title
                 .map(s => "(" + s.drop(1).dropRight(1) + ")")
                 .getOrElse("")}</div><div class="panel-body">""".stripMargin
          else if (mathEnvs.contains(m.group(1)))
            "$$" + m.group(0)
          else if (divEnvs.contains(m.group(1)))
            s"""<div class="${m.group(1)}">"""
          else Regex.quoteReplacement(m.group(0))
        recRplBegins(m.after.toString,
                     head + m.before.toString + newString,
                     newCounter,
                     newLabels)
      }
      .getOrElse((head + txt, thmCounter, labels))
  }

  def labelTags(t: String): Vector[(String, String)] =
    labelReg.findAllMatchIn(t).map{
      m => m.group(1).toString
    }.toVector.zipWithIndex.map{
      case(l, n) => s"\\label{$l}" -> s"\\label{$l}\\tag{${n + 1}}"
    }

  def addTags(t: String): String = {
    pprint.log(labelTags(t))
    replaceAll(t)(labelTags(t))
  }

  def rplBegins(txt: String): (String, Map[String, Int]) =
    (recRplBegins(txt)._1
       .replace("""\\begin""", """\begin""")
       .replace("""\$""", "$"),
     recRplBegins(txt)._3)

  def replaceEnds(txt: String): String =
    endReg.replaceAllIn(
      txt,
      m =>
        if (mathEnvs.contains(m.group(1)))
          Regex.quoteReplacement(m.group(0) + "$$")
        else if (thmEnvs.keySet.contains(m.group(1))) "</div></div></div>"
        else if (divEnvs.contains(m.group(1))) "</div>"
        else Regex.quoteReplacement(m.group(0))
    )

  def recReplaceSubSection(txt: String,
                           head: String,
                           counter: Int,
                           section: Int): String =
    """(\\subsection\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .findFirstMatchIn(txt)
      .map { m =>
        val newHead =
          head + blockParas(m.before.toString) + s"""<h3>$section.${counter + 1} ${m
            .group(2)}.</h3>"""
        recReplaceSubSection(m.after.toString, newHead, counter + 1, section)
      }
      .getOrElse(head + blockParas(txt))

  def recReplaceSection(txt: String,
                        head: String = "",
                        counter: Int = 0,
                        secs: Map[Int, String] = Map(),
                        chapters: Map[Int, String] = Map())
    : (String, Map[Int, String], Map[Int, String]) =
    """(\\section\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .findFirstMatchIn(txt)
      .map { (m) =>
        val title = m.group(2).toString
        val prev = recReplaceSubSection(m.before.toString, "", 0, counter)
        val newHead =
          s"""$head$prev</section>
             <section><h2 id="section-${counter + 1}">${counter + 1}. $title.</h2>""".stripMargin
        recReplaceSection(
          m.after.toString,
          newHead,
          counter + 1,
          secs + ((counter + 1) -> title),
          chapters + (counter -> prev)
        )
      }
      .getOrElse((head + txt, secs, chapters + (counter -> txt)))

  val bfReg1: Regex = "(\\{\\\\bf)([^\\}]+)\\}".r

  val bfReg2: Regex = "(\\{\\\\bf\\{)([^\\}]+)\\}\\}".r

  def replaceBf(txt: String): String = {
    bfReg1.replaceAllIn(
      txt,
      (m) =>
        if (dolReg
              .findAllIn(m.before + " ")
              .size % 2 == 0 && doldolReg.findAllIn(m.before).size % 2 == 0)
          Regex.quoteReplacement(s"<strong>${m.group(2)}</strong>")
        else s"\\{\\\\bf ${m.group(2)}\\}"
    )
  }

  /**
    * purges a regular expression when it is not it a mathematics environment
    * @param r the regex to purge
    * @param txt the source text
    * @return purged text
    */
  def purge(r: Regex)(txt: String): String =
    r.replaceAllIn(
      txt,
      (m) =>
        if (dolReg
              .findAllIn(m.before + " ")
              .size % 2 == 0 && doldolReg.findAllIn(m.before).size % 2 == 0
            && """\\\[""".r
              .findAllIn(m.before)
              .size == """\\\]""".r.findAllIn(m.before).size)
          ""
        else m.group(0).toString
    )

  val emReg: Regex = "(\\{\\\\em[^a-zA-Z])([ \\\\])([^\\}]+)\\}".r

  def replaceUnderline(txt: String): String =
    """(\\underline\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .replaceAllIn(
        txt,
        (m) =>
          if (dolReg
                .findAllIn(m.before + " ")
                .size % 2 == 0 && doldolReg.findAllIn(m.before).size % 2 == 0)
            Regex.quoteReplacement(s"<u>${m.group(2)}</u>")
          else
            s"\\\\underline\\{${m.group(2).replace("\\", "\\\\")}\\}"
      )

  val tinyReg: Regex = "(\\{\\\\tiny )([^\\}]+)\\}".r

  def replaceTiny(txt: String): String =
    tinyReg.replaceAllIn(txt,
                         (m) => Regex.quoteReplacement(s"""{${m.group(2)}}"""))

  def bracesMatched(txt: String): Boolean = {
    val opens = """\{""".r.findAllIn(txt).length
    val closes = """\}""".r.findAllIn(txt).length
    opens == closes
  }

  def block(txt: String, head: String, secHead: String): (String, String) = {
    blankLineReg
      .findFirstMatchIn(txt)
      .map { (m) =>
        if (doldolReg.findAllIn(secHead + head + m.before).size % 2 == 0)
          (head + m.before, m.after.toString)
        else {
          block(m.after.toString, head + m.before.toString + "\n", secHead)
        }
      }
      .getOrElse(head + txt, "")
  }

  def blockParas(txt: String, head: String = ""): String = {
    blankLineReg
      .findFirstMatchIn(txt)
      .map { (m) =>
        val (blk, rest) = block(m.after.toString, "", head + m.before.toString)
        val break =
          if (doldolReg.findAllIn(m.before.toString).size % 2 == 0)
            """</p>
            |<p class="text-justify">
          """.stripMargin
          else ""
        val newHead =
          s"""
             |$head
             | <p class="text-justify">
             |${m.before}
             | $break
             | $blk
             | </p>
           """.stripMargin
        blockParas(rest, newHead)
      }
      .getOrElse(head + txt)
  }

  def inBraces(txt: String, head: String): (String, String) = {
    """([^\\])(\})""".r
      .findFirstMatchIn(txt)
      .map { (m) =>
        if (bracesMatched(head + m.before + m.group(1))) {
          (head + m.before.toString + m.group(1).toString, m.after.toString)
        } else {
          inBraces(m.after.toString, head + m.before.toString + m.group(0))
        }
      }
      .getOrElse(throw new Exception("Unclosed brace for footnote"))
  }

  def recReplaceFootnotes(txt: String,
                          head: String = "",
                          counter: Int = 0): String =
    """\\footnote\{""".r
      .findFirstMatchIn(txt)
      .map { (m) =>
        val (note, rest) = inBraces(m.after.toString, "")
        val newHead =
          head + m.before + s"""<sup> <a data-toggle="collapse" href="#footnote-${counter + 1}" aria-expanded="false" aria-controls="footnote-${counter + 1}">
                                ${counter + 1}
                                 </a> </sup><span class="collapse small" id="footnote-${counter + 1}">$note</span> """.stripMargin
        recReplaceFootnotes(rest, newHead, counter + 1)
      }
      .getOrElse(head + txt)

  def recReplacePara(txt: String, head: String = ""): String =
    """\\para\{""".r
      .findFirstMatchIn(txt)
      .map { (m) =>
        val (header, rest) = inBraces(m.after.toString, "")
        val newHead =
          head + m.before + s"<strong>$header :</strong>"
        recReplacePara(rest, newHead)
      }
      .getOrElse(head + txt)

  def recReplaceParag(txt: String, head: String = ""): String =
    """\\parag\{""".r
      .findFirstMatchIn(txt)
      .map { (m) =>
        val (header, rest) = inBraces(m.after.toString, "")
        val newHead =
          head + m.before + s"<strong>$header</strong>"
        recReplaceParag(rest, newHead)
      }
      .getOrElse(head + txt)

  def recReplaceEm(txt: String, head: String = ""): String =
    """\{\\em[^a-zA-Z]""".r
      .findFirstMatchIn(txt)
      .map { (m) =>
        val (header, rest) =
          inBraces(m.group(0).toString.takeRight(1) + m.after.toString, "")
        val newHead =
          head + m.before + s"<em>$header</em>"
        recReplaceEm(rest, newHead)
      }
      .getOrElse(head + txt)

  def recReplaceMagenta(txt: String, head: String = ""): String =
    """\{\\color\{[a-zA-Z]+\}""".r
      .findFirstMatchIn(txt)
      .map { (m) =>
        val (header, rest) = inBraces(m.after.toString, "")
        val newHead =
          head + m.before + header
        recReplaceMagenta(rest, newHead)
      }
      .getOrElse(head + txt)
}

class TeXToHtml(header: String, text: String) {
  import TeXToHtml._

  val defs: Vector[Regex.Match] = defReg.findAllMatchIn(header).toVector

  val newCommands: Vector[Regex.Match] =
    newCommReg.findAllMatchIn(header).toVector

  val renewCommands: Vector[Regex.Match] = renewCommReg
    .findAllMatchIn(header)
    .toVector

  val defSubs: Vector[(String, String)] =
    (defs.map((m) => m.group(2) -> m.group(3).trim.drop(1).dropRight(1)) ++
      (newCommands ++ renewCommands).map((m) =>
        m.group(2) -> m.group(3).trim.drop(2).dropRight(1))).filterNot((c) =>
      Set("\\I", "\\matrices", "\\para", "\\parag").contains(c._1))

  def defReplace(txt: String): String = defSubs.foldLeft[String](txt) {
    case (t, (x, y)) =>
      new Regex(x.replace("\\", "\\\\") + "([^a-zA-Z0-9])")
        .replaceAllIn(t, (m) => Regex.quoteReplacement(y + m.group(1)))
  }

  def recDefReplace(txt: String): String = {
    val next = defReplace(txt)
    if (next == txt) next else recDefReplace(next)
  }

  lazy val defReplaced: String =
    recDefReplace(text).replace("""\noindent""", "").replace("\\textrm", "")

  lazy val baseReplaced: String =
    replaceEnds(
      replaceTiny(
        recReplaceMagenta(recReplaceEm(
          recReplacePara(recReplaceParag(replaceItems(defReplaced)))))))

  lazy val (begReplaced: String, labels: Map[String, Int]) = rplBegins(
    baseReplaced)

  lazy val refReplaced: String =
    """\\ref\{([a-zA-Z0-9:+-_]+)\}""".r.replaceAllIn(
      begReplaced,
      (m) =>
        labels
          .get(m.group(1))
          .map((n) => s"""<a href="#theorem-$n">$n</a>""")
          .getOrElse(m.group(0)))

  lazy val (secReplaced: String,
            sections: Map[Int, String],
            chapters: Map[Int, String]) = recReplaceSection(refReplaced)

  lazy val theoremChapters: Map[Int, Int] = {
    val regex = """(id="theorem-)([0-9]+)""".r
    chapters.toVector.flatMap {
      case (n, txt) =>
        (regex findAllMatchIn txt).map((m) => m.group(2).toInt -> n).toVector
    }
  }.toMap

  def refChapters(txt: String): String = {
    """(href="#theorem-)([0-9]+)""".r
      .replaceAllIn(txt, (m) => {
        val j = m.group(2).toInt
        s"""href="chapter-${theoremChapters(j)}.html#theorem-$j"""
      })
  }

  lazy val allReplaced: String =
    // purge("""[\{\}]""".r)
    addTags(
      recReplaceFootnotes(replaceUnderline(replaceBf(secReplaced))))

  lazy val chapReplaced: Map[Int, String] =
    chapters.filter(_._1 > 0).mapValues { (chapter) =>
      addTags(purge("""[\{\}]""".r)(
        recReplaceFootnotes(replaceUnderline(replaceBf(refChapters(chapter))))))
    }

  lazy val sortedSections: Vector[(Int, String)] =
    sections.toVector.sortBy(_._1)

  def sectionList(v: Vector[(Int, String)]): String =
    v.map {
        case (n, title) =>
          s"""<li><a href="#section-$n">$n. $title</a> </li>"""
      }
      .mkString("\n")

  def chapterList(v: Vector[(Int, String)]): String =
    v.map {
        case (n, title) =>
          s"""<li><a href="chapter-$n.html">$n. $title</a> </li>"""
      }
      .mkString("\n")

  def chapterMenu(v: Vector[(Int, String)]): String = {
    val ttl = s"Chapters ${v.head._1} to ${v.last._1}"
    s"""
    |<li class="dropdown">
    |<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"> $ttl <span class="caret"></span></a>
    |  <ul class="dropdown-menu">
    |     ${chapterList(v)}
    |  </ul>
    |</li>
    """.stripMargin
  }

  def tocList: String =
    sortedSections
      .map {
        case (n, title) =>
          s"""<li><a href="chapter-$n.html">$title</a> </li>"""
      }
      .mkString("\n")

  lazy val chapNav: String = nav(
    sortedSections.grouped(14).toVector.map(chapterMenu) : _*
    // chapterMenu(sortedSections.take(12)),
    //                              chapterMenu(sortedSections.slice(12, 27)),
    //                              chapterMenu(sortedSections.drop(27))
                               )

  // lazy val draftHtml: String = top + nav(
  //   sectionList(sortedSections.take(12)),
  //   sectionList(sortedSections.slice(12, 27)),
  //   sectionList(sortedSections.drop(27))) + banner + allReplaced + foot

  def chapLink(n: Int): String =
    sections
      .get(n)
      .map { (t) =>
        s"""<div class="pull-right"><a href="chapter-$n.html" class="btn btn-primary">Chapter $n. $t</a></div>"""
      }
      .getOrElse("")

  lazy val chapHtml: Map[Int, String] = chapReplaced.map {
    case (n, txt) =>
      (n,
       s"""
         |$top
         |<div class="container-fluid">
         |<div class="banner">
         |<h1 class="text-center bg-primary">Chapter $n : ${sections(n)}</h1>
         |</div>
         |</div>
         |$chapNav
         |<div class="container">
         |$txt
         |
         |${chapLink(n + 1)}
         |<p>&nbsp;</p>
         |<p>&nbsp;</p>
         |<p>&nbsp;</p>
         |<p>&nbsp;</p>
         |<p>&nbsp;</p>
         |$foot""".stripMargin)
  }

  lazy val tocHtml: String =
    s"""
       |$top
       |$banner
       |$chapNav
       |<h1 class="text-center bg-primary">Table of Contents</h1>
       |<p>&nbsp;</p>
       |<ol>
       |$tocList
       |</ol>
       |<p>&nbsp;</p>
       |<p>&nbsp;</p>
       |$foot""".stripMargin

  // def writeFull(target: String) =
  //   write.over(pwd / "docs" / target / "index.html", draftHtml)

  def writeChaps(target: String): Unit = {
    chapHtml.foreach {
      case (n, html) =>
        write.over(pwd / "docs" / target / s"chapter-$n.html", html)
    }
    write.over(pwd / "docs" / target / "index.html", tocHtml)
  }

  def writeHtml(target: String): Unit = {
    // writeFull(fullTarget)
    writeChaps(target)
  }
}

object SiteBuild extends App {
  pprint.log("Converting notes")
  import TeXToHtml._
  val converter = teXConvertor("stat-and-prob")
  converter.writeHtml("notes")
  val js = read(resource / "out.js")
  write.over(pwd / "docs" / "js" / "probability.js", js)
  import Site._
  pprint.log("making static site")
  mkHome()
  mkAss()
  mkIllus()
  mkProblems()
  mkMidterm()
  mkRules()
}
