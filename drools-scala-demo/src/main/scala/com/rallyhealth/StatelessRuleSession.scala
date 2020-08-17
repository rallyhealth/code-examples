package com.rallyhealth

import com.rallyhealth.vocab.{Event, Fact, Atom, Predicate}
import org.drools.KnowledgeBaseFactory
import scala.collection.JavaConverters._

/**
 * Stateless session for interacting with the rules engine. It is "stateless" because all of the rules and facts are
 * required up-front, and rules are executed all at once.
 */
trait StatelessRuleSession {

  /**
   * Fires all rules and and return deduced facts. Can only be called once (because once the rules are fired
   * the underlying rule session is disposed), otherwise an [[IllegalStateException]] will be thrown, so if reporting on
   * more than one type is desired, then any filtering logic has to live outside this method.
   */
  def getInferences(): Set[Fact]

  def getInferences(eventSubset: Seq[Event]): Set[Fact]
}

/**
 * Drools implementation of [[StatelessRuleSession]]. Note that we intentionally don't use the Drools
 * [[org.drools.runtime.StatelessKnowledgeSession]]. In Drools, StatelessKnowledgeSession is just a convenience wrapper
 * around [[org.kie.internal.runtime.StatefulKnowledgeSession]] that requires the user to provide all rules and facts up front, immediately fires
 * all rules, and then disposes of the session. This doesn't work for our use case because the way we are using Drools
 * doesn't conform to the paradigm of mutating Java beans.
 *
 * All of the examples I've seen of enterprise applications that use Drools pass in mutable Java objects as facts, then
 * running the rules mutates the Java objects. To get the results, you'd simply look at your mutated Java objects after
 * the rules were fired.
 *
 * However, to reduce rule complexity and to minimize the chance of getting our data into a bad state, in our use case
 * we make our facts immutable to Drools. To query the results of executing some rules, we query the session for
 * specific facts (consequences) that have been inserted as a result of running the rules.
 *
 * @param facts The [[Atom]]s and [[Predicate]]s that will inform the rules.
 */
class DroolsStatelessRuleSession(builder: RuleBuilder, facts: Set[Fact])
  extends StatelessRuleSession {

  private val knowledgeSession = {

    val knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase()
    knowledgeBase.addKnowledgePackages(builder.builder.getKnowledgePackages)

    val session = knowledgeBase.newStatefulKnowledgeSession()

    session
  }

  def getInferences(): Set[Fact] = {

    try {
      facts.foreach(knowledgeSession.insert)

      knowledgeSession.fireAllRules()

      val factsOfInterest = knowledgeSession.getObjects.asScala.map(_.asInstanceOf[Fact]).toSet

      factsOfInterest
    }
    finally {
      knowledgeSession.dispose()
    }
  }

  override def getInferences(eventSubset: Seq[Event]): Set[Fact] = throw new UnsupportedOperationException
}
