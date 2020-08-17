package com.rallyhealth

import com.rallyhealth.vocab.{Event, Rollback, Nullify, Fact}
import org.drools.KnowledgeBase
import com.rallyhealth.implicits.FactImplicits._

class RollbackEngine(facts: Set[Fact], knowledgeBase: KnowledgeBase) {

  private val eventStreamHelper = new EventStreamHelper(facts)

  val backgroundFacts: Set[Fact] = facts -- eventStreamHelper.events

  def nonNullifiedEvents: Seq[Event] = eventStreamHelper.nonNullifiedEvents

  def inferencesToReverse: Seq[Fact] = {
    if (facts.exists(_.isInstanceOf[Nullify])) {
      eventStreamHelper.eventStreamPairs.flatMap{
        case EventStreamContainer(stream1, stream2, nullification) =>

          val inferences1 = (backgroundFacts ++ stream1).eventBasedInferences(knowledgeBase)

          val inferences2 = (backgroundFacts ++ stream2).eventBasedInferences(knowledgeBase)

          val reversibleInferences = inferences1 -- inferences2

          reversibleInferences.map(x => Rollback(x, nullification))

      }
    }
    else {
      // if there are no Nullify facts, then nothing will get reversed.
      // No need to run drools to find that out
      Seq.empty[Fact]
    }
  }

}