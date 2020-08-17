package com.rallyhealth.vocab.incentives

import com.rallyhealth.vocab.Event
import org.joda.time.DateTime


case class Completed(employee: Employee, activity: Activity, timestamp: DateTime, timeOfReceipt: DateTime) extends Event
