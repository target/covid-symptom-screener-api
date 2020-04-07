package com.temp.aggregation.kelvinapi.integration.repositories

import com.temp.aggregation.kelvinapi.domain.*
import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import com.temp.aggregation.kelvinapi.repositories.AssessmentQuestionAnswerRepository
import com.temp.aggregation.kelvinapi.repositories.AssessmentQuestionRepository
import com.temp.aggregation.kelvinapi.repositories.OrganizationRepository
import com.temp.aggregation.kelvinapi.repositories.TemperatureRepository
import org.springframework.beans.factory.annotation.Autowired

import static com.temp.aggregation.kelvinapi.domain.ApprovalStatus.APPROVED

class AssessmentQuestionAnswerRepositoryIntegrationSpec extends BaseIntegrationSpec {
  @Autowired
  AssessmentQuestionAnswerRepository repository

  @Autowired
  AssessmentQuestionRepository assessmentQuestionRepository

  @Autowired
  TemperatureRepository temperatureRepository

  @Autowired
  OrganizationRepository organizationRepository

  void cleanup() {
    repository.deleteAll()
    assessmentQuestionRepository.deleteAll()
    temperatureRepository.deleteAll()
    organizationRepository.deleteAll()
  }

  void 'can save an AssessmentQuestionAnswer if temperature and question exist'() {
    given:
    OrganizationDTO savedOrganization = organizationRepository.save(
        new OrganizationDTO(
            orgName: 'testOrg',
            taxId: '111',
            contactName: 'Test Contact',
            contactEmail: 'test-org@email.com',
            sector: OrganizationSector.OTHER_PRIVATE_BUSINESS,
        )
    )
    savedOrganization.approvalStatus = APPROVED
    savedOrganization = organizationRepository.save(savedOrganization)
    TemperatureDTO savedTemperature = temperatureRepository.save(
        new TemperatureDTO(
            temperature: 98.6f,
            userId: 'test-user',
            latitude: 44.934941,
            longitude: 44.934941,
            organizationId: savedOrganization.id
        )
    )
    AssessmentQuestionDTO savedQuestion = assessmentQuestionRepository.save(
        new AssessmentQuestionDTO(
            displayValue: 'Wha?'
        )
    )

    when:
    AssessmentQuestionAnswerDTO answer = repository.save(
        new AssessmentQuestionAnswerDTO(
            temperature: savedTemperature,
            question: savedQuestion,
            answer: true

        )
    )

    then:
    answer.id
    answer.temperature.id == savedTemperature.id
    answer.question.id == savedQuestion.id

    and:
    !repository.findById(answer.id).isEmpty()
  }
}
