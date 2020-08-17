package com.rallyhealth

import com.rallyhealth.vocab.incentives._
import com.rallyhealth.implicits.FactImplicits._

object Fixtures {

  val wellnessPlan = {
    val companyFitnessChallenge = BasicActivity("COMPANY.FITNESS.CHALLENGE")

    val bodyFatActivity = BasicActivity("BODY.FAT.TARGET")

    val isBodyFact = IsBodyFatActivity(bodyFatActivity, 27)

    val biometricOrChallenge =
      Choice("BODY.FAT.OR.CHALLENGE", bodyFatActivity, companyFitnessChallenge)

    companyFitnessChallenge ++
      bodyFatActivity ++
      isBodyFact ++
      biometricOrChallenge ++
      RequiresFailureOf(companyFitnessChallenge, bodyFatActivity) ++
      Pays(biometricOrChallenge, Reward(100, "dollars"))
  }

}
