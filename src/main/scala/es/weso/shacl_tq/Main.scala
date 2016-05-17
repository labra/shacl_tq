package es.weso.shacl_tq

import org.rogach.scallop._
import org.rogach.scallop.exceptions._
import es.weso.rdf.jena.RDFAsJenaModel

class Opts(
  arguments: Array[String],
  onError: (Throwable, Scallop) => Nothing) extends ScallopConf(arguments) {

  banner("""| ShExperiments
            | Options:
            |""".stripMargin)

  footer("Enjoy!")

  val data = opt[String]("data",
    short = 'd',
    required = true,
    descr = "Data to validate")

  val dataFormat = opt[String]("data-format",
    short = 'f',
    default = Some("Turtle"),
    descr = "Data format to validate (default = TURTLE)")
    
  val shacl = opt[String]("shacl",
    short = 'x',
    descr = "SHACL schema")

  override protected def onError(e: Throwable) = onError(e, builder)

}

object Main extends App {
  
    override def main(args: Array[String]): Unit = {
    
    val opts = new Opts(args, errorDriver)

    if (args.length == 0) {
      opts.printHelp()
      return
    }

    val str = io.Source.fromFile(opts.data()).mkString
    val binder = ShaclBinder.fromString(str,opts.dataFormat(),None)
    val rdf = RDFAsJenaModel.fromChars(str,opts.dataFormat()).get
    val result = binder.validateModel(rdf)
    println("Result: " + result)
    println("Prefix map: " + binder.pm)
    
/*    val (results, time) = 
      ShaclValidator.validate(opts.data(), opts.shacl.get.getOrElse(""))
      println("Result: " + ShaclValidator.result2Str(results))
      println("Elapsed time: " + time + " ns") */ 
    
   } 
    
    
   private def errorDriver(e: Throwable, scallop: Scallop) = e match {
    case Help(s) =>
      println("Help: " + s)
      scallop.printHelp
      sys.exit(0)
    case _ =>
      println("Error: %s".format(e.getMessage))
      scallop.printHelp
      sys.exit(1)
  }

}
    
    