package com.rallyhealth

import com.rallyhealth.Fixtures._
import com.rallyhealth.vocab.{Rollback, Fact, Event, Nullify}
import com.rallyhealth.vocab.incentives._
import org.joda.time.DateTime
import org.scalatest.{Matchers, FlatSpec}
import com.rallyhealth.implicits.FactImplicits._
import DateTimeImplicits._

class RollbackEngineScalaSpec extends FlatSpec with Matchers {

  "A 'nullified' failed body fat reading " should "cause inferences to be rolled back in the case where an employee has completed the alternative" in {

    val now = DateTime.now

    val bodyFat = BodyFatReading(Employee("DONALD.FAGEN"), 28, now, now)

    val fitness =
      Completed(
        Employee("DONALD.FAGEN"),
        BasicActivity("COMPANY.FITNESS.CHALLENGE"), now.plusDays(5), now.plusDays(5)
      )

    val events =
      bodyFat ++ fitness ++ Nullify(bodyFat, now.plusDays(10), now.plusDays(10))

    println("---- Processing the following events through the Rollback Engine ----")
    events.filterByType[Event].toSeq.sortBy(_.timeOfReceipt).foreach(println)
    println()

    val rollbackInstance =
      new RollbackEngine(wellnessPlan ++ events, IncentivesKnowledgeBase.knowledgeBase)

    println("---- Rollback facts ----")
    rollbackInstance.inferencesToReverse.foreach(println)


    val underlying =
      rollbackInstance.inferencesToReverse.toSet[Fact].filterByType[Rollback].map(_.assertion)

    underlying.contains(
      Eligible(Employee("DONALD.FAGEN"),Reward(100,"dollars"))
    ) === true


    underlying.contains(
      Failed(Employee("DONALD.FAGEN"),BasicActivity("BODY.FAT.TARGET"), now, now)
    ) === true


    underlying.contains(
      Unlocked(BasicActivity("COMPANY.FITNESS.CHALLENGE"))
    ) === true

  }

}
