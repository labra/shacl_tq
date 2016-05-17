package es.weso.shacl_tq

import java.net.URI
import java.util.UUID
import org.apache.jena.graph.Graph
import org.apache.jena.graph.compose.MultiUnion
import org.apache.jena.query.Dataset
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.util.FileUtils
import org.topbraid.shacl.arq.SHACLFunctions
import org.topbraid.shacl.constraints.ModelConstraintValidator
import org.topbraid.shacl.util.ModelPrinter
import org.topbraid.spin.arq.ARQFactory
import org.topbraid.spin.util.JenaUtil
import org.topbraid.shacl._
import org.apache.jena.riot._
import java.io.ByteArrayOutputStream

object ShaclValidator {
  
  
  
  def validate(dataModel: Model, shapesModel: Model): (Model,Long) = {
    try {
      val shaclModel = ShaclSystemModel.shaclModel
      
      val combined = ModelFactory.createDefaultModel()
      combined.add(dataModel);
      combined.add(shapesModel);
      combined.add(shaclModel);
      
      SHACLFunctions.registerFunctions(combined)
      
      val shapesGraphURI = URI.create("urn:x-shacl-shapes-graph:" + UUID.randomUUID().toString())
      val dataset = ARQFactory.get.getDataset(dataModel)
      dataset.addNamedModel(shapesGraphURI.toString(), combined)
      
      val startTime = System.nanoTime()
      val results = ModelConstraintValidator.get.validateModel(dataset, shapesGraphURI, null, false, null)
      results.setNsPrefixes(shaclModel)
      val endTime = System.nanoTime()
      val time = endTime - startTime
      (results,time)
    } catch {
      case e: Exception => {
        val model = ModelFactory.createDefaultModel
        model.add(
            model.createResource(), 
            model.createProperty("exception"), 
            model.createLiteral(e.toString))
        (model,0)    
      }
    }
    
  }
  
  def validate(data: String, schema: String): (Model, Long) = {
    try {
      val dataModel = RDFDataMgr.loadModel(data)
      val shapesModel = 
        if (schema == "")
            JenaUtil.createDefaultModel()
          else 
            RDFDataMgr.loadModel(schema)
      validate(dataModel,shapesModel)      
    } catch {
      case e: Exception => {
        val model = ModelFactory.createDefaultModel
        model.add(
            model.createResource(), 
            model.createProperty("exception"), 
            model.createLiteral(e.toString))
        (model,0)    
      }
    }
  }
  
  def result2Str(m: Model): String = {
    if (m.size == 0) {
      "Validated!"
    } else {
      model2Str(m)
    }
  }
  
  def model2Str(m: Model): String = {
    val out = new ByteArrayOutputStream
    RDFDataMgr.write(out,m,RDFFormat.TURTLE_PRETTY)
    out.toString       
  }


}