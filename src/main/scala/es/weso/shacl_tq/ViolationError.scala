package es.weso.shacl_tq

import es.weso.rdf.nodes._
import es.weso.rdf.RDFReader
import util._

case class ViolationError(
    message: String,
    focusNode: Option[RDFNode],
    subject: Option[RDFNode],
    predicate: Option[RDFNode],
    severity: Option[RDFNode],
    sourceConstraint: Option[RDFNode],
    sourceShape: Option[RDFNode],
    sourceTemplate: Option[RDFNode]
) {
  
  override def toString: String = {
    s"*** Error: $message\n" +
    maybe("focusNode", focusNode) + 
    maybe("subject", subject) +
    maybe("predicate", predicate) + 
    maybe("severity", severity) +
    maybe("sourceConstraint", sourceConstraint) +
    maybe("sourceShape", sourceShape) +
    maybe("sourceTemplate", sourceTemplate) 
  }
  
  private def maybe[A](name: String, m: Option[A]) = {
    if (m.isDefined) s"$name: ${m.get.toString}\n" 
    else ""
  }
}

object ViolationError {
  
  def msgError(msg: String): ViolationError = 
    ViolationError(msg,None,None,None,None,None,None,None)
    
  def parse(rdf:RDFReader, node: RDFNode): Try[ViolationError] = {
    ViolationErrorParser.parse(node,rdf)
  }
  
}
