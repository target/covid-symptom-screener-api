package com.temp.aggregation.kelvinapi.integration.controllers

import com.temp.aggregation.kelvinapi.domain.*
import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import com.temp.aggregation.kelvinapi.integration.testclients.TemperatureClient
import com.temp.aggregation.kelvinapi.repositories.AssessmentQuestionAnswerRepository
import com.temp.aggregation.kelvinapi.repositories.AssessmentQuestionRepository
import com.temp.aggregation.kelvinapi.repositories.OrganizationRepository
import com.temp.aggregation.kelvinapi.repositories.TemperatureRepository
import com.temp.aggregation.kelvinapi.repositories.UserRoleRepository
import com.temp.aggregation.kelvinapi.services.AssessmentQuestionService
import com.temp.aggregation.kelvinapi.services.TemperaturesService
import feign.FeignException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Unroll

import static com.temp.aggregation.kelvinapi.domain.ApprovalStatus.*
import static com.temp.aggregation.kelvinapi.domain.OrganizationSector.OTHER_PRIVATE_BUSINESS
import static com.temp.aggregation.kelvinapi.domain.Role.ADMIN
import static org.springframework.data.domain.Sort.Direction.ASC

class TemperaturesControllerFunctionalSpec extends BaseIntegrationSpec {
  @Autowired
  TemperatureClient client

  @Autowired
  TemperatureRepository temperatureRepository

  @Autowired
  TemperaturesService temperaturesService

  @Autowired
  OrganizationRepository organizationRepository

  @Autowired
  AssessmentQuestionService assessmentQuestionService

  @Autowired
  AssessmentQuestionRepository assessmentQuestionRepository

  @Autowired
  AssessmentQuestionAnswerRepository assessmentQuestionAnswerRepository

  @Autowired
  UserRoleRepository userRoleRepository

  void setup() {
    cleanup()
    userRoleRepository.save(
        new UserRoleDTO(emailAddress: 'test-adminA@email.com', role: ADMIN)
    )
  }

  void cleanup() {
    assessmentQuestionAnswerRepository.deleteAll()
    assessmentQuestionRepository.deleteAll()
    temperatureRepository.deleteAll()
    organizationRepository.deleteAll()
  }

  void 'can get temperatures by org id including associated question answers'() {
    given:
    OrganizationDTO savedOrg = organizationRepository.save(
        new OrganizationDTO(
            orgName: 'testOrgA',
            taxId: '111',
            approvalStatus: APPROVED,
            sector: OTHER_PRIVATE_BUSINESS,
            contactName: 'Test Contact',
            contactEmail: 'test-contact@email.com',
            contactPhone: '111-111-1111'
        )
    )
    List<TemperatureDTO> temperatureDTOs = [
        new TemperatureDTO(
            organizationId: savedOrg.id,
            temperature: 98.6,
            userId: 'test-user-a',
            latitude: 44.934940,
            longitude: -93.158660
        ),
        new TemperatureDTO(
            organizationId: savedOrg.id,
            temperature: 100.5,
            userId: 'test-user-b',
            latitude: 44.934941,
            longitude: -93.158661
        ),
        new TemperatureDTO(
            organizationId: 'aDifferentOrg',
            temperature: 100.5,
            userId: 'test-user-b',
            latitude: 44.934941,
            longitude: -93.158661
        )
    ]
    List<TemperatureDTO> savedTemperatureDTOs = temperatureRepository.saveAll(temperatureDTOs)
    AssessmentQuestionDTO savedQuestionA = assessmentQuestionRepository.save(
        new AssessmentQuestionDTO(
            displayValue: 'Wha?'
        )
    )
    AssessmentQuestionDTO savedQuestionB = assessmentQuestionRepository.save(
        new AssessmentQuestionDTO(
            displayValue: 'Huh?'
        )
    )

    assessmentQuestionAnswerRepository.saveAll(savedTemperatureDTOs.collectMany { temperature ->
      return [
          new AssessmentQuestionAnswerDTO(
              temperature: temperature,
              question: savedQuestionA,
              answer: true
          ),
          new AssessmentQuestionAnswerDTO(
              temperature: temperature,
              question: savedQuestionB,
              answer: true
          )
      ]
    })

    when:
    ResponseEntity<ListResponse<Temperature>> response = client.getTemperatures(savedOrg.id)

    then:
    response.statusCode == HttpStatus.OK
    response.body.total == 2
    response.body.results*.temperature.containsAll([98.6f, 100.5f])
    response.body.results*.userId.containsAll(['test-user-a', 'test-user-b'])
    response.body.results*.organizationId.unique() == [savedOrg.id]
    response.body.results*.latitude == [44.934940f, 44.934941f]
    response.body.results*.longitude == [-93.158660f, -93.158661f]
    response.body.results*.created
    response.body.results*.createdBy
    response.body.results*.lastModified
    response.body.results*.lastModifiedBy
    response.body.results.every { temperature ->
      temperature.questionAnswers.find { it.question.id == savedQuestionA.id }
      temperature.questionAnswers.find { it.question.id == savedQuestionB.id }
    }
  }

