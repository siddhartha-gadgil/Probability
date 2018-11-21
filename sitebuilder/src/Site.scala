package probability

import ammonite.ops
import ops._

import scala.xml.Elem
import scala.util.Try

object Site {

  /**
    * inclusion for mathJax
    */
  val mathjax: String =
    """
      |<!-- mathjax config similar to math.stackexchange -->
      |<script type="text/x-mathjax-config">
      |MathJax.Hub.Config({
      |jax: ["input/TeX", "output/HTML-CSS"],
      |tex2jax: {
      |  inlineMath: [ ['$', '$'] ],
      |  displayMath: [ ['$$','$$'], ["\\[","\\]"] ],
      |  processEscapes: true,
      |  skipTags: ['script', 'noscript', 'style', 'textarea', 'pre', 'code']
      |},
      |messageStyle: "none",
      |"HTML-CSS": { preferredFont: "TeX", availableFonts: ["STIX","TeX"] }
      |});
      |</script>
      |<script type="text/javascript" async
      |      src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-MML-AM_CHTML">
      |       </script>
    """.stripMargin

  /**
    * (html) head of a page
    * @param relDocsPath relative path to the `docs` folder, which is the base of the site
    * @return head
    */
  def head(relDocsPath: String): String =
    s"""
       |<head>
       |    <meta charset="utf-8">
       |    <meta http-equiv="X-UA-Compatible" content="IE=edge">
       |    <meta name="viewport" content="width=device-width, initial-scale=1">
       |    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
       |    <title>MA 261: Probability Models</title>
       |    <link rel="icon" href="${relDocsPath}IIScLogo.jpg">
       |
       |    <!-- Bootstrap -->
       |    <link href="${relDocsPath}css/bootstrap.min.css" rel="stylesheet">
       | <link href="${relDocsPath}css/extras.css" rel="stylesheet">
       |   <link href="${relDocsPath}css/katex.min.css" rel="stylesheet">
       |   <script src="${relDocsPath}js/katex.min.js"></script>
       |
       |    $mathjax
       |  </head>
       |
   """.stripMargin

  /**
    * navigation bar
    * @param relDocsPath relative path to the `docs` folder, which is the base of the site
    * @return navigation bar
    */
  def nav(relDocsPath: String = ""): Elem =
    <nav class="navbar navbar-default">
      <div class="container-fluid">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <span class="navbar-brand">Instructor: <a href="http://math.iisc.ac.in/~gadgil" target="_blank">Siddhartha Gadgil</a></span>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
          <ul class="nav navbar-nav" id="left-nav">
            <li><a href={s"${relDocsPath}index.html"}>Home</a></li>
            <li class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                Assignments<span class="caret"></span></a>
              <ul class="dropdown-menu">
                {assList(relDocsPath)}
              </ul>
            </li>
            <li class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                Illustrations<span class="caret"></span></a>
              <ul class="dropdown-menu">
                {illusList(relDocsPath)}
              </ul>
            </li>
          </ul>
          <ul class="nav navbar-nav navbar-right">
            <li> <a href={s"${relDocsPath}prob-problems.html"}>Problems</a></li>
            <li> <a href={s"${relDocsPath}notes/index.html"} target="_blank">Notes</a></li>
          </ul>
        </div><!-- /.navbar-collapse -->
      </div><!-- /.container-fluid -->
    </nav>

  /**
    * foot of a page, not including closing the body
    * @param relDocsPath relative path to the `docs` folder, which is the base of the site
    * @return text for foot
    */
  def foot(relDocsPath: String): String =
    s"""
       |<div class="container-fluid">
       |  <p>&nbsp;</p>
       |  <p>&nbsp;</p>
       |  <p>&nbsp;</p>
       | <div class="footer navbar-fixed-bottom navbar-default">
       |  <h4> &nbsp;<a href="http://math.iisc.ac.in" target="_blank">&nbsp; Department of Mathematics,</a>
       | &nbsp;<a href="http://iisc.ac.in" target="_blank">Indian Institute of Science.</a></h4>
       | </div>
       |</div>
       |<script type="text/javascript" src="${relDocsPath}js/jquery-2.1.4.min.js"></script>
       |<script type="text/javascript" src="${relDocsPath}js/bootstrap.min.js"></script>
       |<script type="text/javascript" src="${relDocsPath}js/probability.js"></script>
       |<script>
       |  Illustrations.main()
       |</script>
   """.stripMargin

