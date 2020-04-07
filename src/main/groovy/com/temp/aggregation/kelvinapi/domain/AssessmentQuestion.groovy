package com.temp.aggregation.kelvinapi.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

import javax.validation.constraints.NotNull

import static com.temp.aggregation.kelvinapi.domain.AssessmentQuestionStatus.ENABLED

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class AssessmentQuestion implements Audited {
  String id
  @NotNull(message = 'display_value must not be null')
  String displayValue
  AssessmentQuestionStatus status = ENABLED
  int sortPriority
}
