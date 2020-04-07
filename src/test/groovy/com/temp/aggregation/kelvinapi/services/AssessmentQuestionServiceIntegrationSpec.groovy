package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.AssessmentQuestion
import com.temp.aggregation.kelvinapi.domain.AssessmentQuestionDTO
import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import com.temp.aggregation.kelvinapi.repositories.AssessmentQuestionRepository
import org.springframework.beans.factory.annotation.Autowired

import static com.temp.aggregation.kelvinapi.domain.AssessmentQuestionStatus.DISABLED
import static com.temp.aggregation.kelvinapi.domain.AssessmentQuestionStatus.ENABLED

class AssessmentQuestionServiceIntegrationSpec extends BaseIntegrationSpec {
  @Autowired
  AssessmentQuestionRepository repository

  @Autowired
  AssessmentQuestionService service

  void cleanup() {
    repository.deleteAll()
  }

  void 'get all questions only returns ENABLED'() {
    given:
    AssessmentQuestion savedA = repository.save(
        new AssessmentQuestion(displayValue: 'What\'s up?', status: ENABLED)
    )
    AssessmentQuestion savedB = repository.save(
        new AssessmentQuestion(displayValue: 'Just what do you think you\'re doing?', status: ENABLED)
    )
    repository.save(
        new AssessmentQuestion(displayValue: 'Why would you do that?', status: DISABLED)
    )

    when:
    List<AssessmentQuestionDTO> retrieved = service.findByStatuses([ENABLED])

    then:
    retrieved.size() == 2
    retrieved*.id.contains(savedA.id)
    retrieved*.id.contains(savedB.id)
    retrieved.every { question ->
      question.id &&
          question.lastModifiedBy &&
          question.lastModified &&
          question.createdBy &&
          question.created
    }
  }

  void 'can create and defaults to ENABLED'() {
    when:
    AssessmentQuestionDTO created = service.create(
        new AssessmentQuestionDTO(
            displayValue: 'Are you talking to me?'
        )
    )

    then:
    created.status == ENABLED
  }

  void 'can set disabled by id'() {
    given:
    AssessmentQuestion saved = repository.save(
        new AssessmentQuestion(displayValue: 'What\'s up?', status: ENABLED)
    )

    when:
    AssessmentQuestionDTO disabled = service.disable(saved.id)

    then:
    disabled.status == DISABLED

    and:
    repository.findById(saved.id).orElse(null).status == DISABLED
  }
}