  /**
    * whether a line is `---`
    * @param s line
    * @return
    */
  def threeDash(s: String): Boolean = s.trim == "---"

  /**
    * whether a file has top matter
    * @param l lines in the file
    * @return boolean
    */
  def withTop(l: Vector[String]): Boolean =
    (l.count(threeDash) == 2) && threeDash(l.head)

  /**
    * body of the file, excluding topmatter
    * @param l lines of a file
    * @return body
    */
  def body(l: Vector[String]): Vector[String] =
    if (withTop(l)) l.tail.dropWhile(l => !threeDash(l)).tail else l

  /**
    * top matter in YAML, if any
    * @param lines lines of the file
    * @return optional top matter lines
    */
  def topmatter(lines: Vector[String]): Option[Vector[String]] =
    if (withTop(lines)) Some(lines.tail.takeWhile(l => !threeDash(l)))
    else None

  /**
    * title if present in top matter
    * @param l lines of a file
    * @return optional title
    */
  def titleOpt(l: Vector[String]): Option[String] =
    for {
      tm <- topmatter(l)
      ln <- tm.find(_.startsWith("title: "))
    } yield ln.drop(6).trim

  /**
    * escaped filename, lowercase with hyphens not spaces
    * @param s raw string
    * @return
    */
  def filename(s: String): String = s.toLowerCase.replaceAll("\\s", "-")

  /**
    * date if it is part of topmatter
    * @param l lines of a file
    * @return
    */
  def dateOpt(l: Vector[String]): Option[(Int, Int, Int)] =
    for {
      tm <- topmatter(l)
      m <- """date: (\d\d\d\d)-(\d\d)-(\d\d)""".r.findFirstMatchIn(
        tm.mkString("\n"))
    } yield (m.group(1).toInt, m.group(2).toInt, m.group(3).toInt)

  /**
    * months of the year
    */
  val months = Vector("Jan",
                      "Feb",
                      "Mar",
                      "Apr",
                      "May",
                      "Jun",
                      "Jul",
                      "Aug",
                      "Sep",
                      "Oct",
                      "Nov",
                      "Dec")

  /**
    * Assignment
    * @param name filename
    * @param content the main body
    * @param optDate date if part of top-matter
    * @param optTitle title if part of top-matter
    */
  case class Assignment(name: String,
                        content: String,
                        optDate: Option[(Int, Int, Int)],
                        optTitle: Option[String]) {

    val title: String = optTitle.getOrElse(name)

    val dateString: String =
      optDate.map { case (y, m, d) => s"${months(m - 1)} $d, $y" }.getOrElse("")

    /**
      * file onto which to write
      */
    val target: Path = pwd / "docs" / "assignments" / s"$name.html"

    /**
      * url of the file, for menus
      * @param relDocsPath relative path to the `docs` folder, which is the base of the site
      * @return
      */
    def url(relDocsPath: String) = s"${relDocsPath}assignments/$name.html"

    val date: (Int, Int, Int) = optDate.getOrElse((0, 0, 0))

    /**
      * html body for the assignment
      */
    val assContent: String =
      s"""
      <h2 class="text-center"> $title </h2>
      <h4 class="text-center"> due by $dateString </h4>
      $content
      """

    /**
      * the final output
      * @return
      */
    def output: String =
      page(assContent.toString, "../")

    def save(): Unit = write.over(target, output)
  }

  def getAss(p: Path): Assignment = {
    val l = ops.read.lines(p).toVector
    val name =
      titleOpt(l).map(filename).getOrElse(p.name.dropRight(p.ext.length + 1))
    val content = body(l).mkString("\n")
    Assignment(name, content, dateOpt(l), titleOpt(l))
  }