  void 'can save temperatures via the API with questions and answers'() {
    String orgAuthCode = 'auth1'
    OrganizationDTO savedOrg = organizationRepository.save(
        new OrganizationDTO(
            authorizationCode: orgAuthCode,
            taxId: '11111',
            orgName: 'testOrg',
            contactName: 'Joe',
            contactEmail: 'joe@test.com',
            approvalStatus: APPROVED,
            sector: OTHER_PRIVATE_BUSINESS
        )
    )

    AssessmentQuestion savedQuestionA = assessmentQuestionService.create(
        new AssessmentQuestion(
            displayValue: 'Wha?'
        )
    )
    AssessmentQuestion savedQuestionB = assessmentQuestionService.create(
        new AssessmentQuestion(
            displayValue: 'Huh?'
        )
    )

    List<Temperature> temperatures = [
        new Temperature(
            temperature: 98.6,
            userId: 'test-user-a',
            latitude: 44.934940,
            longitude: -93.158660,
            questionAnswers: [
              new AssessmentQuestionAnswer(
                  question: savedQuestionA,
                  answer: true
              ),
              new AssessmentQuestionAnswer(
                  question: savedQuestionB,
                  answer: true
              )
            ]
        ),
        new Temperature(
            temperature: 100.5,
            userId: 'test-user-b',
            latitude: 44.934941,
            longitude: -93.158661,
            questionAnswers: [
                new AssessmentQuestionAnswer(
                    question: savedQuestionA,
                    answer: true
                ),
                new AssessmentQuestionAnswer(
                    question: savedQuestionB,
                    answer: true
                )
            ]
        )
    ]

    when:
    ResponseEntity<List<Temperature>> response = client.saveTemperatures(orgAuthCode, temperatures)

    then:
    response.statusCode == HttpStatus.CREATED
    response.body.size() == 2

    when: 'confirm temperatures are in the db'
    Page<Temperature> retrieved = temperaturesService.getTemperaturesFor(savedOrg.id, PageRequest.of(0, 100, ASC, 'id'))

    then:
    retrieved.content.size() == 2
    retrieved.content*.id.containsAll(response.body*.id)
    retrieved.content.every { temperature ->
      temperature.questionAnswers.find { it.question.id == savedQuestionA.id } &&
          temperature.questionAnswers.find { it.question.id == savedQuestionB.id }
    }
  }

  @Unroll
  void 'save temperatures with unapproved org fails'() {
    given:
    String orgAuthCode = 'auth1'
    organizationRepository.save(
        new OrganizationDTO(
            authorizationCode: orgAuthCode,
            taxId: '11111',
            orgName: 'testOrg',
            contactName: 'Joe',
            contactEmail: 'joe@test.com',
            approvalStatus: approvalStatus,
            sector: OTHER_PRIVATE_BUSINESS
        )
    )

    List<Temperature> temperatures = [
        new Temperature(
            temperature: 98.6,
            userId: 'test-user-a',
            latitude: 44.934940,
            longitude: -93.158660
        ),
        new Temperature(
            temperature: 100.5,
            userId: 'test-user-b',
            latitude: 44.934941,
            longitude: -93.158661
        )
    ]

    when:
    client.saveTemperatures(orgAuthCode, temperatures)

    then:
    FeignException e = thrown(FeignException)
    e.status() == HttpStatus.FORBIDDEN.value()

    where:
    approvalStatus << [APPLIED, REJECTED, SUSPENDED]
  }

