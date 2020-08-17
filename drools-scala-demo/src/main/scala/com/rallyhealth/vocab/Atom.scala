package com.rallyhealth.vocab

/**
 * An Atom is an object with properties that are themselves encouraged to be simple types, e.g.
 * numbers, strings, dates.  Atoms should not include predicates as properties.  We should enforce this
 * explicitly at some point.
 */
trait Atom extends Fact