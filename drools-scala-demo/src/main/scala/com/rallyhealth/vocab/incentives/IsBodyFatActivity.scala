package com.rallyhealth.vocab.incentives

case class IsBodyFatActivity(
        activity: BasicActivity,
        maxAcceptableValue: Double,
        acceptableReadingType: Class[_ <: BiometricReading] = classOf[BodyFatReading])
  extends IsBiometricActivity
