package com.rallyhealth

import org.drools.{KnowledgeBase, KnowledgeBaseFactory}


object IncentivesKnowledgeBase {
  lazy val knowledgeBase: KnowledgeBase = {
    val kb = KnowledgeBaseFactory.newKnowledgeBase()
    kb.addKnowledgePackages(IncentivesRuleBuilder.builder.getKnowledgePackages)
    kb
  }
}