  void 'save temperature with an org name  does not persist name'() {
    given:
    String orgAuthCode = 'auth1'
    organizationRepository.save(
        new OrganizationDTO(
            authorizationCode: orgAuthCode,
            taxId: '11111',
            orgName: 'testOrg',
            contactName: 'Joe',
            contactEmail: 'joe@test.com',
            approvalStatus: APPROVED,
            sector: OTHER_PRIVATE_BUSINESS
        )
    )
    Temperature temperature = new Temperature(
        temperature: 100.5,
        userId: 'test-user-b',
        latitude: 44.934941,
        longitude: -93.158661,
        organizationName: 'some other name'
    )

    when:
    ResponseEntity<List<Temperature>> saved = client.saveTemperatures(orgAuthCode, [temperature])

    then:
    saved.statusCode == HttpStatus.CREATED
    saved.body.size() == 1
    saved.body.first().organizationName == 'testOrg'

    when:
    ResponseEntity<Temperature> retrieved = client.getTemperature(saved.body.id)

    then:
    retrieved.statusCode == HttpStatus.OK
    retrieved.body.organizationName == 'testOrg'
  }

  void 'can get individual temperature by id'() {
    given:
    OrganizationDTO savedOrg = organizationRepository.save(
        new OrganizationDTO(
            orgName: 'testOrg',
            approvalStatus: APPROVED,
            taxId: '111',
            contactName: 'Test User',
            contactEmail: 'testuser@email.com',
            sector: OTHER_PRIVATE_BUSINESS
        )
    )
    List<TemperatureDTO> temperatureDTOs = [
        new TemperatureDTO(
            organizationId: savedOrg.id,
            temperature: 98.6,
            userId: 'test-user-a',
            latitude: 44.934940,
            longitude: -93.158660
        ),
        new TemperatureDTO(
            organizationId: savedOrg.id,
            temperature: 100.5,
            userId: 'test-user-b',
            latitude: 44.934941,
            longitude: -93.158661
        )
    ]
    List<TemperatureDTO> saved = temperatureRepository.saveAll(temperatureDTOs)

    when:
    ResponseEntity<Temperature> gotFirstResponse = client.getTemperature(saved.first().id)

    then:
    gotFirstResponse.statusCode == HttpStatus.OK
    gotFirstResponse.body.id == saved.first().id
    gotFirstResponse.body.organizationName == 'testOrg'
    gotFirstResponse.body.questionAnswers.isEmpty()

    when:
    ResponseEntity<Temperature> gotSecondResponse = client.getTemperature(saved.last().id)

    then:
    gotSecondResponse.statusCode == HttpStatus.OK
    gotSecondResponse.body.id == saved.last().id
    gotSecondResponse.body.organizationName == 'testOrg'
    gotSecondResponse.body.questionAnswers.isEmpty()
  }

  void '404 on get by id is handled'() {
    when:
    ResponseEntity<ErrorResponse> response = client.getTemperatureNotFound('bad-id')

    then:
    response.statusCode == HttpStatus.NOT_FOUND
    response.body.message == 'Not Found'
  }

  void 'can delete individual temperature by id'() {
    given:
    String organizationId = 'testOrgD'
    List<TemperatureDTO> temperatureDTOs = [
        new TemperatureDTO(
            organizationId: organizationId,
            temperature: 98.6,
            userId: 'test-user-a',
            latitude: 44.934940,
            longitude: -93.158660
        ),
        new TemperatureDTO(
            organizationId: organizationId,
            temperature: 100.5,
            userId: 'test-user-b',
            latitude: 44.934941,
            longitude: 44.934941
        )
    ]
    List<TemperatureDTO> saved = temperatureRepository.saveAll(temperatureDTOs)

    when:
    ResponseEntity<Void> deletedFirst = client.deleteTemperature(saved.first().id)

    then:
    deletedFirst.statusCode == HttpStatus.NO_CONTENT
    deletedFirst.body == null

    and:
    temperatureRepository.findById(saved.first().id).orElse(null) == null

    when:
    ResponseEntity<Void> deletedSecond = client.deleteTemperature(saved.last().id)

    then:
    deletedSecond.statusCode == HttpStatus.NO_CONTENT
    deletedSecond.body == null

    and:
    temperatureRepository.findById(saved.last().id).orElse(null) == null
  }

  void 'get temperatures is restricted to admin user'() {
    given:
    userRoleRepository.deleteById('test-adminA@email.com')

    when:
    client.getTemperatures()

    then:
    FeignException e = thrown(FeignException)
    e.status() == HttpStatus.FORBIDDEN.value()
  }
}
