package es.weso.shacl_tq

import scala.collection.JavaConversions._

object DataFormats {

  lazy val formats = List("TURTLE")

  def available(format: String): Boolean = {
    formats.contains(format.toUpperCase)
  }

  def default = formats(0)

  lazy val toList: List[String] = formats

  override def toString(): String = {
    toList.mkString(",")
  }
  
  def show: String = {
    toString
  }
  
}