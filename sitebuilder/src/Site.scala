package probability

import ammonite.ops
import ops._

import scala.xml.Elem
import scala.util.Try

object Site {


  val mathjax =
    """
      |<!-- mathjax config similar to math.stackexchange -->
      |<script type="text/x-mathjax-config">
      |MathJax.Hub.Config({
      |jax: ["input/TeX", "output/HTML-CSS"],
      |tex2jax: {
      |  inlineMath: [ ['$', '$'] ],
      |  displayMath: [ ['$$', '$$']],
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
            <li> <a href={s"${relDocsPath}notes/index.html"} target="_blank">Notes</a></li>
          </ul>
        </div><!-- /.navbar-collapse -->
      </div><!-- /.container-fluid -->
    </nav>

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


  def threeDash(s: String): Boolean = s.trim == "---"

  def withTop(l: Vector[String]): Boolean =
    (l.count(threeDash) == 2) && threeDash(l.head)

  def body(l: Vector[String]): Vector[String] =
    if (withTop(l)) l.tail.dropWhile((l) => !threeDash(l)).tail else l

  def topmatter(lines: Vector[String]): Option[Vector[String]] =
    if (withTop(lines)) Some(lines.tail.takeWhile((l) => !threeDash(l)))
    else None

  def titleOpt(l: Vector[String]): Option[String] =
    for {
      tm <- topmatter(l)
      ln <- tm.find(_.startsWith("title: "))
    } yield ln.drop(6).trim

  def filename(s: String): String = s.toLowerCase.replaceAll("\\s", "-")


  def dateOpt(l: Vector[String]): Option[(Int, Int, Int)] =
    for {
      tm <- topmatter(l)
      m <- """date: (\d\d\d\d)-(\d\d)-(\d\d)""".r.findFirstMatchIn(
        tm.mkString("\n"))
    } yield (m.group(1).toInt, m.group(2).toInt, m.group(3).toInt)

  val months = Vector("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

  case class Assignment(name: String,
                  content: String,
                  optDate: Option[(Int, Int, Int)],
                  optTitle: Option[String]) {
    val title: String = optTitle.getOrElse(name)

    val dateString: String =
      optDate.map { case (y, m, d) => s"${months(m-1)} $d, $y" }.getOrElse("")

    val target: Path = pwd / "docs" / "assignments" / s"$name.html"

    def url(relDocsPath: String) = s"${relDocsPath}assignments/$name.html"

    val date: (Int, Int, Int) = optDate.getOrElse((0, 0, 0))

    val assContent: String =
      s"""
      <h2 class="text-center"> $title </h2>
      <h4 class="text-center"> due by $dateString </h4>
      $content
      """

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

  def assDir: Path = pwd / "sitebuilder" / "resources" / "assignments"

  def allAss: Seq[Assignment] =
    Try(ls(assDir).map(getAss).sortBy(_.date).reverse).getOrElse(Seq())

  def assList(relDocsPath: String): Seq[Elem] =
      allAss.map(
        (ass) =>
          <li><a href={s"${ass.url(relDocsPath)}"}>{ass.title}</a></li>
      )

  val allIllus =
    Vector("Fair Coin?" -> "fair-coin",
      "Repeated Tosses" -> "coin-tosses",
      "Birthday Paradox" -> "birthdays",
      "Percolation" -> "percolation",
      "Bayes rule for Coins" -> "bayes-coin")

  def illusList(relDocsPath: String): Vector[Elem] =
    allIllus.map{
      case (name, tag) =>
        <li><a href={s"${relDocsPath}illustrations/$tag.html"}>{name}</a></li>
    }

  def page(s: String,
           relDocsPath: String): String =
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

val assList: Seq[Elem] = allAss.map((ass) =>
  <li> <a href={ass.url("")}> {ass.title}</a>, due by {ass.dateString}.</li>)

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
      <p>Assignments will be posted roughly once a week.</p>
      <ul>
        {assList}
      </ul>
    </div>
    <div id="refs" class="section">
      <h3>Suggested books</h3>
      <ol>
        <li> Ross, S.M. , Introduction to Probability Models ,Academic Press 1993.</li>
        <li> Taylor, H.M., and Karlin, S., An Introduction to Stochastic Modelling ,Academic Press, 1994. </li>
      </ol>
    </div>
    <div id ="details" class="section">
      <h3> Course Details</h3>
      <ul>
        <li><strong>Instructor:</strong> <a href="http://math.iisc.ac.in/~gadgil" target="_blank"> Siddhartha Gadgil</a></li>
        <li><strong>E-mail:</strong> <a href="mailto:siddhartha.gadgil@gmail.com" target="_blank"> siddhartha.gadgil@gmail.com</a>.</li>
        <li> <strong>Office:</strong> N-15, Department of Mathematics. </li>
        <li><strong>Lecture Timings: </strong> Monday, Wednesday, Friday : 9:00 am - 10:00 am. </li>
        <li><strong>Lecture Venue: </strong> LH-5, Department of Mathematics.</li>
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
      </ul>
    </div>
  </div>

  <div class="col-md-3 section" id ="Alerts">
    <h4> <a href="assign-all.html">Upcoming Assignments</a> </h4>
    <ul>
      {allAss.map((ass) =>
      <li>
        <a href={ass.url("")}> {ass.title}</a>, due by {ass.dateString}.
      </li>)
    }
    </ul>

  </div>

  </section>

  val assignAll: Elem =
    <section>
      <h2> Assignments </h2>
      <ul>
        {assList}
      </ul>
    </section>

  def mkAss(): Unit = {
    allAss.foreach { (ass) =>
      pprint.log(s"saving assignment ${ass.name} due on ${ass.dateString}")
      write.over(ass.target, ass.output)
    }
    write.over(pwd / "docs" / "assign-all.html", page(assignAll.toString, ""))
  }

  def mkIllus(): Unit = {
    allIllus.foreach { case (name, tag) =>
      pprint.log(s"writing illustration $name")
      write.over(
        pwd / "docs" / "illustrations" / s"$tag.html",
        page(s"""<div id="$tag"></div>""", "../")
        )
    }
  }


  def mkHome(): Unit =
    write.over(pwd / "docs" / "index.html", page(home.toString, ""))



}
