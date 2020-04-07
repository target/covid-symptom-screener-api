package com.temp.aggregation.kelvinapi.domain

import java.time.Instant

trait Audited {
  Instant created
  String createdBy
  Instant lastModified
  String lastModifiedBy
}
