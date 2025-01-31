package com.temp.aggregation.kelvinapi.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

import javax.validation.constraints.NotNull

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class AssessmentQuestionAnswer implements Audited {
  String id
  @NotNull(message = 'question must not be null')
  AssessmentQuestion question
  @NotNull(message = 'answer must not be null')
  Boolean answer
}
