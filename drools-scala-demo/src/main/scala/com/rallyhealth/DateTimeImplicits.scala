package com.rallyhealth

import org.joda.time.DateTime

object DateTimeImplicits {

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

  /**
   * Wrapper class for [[DateTime]] adding some convenience methods
   */
  implicit class DateTimeDecorator(val date: DateTime) extends AnyVal {

    def < (other: DateTime): Boolean = date.isBefore(other)

    def === (other: DateTime): Boolean = date.isEqual(other)

    def > (other: DateTime): Boolean = date.isAfter(other)

    def <= (other: DateTime): Boolean = (this < other) || (this === other)

    def >= (other: DateTime): Boolean = (this > other) || (this === other)

    private def min(other: DateTime): DateTime = if (this <= other) this.date else other

    private def max(other: DateTime): DateTime = if (this >= other) this.date else other

  }


}
