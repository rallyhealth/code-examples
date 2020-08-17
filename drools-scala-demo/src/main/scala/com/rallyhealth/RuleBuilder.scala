package com.rallyhealth

import java.io.InputStream

import org.drools.builder.{ResourceType, KnowledgeBuilderFactory, KnowledgeBuilder}
import org.drools.io.ResourceFactory


/**
 * Trait for parsing .drl (drools) input streams and building an in-memory model.
 * Building this model is expensive so we'd like to do it one time only.
 */
trait RuleBuilder {

  protected def createBuilder(resources: Seq[InputStream]): KnowledgeBuilder = {

    val builder = KnowledgeBuilderFactory.newKnowledgeBuilder()

    resources.foreach {
      inputStream =>
        val res = ResourceFactory.newInputStreamResource(inputStream)
        builder.add(res, ResourceType.DRL)
    }

    if (builder.hasErrors) {
      val errors = builder.getErrors
      throw new RuntimeException("Unable to: create KnowledgeBuilder: " + errors.toString)
    }

    builder
  }

  def builder: KnowledgeBuilder

}
