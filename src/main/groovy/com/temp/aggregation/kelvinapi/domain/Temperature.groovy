package com.temp.aggregation.kelvinapi.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Temperature implements Audited {
  String id
  String organizationId
  String organizationName
  Set<AssessmentQuestionAnswer> questionAnswers = [] as Set<AssessmentQuestionAnswer>
  @NotNull(message = 'temperature must not be null')
  @Min(value = 95L)
  @Max(value = 105L)
  Float temperature
  String userId
  float latitude
  float longitude
  Instant timestamp = Instant.now()
}
