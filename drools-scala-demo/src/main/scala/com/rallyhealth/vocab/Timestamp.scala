package com.rallyhealth.vocab

import org.joda.time.DateTime

/**
 * Allows for adding timestamps to relations
 */
trait Timestamp {

  def timestamp: DateTime

  def isBefore(other: DateTime) : Boolean = timestamp.isBefore(other)

  def isBefore(other: Long): Boolean = timestamp.isBefore(other)

  def isBefore(other: Timestamp): Boolean = timestamp.isBefore(other.timestamp)

  def isOnOrBefore(other: DateTime): Boolean = isBefore(other) || isEqual(other)

  def isAfter(other: DateTime) : Boolean = timestamp.isAfter(other)

  def isAfter(other: Timestamp): Boolean = timestamp.isAfter(other.timestamp)

  def isOnOrAfter(other: DateTime): Boolean = timestamp.isAfter(other) || timestamp.isEqual(other)

  def isEqual(other: DateTime) : Boolean = timestamp.isEqual(other)

  def isEqual(other: Timestamp): Boolean = timestamp.isEqual(other.timestamp)

}
