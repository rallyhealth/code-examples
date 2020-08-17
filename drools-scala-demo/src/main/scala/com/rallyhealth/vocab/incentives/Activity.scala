package com.rallyhealth.vocab.incentives

import com.rallyhealth.vocab.Atom

trait Activity extends Atom {
  def activityName: String
}
