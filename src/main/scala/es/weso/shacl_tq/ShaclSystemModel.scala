package es.weso.shacl_tq

import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.SystemTriples;

object ShaclSystemModel {
  def shaclModel: Model = {
    val m: Model = JenaUtil.createDefaultModel()

    val shaclTTL = getClass().getResourceAsStream("/etc/shacl.ttl")
		m.read(shaclTTL, SH.BASE_URI, FileUtils.langTurtle)
			
		val dashTTL = getClass().getResourceAsStream("/etc/dash.ttl")
		m.read(dashTTL, SH.BASE_URI, FileUtils.langTurtle);
			
		m.add(SystemTriples.getVocabularyModel())
		SHACLFunctions.registerFunctions(m);
    m
  }
}

