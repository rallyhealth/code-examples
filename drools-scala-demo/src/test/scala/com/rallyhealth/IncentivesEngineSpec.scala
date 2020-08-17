package com.rallyhealth

import com.rallyhealth.Fixtures._
import com.rallyhealth.implicits.FactImplicits._
import com.rallyhealth.vocab.incentives._
import org.joda.time.DateTime
import org.scalatest._
import com.rallyhealth.vocab.incentives.Failed

class IncentivesEngineSpec extends FlatSpec with Matchers {

  private val now = DateTime.now

  "An employee who obtains a successful body fat reading " should "earn a reward" in {

    println("---- Background facts for employee incentives plan ----")
    wellnessPlan.foreach(println)
    println()

    val reading = BodyFatReading(Employee("DONALD.FAGEN"), 25, now, now)
    println("---- Body fat percentage reading event ----")
    println(reading)
    println()

    val inferences = {

      val incentivesEngineOutput =
        (wellnessPlan ++ reading).eventBasedInferences(IncentivesKnowledgeBase.knowledgeBase)
      (incentivesEngineOutput -- wellnessPlan) -- Set(reading)
    }

    println("---- Inferences after processing a body fat percentage of 25 ----")
    inferences.foreach(println)

    inferences.filterByType[Eligible].head should be (Eligible(Employee("DONALD.FAGEN"),Reward(100,"dollars")))

  }

  "An employee who fails to obtain the target body fat " should " have a Failed inference generated " in {

    println("---- Background facts for employee incentives plan ----")
    wellnessPlan.foreach(println)
    println()

    val reading = BodyFatReading(Employee("DONALD.FAGEN"), 28, now, now)
    println("---- Body fat percentage reading event ----")
    println(reading)
    println()

    val inferences = {

      val incentivesEngineOutput =
        (wellnessPlan ++ reading).eventBasedInferences(IncentivesKnowledgeBase.knowledgeBase)
      (incentivesEngineOutput -- wellnessPlan) -- Set(reading)
    }

    println("---- Inferences after processing a body fat percentage of 28 ----")
    inferences.foreach(println)

    inferences.filterByType[Failed].head should be (Failed(Employee("DONALD.FAGEN"),BasicActivity("BODY.FAT.TARGET"), now, now))

  }

  "An employee who fails to obtain the target body fat but subsequently completes an alternative activity" should "unlock the alternative and earn a reward" in {

    println("---- Background facts for employee incentives plan ----")
    wellnessPlan.foreach(println)
    println()

    val reading = BodyFatReading(Employee("DONALD.FAGEN"), 28, now, now)
    val completesFitnessChallenge =
      Completed(Employee("DONALD.FAGEN"), BasicActivity("COMPANY.FITNESS.CHALLENGE"), now.plusDays(5), now.plusDays(5))
    println("---- Events ----")
    println(reading)
    println(completesFitnessChallenge)
    println()

    val inferences = {

      val incentivesEngineOutput =
        (wellnessPlan ++
          reading ++
          completesFitnessChallenge).eventBasedInferences(IncentivesKnowledgeBase.knowledgeBase)
      (incentivesEngineOutput -- wellnessPlan) -- Set(reading, completesFitnessChallenge)
    }

    println("---- Inferences after processing events ----")
    inferences.foreach(println)

    inferences.filterByType[Failed].head should be (Failed(Employee("DONALD.FAGEN"),BasicActivity("BODY.FAT.TARGET"), now, now))

    inferences.contains(
      Unlocked(
        Choice("BODY.FAT.OR.CHALLENGE",BasicActivity("BODY.FAT.TARGET"),BasicActivity("COMPANY.FITNESS.CHALLENGE"))
      )
    ) === true


    inferences.contains(
      Eligible(Employee("DONALD.FAGEN"),Reward(100,"dollars"))
    ) === true


  }

}
