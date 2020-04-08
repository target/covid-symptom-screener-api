package com.temp.aggregation.kelvinapi.controllers

import com.temp.aggregation.kelvinapi.domain.AssessmentQuestion
import com.temp.aggregation.kelvinapi.domain.AssessmentQuestionStatus
import com.temp.aggregation.kelvinapi.security.UserRoleService
import com.temp.aggregation.kelvinapi.services.AssessmentQuestionService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*

import javax.validation.Valid

@RestController
@Slf4j
@Validated
class AssessmentQuestionController {
  @Autowired
  AssessmentQuestionService assessmentQuestionService

  @Autowired
  UserRoleService userRoleService

  @InitBinder
  void initBinder(WebDataBinder dataBinder) {
    dataBinder.registerCustomEditor(AssessmentQuestionStatus, new CaseInsensitiveEnumConverter(AssessmentQuestionStatus))
  }

  @PostMapping('/questions')
  @ResponseStatus(HttpStatus.CREATED)
  AssessmentQuestion createQuestion(@Valid @RequestBody AssessmentQuestion question) {
    log.info("Request to create AssessmentQuestion: ${question.displayValue}")
    userRoleService.requireAdmin()
    return assessmentQuestionService.create(question)
  }

  @PutMapping('/questions/{id}')
  @ResponseStatus(HttpStatus.OK)
  AssessmentQuestion updateQuestion(@PathVariable String id, @Valid @RequestBody AssessmentQuestion question) {
    log.info("Request to update AssessmentQuestion: $id")
    userRoleService.requireAdmin()
    return assessmentQuestionService.save(id, question)
  }

  @GetMapping('/questions')
  @ResponseStatus(HttpStatus.OK)
  List<AssessmentQuestion> getQuestions(@RequestParam(name = 'status', required = false) List<AssessmentQuestionStatus> statuses) {
    log.info('Request to get all enabled AssessmentQuestions')
    List<AssessmentQuestionStatus> useStatuses = statuses ?: AssessmentQuestionStatus.values().toList()
    List<AssessmentQuestion> assessmentQuestions = assessmentQuestionService.findByStatuses(useStatuses)
    return assessmentQuestions
  }

  @GetMapping('/questions/{id}')
  @ResponseStatus(HttpStatus.OK)
  AssessmentQuestion getById(@PathVariable(value = 'id') String id) {
    log.info("Request for AssessmentQuestion by id $id")
    return assessmentQuestionService.findById(id)
  }

  @DeleteMapping('/questions/{id}')
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void disableQuestion(@PathVariable(value = 'id') String id) {
    log.info("Request to disable question $id.")
    userRoleService.requireAdmin()
    assessmentQuestionService.disable(id)
  }
}
