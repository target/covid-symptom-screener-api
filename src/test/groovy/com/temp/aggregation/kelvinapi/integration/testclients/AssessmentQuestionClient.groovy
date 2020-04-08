package com.temp.aggregation.kelvinapi.integration.testclients

import com.temp.aggregation.kelvinapi.domain.AssessmentQuestion
import com.temp.aggregation.kelvinapi.domain.AssessmentQuestionStatus
import com.temp.aggregation.kelvinapi.domain.ErrorResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

import static org.springframework.web.bind.annotation.RequestMethod.*

@FeignClient(
    name = 'questions',
    url = '${feign.app.url}',
    decode404 = true
)
interface AssessmentQuestionClient {
  @RequestMapping(method = POST, value = '/questions')
  ResponseEntity<AssessmentQuestion> createQuestion(@RequestBody AssessmentQuestion question)

  @RequestMapping(method = PUT, value = '/questions/{id}')
  ResponseEntity<AssessmentQuestion> updateQuestion(@PathVariable(value = 'id') String id,
                                                    @RequestBody AssessmentQuestion question)

  @RequestMapping(method = GET, value = '/questions/{id}')
  ResponseEntity<AssessmentQuestion> getById(@PathVariable(value = 'id') String id)

  @RequestMapping(method = GET, value = '/questions/{id}')
  ResponseEntity<ErrorResponse> getByIdFails(@PathVariable(value = 'id') String id)

  @RequestMapping(method = GET, value = '/questions')
  ResponseEntity<List<AssessmentQuestion>> getQuestions(@RequestParam(value = 'status') List<AssessmentQuestionStatus> statuses)

  @RequestMapping(method = DELETE, value = '/questions/{id}')
  ResponseEntity<Void> disable(@PathVariable(value = 'id') String id)
}