  val assDir: Path = pwd / "sitebuilder" / "resources" / "assignments"

  /**
    * collection of assignments
    */
  lazy val allAss: Seq[Assignment] =
    Try(ls(assDir).map(getAss).sortBy(_.date).reverse).getOrElse(Seq())

  /**
    * list of assignments to be used in menus
    * @param relDocsPath relative path to the `docs` folder, which is the base of the site
    * @return html list
    */
  def assList(relDocsPath: String): Seq[Elem] =
    allAss.map(
      ass => <li><a href={s"${ass.url(relDocsPath)}"}>{ass.title}</a></li>
    )

  /**
    * map of illustrations
    */
  val allIllus =
    Vector(
      "Fair Coin?" -> "fair-coin",
      "Repeated Tosses" -> "coin-tosses",
      "Birthday Paradox" -> "birthdays",
      "Percolation" -> "percolation",
      "Bayes rule for Coins" -> "bayes-coin",
      "Dependent tosses" -> "dependent-tosses",
      "Cantor Distribution" -> "cantor",
      "Markov Process" -> "markov"
    )

  /**
    * list of illustrations to be used in menus
    * @param relDocsPath relative path to the `docs` folder, which is the base of the site
    * @return html list
    */
  def illusList(relDocsPath: String): Vector[Elem] =
    allIllus.map {
      case (name, tag) =>
        <li><a href={s"${relDocsPath}illustrations/$tag.html"}>{name}</a></li>
    }

  /**
    * make a page
    * @param s the body
    * @param relDocsPath relative path to the `docs` folder, which is the base of the site
    * @return complete html for page
    */
  def page(s: String, relDocsPath: String): String =
    s"""
       |<!DOCTYPE html>
       |<html lang="en">
       |${head(relDocsPath)}
       |<body>
       |${nav(relDocsPath)}
       |<div class="container">
       |
       |$s
       |
       |</div>
       |${foot(relDocsPath)}
       |</body>
       |</html>
   """.stripMargin

  /**
    * list of assignments
    */
  val assList: Seq[Elem] = allAss.map(ass =>
    <li> <a href={ass.url("")}> {ass.title}</a>, due by {ass.dateString}.</li>)

  /**
    * body of the course home page
    */
  val home: Elem =
    <section>
  <div class="col-md-9">

      <h1> MA 261: Probability Models </h1>
    <div id="desc" class="section">
      <h3> Course Syllabus </h3>
        <p>
          Sample spaces, events, probability, discrete and continuous random variables,
          Conditioning and independence, Bayes  formula, moments and moment generating function, characteristic function,
          laws of large numbers, central limit theorem, Markov chains, Poisson processes.
        </p>
    </div>
    <div class="section" id ="Assignments">
      <h3> <a href="assign-all.html">Assignments</a> </h3>
      <ul>
        {assList}
      </ul>
    </div>
    <div class="section">
      <h3>Grading</h3>
      <ul> There will be one <a href="midterm.html"> midterm</a> and a <a href="final.html">final</a> examination. <strong> Please note the <a href="rules.html">rules</a> for the examinations.  </strong>
        The weightages in the final grades are as follows:
        <li> Assignments: 10%.</li>
        <li> Midterm: 40%.</li>
        <li> Final: 50%.</li>
      </ul>
    </div>
    <div id="refs" class="section">
      <h3>Suggested books</h3>
      <ol>
        <li> Ross, S.M. , Introduction to Probability Models ,Academic Press 1993.</li>
        <li> Taylor, H.M., and Karlin, S., An Introduction to Stochastic Modelling ,Academic Press, 1994. </li>
        <li> Feller, W. An Introduction to Probability Theory and Its Applications, Volume 1, Wiley 1968.</li>
      </ol>
    </div>
    <div id ="details" class="section">
      <h3> Course Details</h3>
      <ul>
        <li><strong>Instructor:</strong> <a href="http://math.iisc.ac.in/~gadgil" target="_blank"> Siddhartha Gadgil</a></li>
        <li><strong>E-mail:</strong> <a href="mailto:siddhartha.gadgil@gmail.com" target="_blank"> siddhartha.gadgil@gmail.com</a>.</li>
        <li> <strong>Office:</strong> N-15, Department of Mathematics. </li>
        <li><strong>Lecture Timings: </strong> Monday, Wednesday, Friday : 9:00 am - 10:00 am. </li>
        <li><strong>Lecture Venue: </strong> LH-1 (if available) or LH-5, Department of Mathematics.</li>
        <li><strong> Teaching Assistants: </strong>
          <ul>
            <li>
               Srikanth Srimala, <strong>e-mail:</strong> <a href="mailto:srikanths@iisc.ac.in" target="_blank">srikanths@iisc.ac.in</a>
            </li>
            <li>
              Somnath Pradhan, <strong>e-mail:</strong> <a href="mailto:somnathp@iisc.ac.in" target="_blank">somnathp@iisc.ac.in</a>
            </li>

