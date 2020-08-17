package com.rallyhealth.vocab

/**
 * A predicate is a statement that is true or false.  The existence of a concrete instantiation
 * of a predicate P means that P is true from the prospective of the rules system.
 * Conversely, the absence of a concrete instantiation of a predicate P means P is false.
 * Thus, there is no method like "isTrue()" associated with a predicate.
 */

trait Predicate extends Fact