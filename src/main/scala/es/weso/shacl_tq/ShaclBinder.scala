package es.weso.shacl_tq

import java.io.{ ByteArrayOutputStream, StringReader }

import scala.util.{ Failure, Success, Try }

import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.RDFLanguages.shortnameToLang

import org.apache.jena.rdf.model.{ Model, ModelFactory, Resource }

import es.weso.rdf.PREFIXES.rdf_type
import es.weso.rdf.{ RDFReader, PrefixMap }
import es.weso.rdf.jena.{RDFAsJenaModel, JenaMapper}
import es.weso.rdf.nodes.{ IRI, RDFNode }
import es.weso.utils.TryUtils
import es.weso.validating.Checked
import es.weso.validating.Checked.{ checkError, errs, ok }
import es.weso.validating.ConstraintReason
import es.weso.validating.ConstraintReason.cReason
import es.weso.validating.SingleReason
import org.apache.jena.vocabulary.RDF
import scala.collection.JavaConversions._


case class ShaclBinder(shapesGraph: Model) {
  
  lazy val sh = IRI("http://www.w3.org/ns/shacl#")
  lazy val sh_Shape = sh + "Shape"
  lazy val sh_ValidationResult = sh + "ValidationResult"
  lazy val sh_message = sh + "message"
  
  def rdfShapes : RDFReader = 
    RDFAsJenaModel(shapesGraph)
  
  def fromString(cs:CharSequence, format: String, base:Option[String]): ShaclBinder = 
    ShaclBinder.fromString(cs,format,base)

  def serialize(format:String): String = {
    val out = new ByteArrayOutputStream
    RDFDataMgr.write(out,shapesGraph,shortnameToLang(format))
    out.toString       
  }

  def validateModel(rdf: RDFReader): Model = {
    rdf match {
      case RDFAsJenaModel(rdfModel) => {
        val (resultModel,time) = ShaclValidator.validate(rdfModel,shapesGraph)
        resultModel
      }
      case _ => {
        val m = ModelFactory.createDefaultModel()
        m.add(m.createResource,m.createProperty(sh_message.str),m.createLiteral(s"Unsupported rdf $rdf"))
        m
      }
    }
  }
  
/*  def validate(rdf: RDFReader): Checked[Boolean,ConstraintReason,ViolationError] = {
    rdf match {
      case RDFAsJenaModel(rdfModel) => {
        val (resultModel,time) = ShaclValidator.validate(rdfModel,shapesGraph)
        println(s"Result: ${ShaclValidator.result2Str(resultModel)}")  
        convertResultModel(resultModel)
      }
      case _ => checkError(ViolationError.msgError(s"Unsupported rdfReaders which are not based on Jena Models $rdf")) 
    }
  }
  */
  
  /**
   * Validates a node against a shape
   * 
   * It only adds a scopeNode declaration from the shape to the node
   */
  def validateNodeShape(node: IRI, shape: String, rdf: RDFReader): Model = {
    rdf match {
      case RDFAsJenaModel(rdfModel) => {
        val sh_scopeNodeStr = "http://www.w3.org/ns/shacl#scopeNode"
        val sh_scopeNode = rdfModel.createProperty(sh_scopeNodeStr)
        val resourceShape = rdfModel.createResource(shape)
        val resourceNode = rdfModel.createResource(node.str)
        rdfModel.add(resourceShape,sh_scopeNode,resourceNode)
        val (resultModel,time) = ShaclValidator.validate(rdfModel,shapesGraph)
        println(s"Result: ${ShaclValidator.result2Str(resultModel)}")  
        resultModel
      }
      case _ => {
        val m = ModelFactory.createDefaultModel()
        m.add(m.createResource,m.createProperty(sh_message.str),m.createLiteral(s"Unsupported rdf $rdf"))
        m
      }
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
    println(s"getViolationError on $node")
    val ts = result.triplesWithSubjectPredicate(node,sh_message)
    val msg = 
      if (ts.size == 1) ts.head.obj.toString
      else "<not found message>"
      
    // ViolationError.parse(result,node)
    // ViolationErrorParser.parse(node,result)
    Success(ViolationError.msgError(msg))
  }
  
  def shapes: List[RDFNode] = {
    val shapeTriples = rdfShapes.triplesWithPredicateObject(rdf_type, sh_Shape)
    shapeTriples.map(_.subj).toList
  }
  
  def pm: PrefixMap = {
    rdfShapes.getPrefixMap
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

