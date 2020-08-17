package com.rallyhealth.vocab

import org.joda.time.DateTime

case class Nullify(event: Event, timestamp: DateTime, timeOfReceipt: DateTime) extends Event