package com.rallyhealth.vocab.incentives

import com.rallyhealth.vocab.Predicate

case class RequiresFailureOf(firstActivity: Activity, secondActivity: Activity) extends Predicate
