package com.temp.aggregation.kelvinapi.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

import javax.validation.constraints.NotNull
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class TemperatureDTO implements AuditedDTO {
  String id
  String organizationId
  String organizationName
  Set<AssessmentQuestionAnswerDTO> questionAnswers = [] as Set<AssessmentQuestionAnswerDTO>
  @NotNull(message = 'temperature must not be null')
  Float temperature
  String userId
  float latitude
  float longitude
  Instant timestamp = Instant.now()
}