          </ul>
        </li>
        <li>
          <strong>Tutorial Timings:</strong> Friday : 5:30 pm - 6:30 pm.
        </li>
        <li><strong>Tutorial Venue: </strong> LH-4, Department of Mathematics.</li>

      </ul>
    </div>
  </div>

  <div class="col-md-3 section" id ="Alerts">
    <h4>Announcements</h4>
    <ul>
      <li> <a href="midterm.html"> Midterm Examination</a> on Fri, 28/9/2018 (updated) </li>
      <li> <a href="final.html"> Final Examination</a> on Fri, 30/11/2018  </li>
    </ul>
    <h4> <a href="assign-all.html">Upcoming Assignments</a> </h4>
    <ul>
      {allAss.map(ass =>
      <li>
        <a href={ass.url("")}> {ass.title}</a>, due by {ass.dateString}.
      </li>)
    }
    </ul>

  </div>

  </section>

  /**
    * body of page listing all assignments
    */
  val assignAll: Elem =
    <section>
      <h2> Assignments </h2>
      <ul>
        {assList}
      </ul>
    </section>

  /**
    * write files for all assignments and the listing page
    */
  def mkAss(): Unit = {
    allAss.foreach { ass =>
      pprint.log(s"saving assignment ${ass.name} due on ${ass.dateString}")
      ass.save()
    }
    write.over(pwd / "docs" / "assign-all.html", page(assignAll.toString, ""))
  }

  /**
    * write files for illustrations
    */
  def mkIllus(): Unit = {
    allIllus.foreach {
      case (name, tag) =>
        pprint.log(s"writing illustration $name")
        write.over(
          pwd / "docs" / "illustrations" / s"$tag.html",
          page(s"""<div id="$tag"></div>""", "../")
        )
    }
  }

  /**
    * write the file for the home page
    */
  def mkHome(): Unit =
    write.over(pwd / "docs" / "index.html", page(home.toString, ""))

  lazy val probProblemsBody: String =
    TeXToHtml.teXConvertor("Probabilityproblemlist").allReplaced

  val probTop =
    """<h2 class="text-center">Probability problems</h2>
    <h3 class="text-center"> by Manjunath Krishnapur</h3>
    <div class="text-right"><a href="Probabilityproblemlist.pdf">PDF version</a></div>
    """

  lazy val probPage: String = page(probTop + probProblemsBody, "")

  def mkProblems(): Unit =
    write.over(pwd / "docs" / "prob-problems.html", probPage)

