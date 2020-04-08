package com.temp.aggregation.kelvinapi.integration.controllers

import com.temp.aggregation.kelvinapi.domain.*
import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import com.temp.aggregation.kelvinapi.integration.testclients.AssessmentQuestionClient
import com.temp.aggregation.kelvinapi.repositories.AssessmentQuestionRepository
import com.temp.aggregation.kelvinapi.repositories.UserRoleRepository
import com.temp.aggregation.kelvinapi.services.AssessmentQuestionService
import feign.FeignException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import static com.temp.aggregation.kelvinapi.domain.AssessmentQuestionStatus.DISABLED
import static com.temp.aggregation.kelvinapi.domain.AssessmentQuestionStatus.ENABLED
import static com.temp.aggregation.kelvinapi.domain.Role.ADMIN

class AssessmentQuestionControllerFunctionalSpec extends BaseIntegrationSpec {
  @Autowired
  AssessmentQuestionClient client

  @Autowired
  AssessmentQuestionService service

  @Autowired
  AssessmentQuestionRepository repository

  @Autowired
  UserRoleRepository userRoleRepository

  void setup() {
    cleanup()
    userRoleRepository.save(
        new UserRoleDTO(emailAddress: 'test-adminA@email.com', role: ADMIN)
    )
  }

  void cleanup() {
    repository.deleteAll()
  }

  void 'can create an assessment question'() {
    given:
    AssessmentQuestion assessmentQuestionUpdate = new AssessmentQuestion(
        displayValue: 'Wha?'
    )

    when:
    ResponseEntity<AssessmentQuestion> response = client.createQuestion(assessmentQuestionUpdate)

    then:
    response.statusCode == HttpStatus.CREATED
    response.body.displayValue == 'Wha?'
    response.body.id
    response.body.created
    response.body.createdBy
    response.body.lastModified
    response.body.lastModifiedBy
  }

  void 'create fails for non-admin user'() {
    given: 'remove the admin role from the test user'
    UserRoleDTO currentTestUserRole = userRoleRepository.findById('test-adminA@email.com').orElse(null)
    userRoleRepository.deleteById('test-adminA@email.com')

    when:
    client.createQuestion(
        new AssessmentQuestion(
            displayValue: 'Wha?'
        )
    )

    then:
    FeignException e = thrown(FeignException)
    e.status() == HttpStatus.FORBIDDEN.value()

    cleanup: 'restore admin role for test user'
    userRoleRepository.save(currentTestUserRole)
  }

  void 'can update an assessment question'() {
    given:
    AssessmentQuestion saved = service.create(
        new AssessmentQuestion(
            displayValue: 'Wha?'
        )
    )

    when:
    ResponseEntity<AssessmentQuestion> updated = client.updateQuestion(
        saved.id,
        new AssessmentQuestion(
            displayValue: 'Are you kidding me?'
        )
    )

    then:
    updated.statusCode == HttpStatus.OK
    updated.body.id == saved.id
    updated.body.displayValue == 'Are you kidding me?'
    updated.body.lastModifiedBy
    updated.body.lastModified
  }

  void 'update fails for non-admin user'() {
    given: 'remove the admin role from the test user'
    AssessmentQuestion saved = service.create(
        new AssessmentQuestion(
            displayValue: 'Wha?'
        )
    )
    UserRoleDTO currentTestUserRole = userRoleRepository.findById('test-adminA@email.com').orElse(null)
    userRoleRepository.deleteById('test-adminA@email.com')

    when:
    client.updateQuestion(
        saved.id,
        new AssessmentQuestion(
            displayValue: 'Wha?'
        )
    )

    then:
    FeignException e = thrown(FeignException)
    e.status() == HttpStatus.FORBIDDEN.value()

    cleanup: 'restore admin role for test user'
    userRoleRepository.save(currentTestUserRole)
  }

  void 'can retrieve question by id'() {
    given:
    AssessmentQuestion saved = service.create(
        new AssessmentQuestion(
            displayValue: 'Wha?'
        )
    )

    when:
    ResponseEntity<AssessmentQuestion> response = client.getById(saved.id)

    then:
    response.statusCode == HttpStatus.OK
    response.body.id == saved.id
  }

  void 'can retrieve question by id returns 404 for bad id'() {
    when:
    ResponseEntity<ErrorResponse> response = client.getByIdFails('bad-id')

    then:
    response.statusCode == HttpStatus.NOT_FOUND
    response.body.message == 'Not Found'
  }

  void 'can retrieve all enabled questions in one call'() {
    given:
    AssessmentQuestion savedA = service.create(
        new AssessmentQuestion(
            displayValue: 'Wha?', status: ENABLED,
            sortPriority: 10
        )
    )
    AssessmentQuestion savedB = service.create(
        new AssessmentQuestion(
            displayValue: 'Huh?', status: ENABLED,
            sortPriority: 20
        )
    )
    service.create(
        new AssessmentQuestion(
            displayValue: 'Buy why?', status: DISABLED,
            sortPriority: 30
        )
    )

    when:
    ResponseEntity<List<AssessmentQuestion>> response = client.getQuestions([ENABLED])

    then:
    response.statusCode == HttpStatus.OK
    response.body.size() == 2
    response.body.find { it.id == savedA.id && it.sortPriority == 10 }
    response.body.find { it.id == savedB.id && it.sortPriority == 20 }
    response.body.first().sortPriority == 10
    response.body.last().sortPriority == 20
  }

  void 'delete call sets status to DISABLED'() {
    given:
    AssessmentQuestion savedA = service.create(
        new AssessmentQuestion(
            displayValue: 'Wha?', status: ENABLED,
            sortPriority: 10
        )
    )
    service.create(
        new AssessmentQuestion(
            displayValue: 'Huh?', status: ENABLED,
            sortPriority: 20
        )
    )

    when:
    ResponseEntity<Void> response = client.disable(savedA.id)

    then:
    response.statusCode == HttpStatus.NO_CONTENT

    and:
    repository.findById(savedA.id).orElse(null).status == DISABLED
  }

  void 'delete fails for non-admin user'() {
    given: 'remove the admin role from the test user'
    AssessmentQuestion saved = service.create(
        new AssessmentQuestion(
            displayValue: 'Wha?',
            sortPriority: 10
        )
    )
    UserRoleDTO currentTestUserRole = userRoleRepository.findById('test-adminA@email.com').orElse(null)
    userRoleRepository.deleteById('test-adminA@email.com')

    when:
    client.disable(saved.id)

    then:
    FeignException e = thrown(FeignException)
    e.status() == HttpStatus.FORBIDDEN.value()

    cleanup: 'restore admin role for test user'
    userRoleRepository.save(currentTestUserRole)
  }
}
