package com.rallyhealth.vocab.incentives

import com.rallyhealth.vocab.Event

trait BiometricReading extends Event {

  def employee: Employee

  def value: Double

  def klass: Class[_ <: BiometricReading]

}
