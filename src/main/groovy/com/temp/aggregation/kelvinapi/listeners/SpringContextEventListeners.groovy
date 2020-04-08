package com.temp.aggregation.kelvinapi.listeners

import com.temp.aggregation.kelvinapi.domain.AssessmentQuestionDTO
import com.temp.aggregation.kelvinapi.domain.UserRoleDTO
import com.temp.aggregation.kelvinapi.repositories.AssessmentQuestionRepository
import com.temp.aggregation.kelvinapi.repositories.UserRoleRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

import static com.temp.aggregation.kelvinapi.domain.AssessmentQuestionStatus.ENABLED
import static com.temp.aggregation.kelvinapi.domain.Role.ADMIN

@Component
class SpringContextEventListeners {
  @Value('${authorization.preauthorized-admins}')
  String[] preauthedAdmins

  @EventListener
  void applicationReadyListener(ApplicationReadyEvent applicationReadyEvent) {
    applicationReadyEvent.applicationContext.getBean(UserRoleRepository).saveAll(
        preauthedAdmins.collect {
          new UserRoleDTO(emailAddress: it, role: ADMIN)
        }
    )

    AssessmentQuestionRepository assessmentQuestionRepository =
        applicationReadyEvent.applicationContext.getBean(AssessmentQuestionRepository)
    if (assessmentQuestionRepository.findAll().isEmpty()) {
      // initialize default questions
      List<AssessmentQuestionDTO> assessmentQuestionDTOS = [
          new AssessmentQuestionDTO(
              displayValue: 'A fever of more than 100.4',
              sortPriority: 10,
              status: ENABLED
          ),
          new AssessmentQuestionDTO(
              displayValue: 'Shortness of breath',
              sortPriority: 20,
              status: ENABLED
          ),
          new AssessmentQuestionDTO(
              displayValue: 'Muscle aches',
              sortPriority: 30,
              status: ENABLED
          ),
          new AssessmentQuestionDTO(
              displayValue: 'A new cough',
              sortPriority: 40,
              status: ENABLED
          )
      ]
      assessmentQuestionRepository.saveAll(assessmentQuestionDTOS)
    }
  }
}
