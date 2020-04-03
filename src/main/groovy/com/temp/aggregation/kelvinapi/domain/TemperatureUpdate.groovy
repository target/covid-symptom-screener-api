package com.temp.aggregation.kelvinapi.domain

import javax.persistence.MappedSuperclass
import javax.validation.constraints.NotNull
import java.time.Instant

@MappedSuperclass
class TemperatureUpdate {
  @NotNull(message = 'temperature must not be null')
  Float temperature
  String userId
  float latitude
  float longitude
  Instant timestamp = Instant.now()
}
