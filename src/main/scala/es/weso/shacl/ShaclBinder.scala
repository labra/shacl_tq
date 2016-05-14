package es.weso.shacl
import es.weso.validating._
import es.weso.validating.Constraint._
import es.weso.validating.Checked._
import es.weso.validating.ConstraintReason._
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import java.io._
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.RDFLanguages._
import es.weso.rdf.RDFReader
import es.weso.rdf.jena.RDFAsJenaModel
import es.weso.rdf.PREFIXES.{rdf_type}
import es.weso.rdf.nodes._
import util._
import es.weso.utils.TryUtils

case class ShaclBinder(shapesGraph: Model) {
  
  lazy val sh = IRI("http://www.w3.org/ns/shacl#")
  lazy val sh_ValidationResult = sh + "ValidationResult"
  
  def fromString(cs:CharSequence, format: String, base:Option[String]): ShaclBinder = 
    ShaclBinder.fromString(cs,format,base)

  def serialize(format:String): String = {
    val out = new ByteArrayOutputStream
    RDFDataMgr.write(out,shapesGraph,shortnameToLang(format))
    out.toString       
  }

  def validate(rdf: RDFReader): Checked[Boolean,ConstraintReason,ViolationError] = {
    rdf match {
      case RDFAsJenaModel(rdfModel) => {
        val (resultModel,time) = ShaclValidator.validate(rdfModel,shapesGraph)
        convertResultModel(resultModel)
      }
      case _ => checkError(ViolationError.msgError(s"Unsupported rdfReader which are not based on Jena Models $rdf")) 
    }
  }
  
  def convertResultModel(resultModel: Model): Checked[Boolean,ConstraintReason,ViolationError] = {
    if (resultModel.size == 0) ok(SingleReason(true,"Validated"))
    else {
      val result = RDFAsJenaModel(resultModel)
      val ts = result.triplesWithPredicateObject(rdf_type,sh_ValidationResult)
      val vs = ts.map(_.subj).map(s => getViolationError(result,s)).toSeq
      val violationErrors = TryUtils.filterSuccess(vs)
      violationErrors match {
        case Success(es) => errs(es)
        case Failure(e) => checkError(ViolationError.msgError(e.getMessage))
      }
    }
  }
  
  private def getViolationError(result: RDFReader, node: RDFNode): Try[ViolationError] = {
    ViolationError.parse(result,node)
  }
  
}

object ShaclBinder {
  def fromString(cs:CharSequence, format: String, base:Option[String]): ShaclBinder = {
    val shapesGraph: Model = ModelFactory.createDefaultModel
    val str_reader = new StringReader(cs.toString)
    val baseURI = base.getOrElse("")
    RDFDataMgr.read(shapesGraph, str_reader, baseURI, shortnameToLang(format))
    ShaclBinder(shapesGraph)
  }

  def fromRDF(rdf: RDFReader): ShaclBinder = {
    rdf match {
      case RDFAsJenaModel(model) => ShaclBinder(model)
      case _ => throw new Exception(s"SHACLBinder: Unsupported rdfReader $rdf")
    }
  }
  
  def empty: ShaclBinder = ShaclBinder(ModelFactory.createDefaultModel)
}

