package com.temp.aggregation.kelvinapi.domain

import java.time.Instant

trait AuditedDTO {
  Instant created
  String createdBy
  Instant lastModified
  String lastModifiedBy
}
