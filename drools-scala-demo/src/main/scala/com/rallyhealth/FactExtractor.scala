package com.rallyhealth

import com.rallyhealth.vocab.{Atom, Predicate, Fact}

import scala.collection.mutable

/**
 * Extracts all facts from composite predicates.
 */
object FactExtractor {

  /**
   * Given the fact LessThen(Number(1), Number(2)), this method produces a set
   * { Number(1), Number(2), LessThan(Number(1), Number(2)) }
   */
  def unwrap(fact: Fact): Set[Fact] = {
    def getAllFacts(fact: Fact, collection: mutable.HashSet[Fact]): Unit = {
      collection += fact
      fact.productIterator.foreach {
        case p: Predicate => getAllFacts(p, collection)
        case a: Atom => collection += a
        case Some(aFact)  =>
          aFact match {
            case a: Atom => collection += a
            case _ => Set.empty
          }
        case _ => Set.empty
      }
    }

    val factCollection = new mutable.HashSet[Fact]
    getAllFacts(fact, factCollection)
    factCollection.toSet
  }


  def unwrap(facts: Set[Fact]): Set[Fact] = facts.flatMap(unwrap)

}