  val finalExam: Elem =
    <div>
    <h2 class="text-center"> Final Examination</h2>
      <p> The final examination is scheduled as follows:</p>
      <ul>
        <li><strong>Date:</strong> Friday, November 30, 2018.</li>
        <li><strong>Time:</strong> 9:00 am - 12:00 noon</li>
        <li><strong>Venue:</strong> LH-1, Department of Mathematics, IISc </li>
      </ul>
      <h3> Syllabus etc</h3>
      <p>
        <strong>
          Please note the <a href="rules.html">rules</a> for the examination.
        </strong>
      </p>
      <p>The syllabus includes all the material covered in the course. However, most of the weightage will be for topics in the second half of the course. A rough list of topics is below.</p>
      <ul>
        <li><strong>Probablitity</strong>
          <ul>
            <li>Discrete Probability spaces; laws of Probability; combinations of Events. </li>
          <li> Independence and Conditional Probabilities. </li>
          <li> Discrete and Continuous distributions;
            some important distributions: Bernoulli, Binomial, Geometric, Exponential, Cauchy, Poisson.</li>
          <li> Random variables: Distributions, Independence, Expectation and Variance;
            conditional distributions and conditional expectation.</li>
          </ul>
        </li>
        <li><strong>Markov Chains</strong>
        <ul>
          <li> Classification of States: accesibility, communicating classes, essential states, irreducibility.</li>
          <li> Transience and recurrence; criteria for recurrence.</li>
          <li> Null and positive recurrence; stationary distributions.</li>
          <li> Random walks on integers and tuples of integers: symmetric and assymmetric.</li>
        </ul></li>
        <li><strong>Poisson Process</strong>
          <ul>
            <li> Counting processes; charactrization of the Poisson process.</li>
            <li> Associated distributions: Exponential, Poisson, Gamma.</li>
            <li> Thinning and superposition.</li>
          </ul>
        </li>
      </ul>
    </div>

  val midterm: Elem =
    <div>
      <h2 class="text-center"> Midterm Examination</h2>
      <p> The midterm examination was scheduled as follows:</p>
      <ul>
        <li><strong>Date:</strong> Friday, September 28, 2018.</li>
        <li><strong>Time:</strong> 8:30 am - 10:00 am</li>
        <li><strong>Venue:</strong> LH-1, Department of Mathematics, IISc </li>
      </ul>
      <h3>Distribution of scores in the midterm</h3>
      <iframe width="478" height="292" frameborder="0" scrolling="no"
              src="https://docs.google.com/spreadsheets/d/e/2PACX-1vSslrC0hE4JnahiPyHIQn1BMdnjHjPrsFKwkm4ez6mCSyQwEQRaLTm-ucGDl3hx7LEn8ec-9rjPRMD0/pubchart?oid=497977102&amp;format=interactive">
      </iframe>
      <h3> Answer scripts, queries etc.</h3>
      <p>
        If you have not seen your answer script or want to meet me (Siddhartha Gadgil) about any related issue, I will be available on Tuesday, October 9, 2019
        between 8:30 am and 11:20 am. in my office N-15, Department of Mathematics.
      </p>
      <h3> Syllabus etc</h3>
      <p>
        <strong>
        Please note the <a href="rules.html">rules</a> for the examination.
        </strong>
      </p>

    <p>
      The syllabus is the material covered in the lectures up to Wednesday, September 19, 2018.
      Topics for the midterm with  (roughly corresponding) chapters in the <a href="http://math.iisc.ac.in/~gadgil/MA261/notes/index.html" target="_blank">notes</a> are as follows
    </p>
    <ul>
         <li>Discrete Probability spaces (Chapters 2 and 3).</li>
        <li> Laws of Probability (Chapter 6). </li>
        <li> Combinations of Events (Chapters 8 and 9). </li>
        <li> Independence and Conditional Probabilities (Chapters 9 to 12). </li>
        <li> Discrete and Continuous distributions (Chapters 13 to 16).</li>
        <li> Some important distributions: Bernoulli, Binomial, Geometric, Exponential, Cauchy.</li>
        <li> Random variables: Distributions, Independence, Expectation and Variance (Chapters 18, 20, 21).</li>
    </ul>
    </div>

  def mkMidterm(): Unit =
    write.over(pwd / "docs" / "midterm.html", page(midterm.toString, ""))

  def mkFinal(): Unit =
    write.over(pwd / "docs" / "final.html", page(finalExam.toString, ""))

  val rules = read(pwd / "sitebuilder" / "resources" / "rules.html")

  def mkRules(): Unit = write.over(pwd / "docs" / "rules.html", page(rules, ""))
}
