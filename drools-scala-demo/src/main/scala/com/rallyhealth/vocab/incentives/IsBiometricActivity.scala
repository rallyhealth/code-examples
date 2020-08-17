package com.rallyhealth.vocab.incentives

import com.rallyhealth.vocab.Predicate

trait IsBiometricActivity extends Predicate {

  def activity: Activity

  def maxAcceptableValue: Double

  def acceptableReadingType: Class[_ <: BiometricReading]

}
