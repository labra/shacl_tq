package es.weso.shacl_tq

import scala.collection.JavaConversions._

object Processors {

  lazy val processors = List("SHACL") 

  def available(format: String): Boolean = {
    processors.contains(format.toUpperCase)
  }

  def default = processors(0)

  lazy val toList: List[String] = processors

  override def toString(): String = {
    toList.mkString(",")
  }
  
  def show: String = {
    toString
  }
  
}