package es.weso.shacl_tq

import org.scalatest._
import java.io.InputStream
import java.net.URI
import java.util.UUID
import org.topbraid.shacl.arq.SHACLFunctions
import org.topbraid.shacl.constraints.ModelConstraintValidator
import org.topbraid.shacl.vocabulary.SH
import org.topbraid.spin.arq.ARQFactory
import org.topbraid.spin.util.JenaUtil
import org.topbraid.shacl.util.ModelPrinter
import com.hp.hpl.jena.graph.Graph
import com.hp.hpl.jena.graph.compose.MultiUnion
import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.util.FileUtils
import org.apache.jena.riot._
import java.io.ByteArrayOutputStream
import ShaclValidator._

class TestShacl extends FunSpec with Matchers {
  
  describe("Shacl validator") {
    ignore("validate issues") {
      validate("issuesData","issueShapes")
      validate("issuesErrorsData","issueShapes",false)
      
      validate("studentData","studentShape")
      validate("codeData","codeShapes")
      validate("groupData","groupShape")
      validate("publicContractData","publicContractShapes")
    }
    
    describe("validate tests") {
      validate("webindexData","webindexShapes")
      
     // validate("groupParentExampleData","groupParentExampleShape")
    }
    
  
    
    def validate(data: String, schema: String, ok: Boolean = true): Unit = {
      it(s"should validate $data with $schema") {
      val dataModel = JenaUtil.createDefaultModel()
      dataModel.read(getClass.getResourceAsStream(s"/$data.ttl"),"urn:dummy", FileUtils.langTurtle)
      
      val shapesModel = JenaUtil.createDefaultModel()
      shapesModel.read(getClass.getResourceAsStream(s"/$schema.ttl"),"urn:dummy", FileUtils.langTurtle)
      
      val shaclModel = JenaUtil.createDefaultModel()
      val is = getClass().getResourceAsStream("/etc/shacl.ttl")
      shaclModel.read(is, SH.BASE_URI, FileUtils.langTurtle)
      
      val combined = ModelFactory.createDefaultModel()
      combined.add(dataModel);
      combined.add(shapesModel);
      combined.add(shaclModel);
      
      SHACLFunctions.registerFunctions(combined)
      
      val shapesGraphURI = URI.create("urn:x-shacl-shapes-graph:" + UUID.randomUUID().toString())
      val dataset = ARQFactory.get.getDataset(dataModel)
      dataset.addNamedModel(shapesGraphURI.toString(), combined)
      
      val results = ModelConstraintValidator.get.validateModel(dataset, shapesGraphURI, null, false, null)
      results.setNsPrefixes(shaclModel)
      
      
      val numberErrors : Int = results.size.toInt
       if (ok) {
         if (numberErrors == 0) info("validated")
         else {
           fail(s"Errors found: ${model2Str(results)}") 
         }
       }
       else {
         if (numberErrors > 0) info(s"Failed with $numberErrors errors")
         else {
           fail("It should not validate but no errors found")
         }
       }
      }
    }
  }
 
}