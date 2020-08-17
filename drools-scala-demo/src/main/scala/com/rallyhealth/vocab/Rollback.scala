package com.rallyhealth.vocab

case class Rollback(assertion: Fact, cause: Nullify) extends Fact
