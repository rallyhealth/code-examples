package com.rallyhealth

import org.drools.builder.KnowledgeBuilder


trait IncentivesRuleBuilder extends RuleBuilder

/**
 * Lazy load rule files for incentives
 */
object IncentivesRuleBuilder extends IncentivesRuleBuilder {
  lazy val builder: KnowledgeBuilder =
    createBuilder(
      Seq(
        getClass.getResourceAsStream("/incentives-rules.drl")
      )
    )
}