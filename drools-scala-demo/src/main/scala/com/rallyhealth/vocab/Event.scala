package com.rallyhealth.vocab

import org.joda.time.DateTime

trait Event extends Fact with Timestamp {

  def timeOfReceipt: DateTime

}
object Event {

  /**
   *
   * @tparam A the type of event to order
   * @return an ordering for events. Events are ordered first by their time of receipt, next by their timestamp
   *         and finally by their string representation. These three properties determine a total ordering on
   *         events which is essential for deterministic rule execution and inferences.
   */
  implicit def eventTotalOrder[A <: Event]: Ordering[A] = {

    new Ordering[A] {
      override def compare(x: A, y: A): Int = {
        def getTimeOfReceipt(e: Event) = e.timeOfReceipt
        val torForX = getTimeOfReceipt(x).getMillis
        val torForY = getTimeOfReceipt(y).getMillis
        if (torForX < torForY) -1
        else if (torForX > torForY) 1
        else {
          val tsForX = x.timestamp.getMillis
          val tsForY = y.timestamp.getMillis
          if (tsForX < tsForY) -1
          else if (tsForX > tsForY) 1
          else {
            val xStr = x.toString
            val yStr = y.toString
            if (xStr < yStr) -1
            else if (xStr > yStr) 1
            else 0
          }
        }
      }
    }


  }

}

