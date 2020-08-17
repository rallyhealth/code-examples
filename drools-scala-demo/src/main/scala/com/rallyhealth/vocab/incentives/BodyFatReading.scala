package com.rallyhealth.vocab.incentives

import org.joda.time.DateTime

case class BodyFatReading(
         employee: Employee,
         value: Double,
         timestamp: DateTime,
         timeOfReceipt: DateTime,
         klass: Class[_ <: BiometricReading] = classOf[BodyFatReading])
  extends BiometricReading
