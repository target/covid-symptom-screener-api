package com.temp.aggregation.kelvinapi.integration

import com.temp.aggregation.kelvinapi.domain.UserRoleDTO
import com.temp.aggregation.kelvinapi.repositories.AssessmentQuestionRepository
import com.temp.aggregation.kelvinapi.repositories.UserRoleRepository
import org.springframework.beans.factory.annotation.Autowired

import static com.temp.aggregation.kelvinapi.domain.Role.ADMIN

class ApplicationIntegrationSpec extends BaseIntegrationSpec {
  @Autowired
  UserRoleRepository userRoleRepository

  @Autowired
  AssessmentQuestionRepository assessmentQuestionRepository

  void 'got application context'() {
    expect:
    serverPort == 8080
    env.activeProfiles.toList() == ['integration']
    applicationContext != null
  }

  void 'pre-authorized admins are saved on startup'() {
    given:
    List<UserRoleDTO> all = userRoleRepository.findAll()

    expect:
    eventually {
      assert all*.role.unique() == [ADMIN]
      assert all*.emailAddress.containsAll('test-adminA@email.com', 'test-adminB@email.com')
    }
  }
}
