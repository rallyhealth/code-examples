package com.rallyhealth

import com.rallyhealth.vocab.{Event, Fact}
import org.drools.KnowledgeBase
import scala.collection.JavaConverters._
import com.rallyhealth.implicits.FactImplicits._

class EventDrivenRulesSession(knowledgeBase: KnowledgeBase, facts: Set[Fact]) {

  private val knowledgeSession = {

    val session = knowledgeBase.newStatefulKnowledgeSession()

    session

  }

  private val events = facts.collect{ case x : Event => x }.toSeq.sorted

  private val backgroundFacts = facts diff events.toSet

  def getInferences(eventSubset: Seq[Event]): Set[Fact] = {

    require(events.containsSlice(eventSubset), "the event subset should be a slice in the ordered set of total events")

    val queue = eventSubset.sorted

    try {

      backgroundFacts.foreach(knowledgeSession.insert)
      knowledgeSession.fireAllRules()

      queue.foreach(controlFact => {
        knowledgeSession.insert(controlFact)
        knowledgeSession.fireAllRules()
      })

      val factsOfInterest = knowledgeSession.getObjects.asScala.map(_.asInstanceOf[Fact]).toSet


      factsOfInterest
    }
    finally {
      knowledgeSession.dispose()
    }

  }

  def getInferences(): Set[Fact] = getInferences(events)

}