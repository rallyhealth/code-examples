package com.rallyhealth

import com.rallyhealth.vocab.{Nullify, Event, Fact}
import org.joda.time.DateTime
import DateTimeImplicits._

/**
 * Given a set of facts containing [[com.rallyhealth.vocab.Event]]s, produces a sequence of ordered
 * (by time of receipt) event sub-sequences. Two sub-sequences are created for each
 * event that has been [[com.rallyhealth.vocab.Nullify]]-ed. The first contains all previous non-nullified
 * events. The second contains all previous non-nullified events minus the event
 * corresponding to the current Nullify under consideration. In both cases, Nullify
 * events are removed.
 *
 * As an example, consider the following sequence of events in time-of-receipt order
 * where C represents [[com.rallyhealth.vocab.incentives.Completed]] and N(C)
 * means that a particular completed has been [[com.rallyhealth.vocab.Nullify]]-ed
 *
 * C1, C2, C3, N(C2), C4, C5, N(C1), C6
 *
 * In this case, two sub-sequence pairs are created (one corresponding to N(C2) and the
 * other corresponding to N(C1))
 *
 * The first pair contains the following sequences:
 * C1, C2, C3
 * C1, C3
 *
 * The second pair contains the following sequences:
 * C1, C3, C4, C5
 * C3, C4, C5
 *
 * Note that in the second pair above, C6 is absent. This is by design since there are no events
 * nullified after C6
 *
 * This class also exposes a sequence of all non-nullified events in the fact set. In the above example,
 * this sequence would be:
 *
 * C3, C4, C5, C6
 *
 * The above example exists in code in EventStreamHelperTest
 *
 */
class EventStreamHelper(facts: Set[Fact]) {

  lazy val events: Seq[Event] = facts.collect{ case x : Event => x }.toSeq.sorted

  private lazy val nullificationEvents = events.collect{ case x: Nullify => x}

  private lazy val nullificationEventIndexes = events.zipWithIndex.filter(_._1.isInstanceOf[Nullify]).map(_._2)

  private lazy val nullificationByEvent = nullificationEvents.map(e => e.event -> e).toMap

  private lazy val eventSubStreams =
    nullificationEventIndexes.map(index => events.slice(0, index + 1))

  lazy val nonNullifiedEvents: Seq[Event] =
    events.filterNot(_.isInstanceOf[Nullify]).filterNot(nullificationByEvent.contains)

  lazy val eventStreamPairs: Seq[EventStreamContainer] =
    eventSubStreams.map{stream =>

      val mostRecentNullify = stream.last.asInstanceOf[Nullify]

      def isNullificationInThePast(nullification: Nullify) =
        mostRecentNullify.timeOfReceipt > nullification.timeOfReceipt

      val allPreviousNonNullifiedCompletes =
        stream.dropRight(1).filterNot(_.isInstanceOf[Nullify])
          .filter{event =>
          nullificationByEvent.get(event) match {
            case Some(nullification) => ! isNullificationInThePast(nullification)
            case None => true
          }
        }

      EventStreamContainer(
        allPreviousNonNullifiedCompletes,
        allPreviousNonNullifiedCompletes.filterNot(_ == mostRecentNullify.event),
        mostRecentNullify
      )

    }

}

case class EventStreamContainer(
                                 previouslyNullifiedEvents: Seq[Event],
                                 currentlyNullifiedEvents: Seq[Event],
                                 nullificationEvent: Nullify) {

  def timestamp: DateTime = nullificationEvent.timestamp

  def timeOfReceipt: DateTime = nullificationEvent.timeOfReceipt

}
