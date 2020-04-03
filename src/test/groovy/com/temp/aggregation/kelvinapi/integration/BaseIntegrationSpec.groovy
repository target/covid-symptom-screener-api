package com.temp.aggregation.kelvinapi.integration

import com.temp.aggregation.kelvinapi.KelvinApiApplication
import com.temp.aggregation.kelvinapi.security.TokenValidationService
import com.temp.aggregation.kelvinapi.domain.User
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = KelvinApiApplication)
@ActiveProfiles('integration')
@EnableFeignClients
@Stepwise
class BaseIntegrationSpec extends Specification {
  @LocalServerPort
  int serverPort

  @Autowired
  Environment env

  @Autowired
  ApplicationContext applicationContext

  @Shared
  User testUser = new User(
      userId: 'test-user-id',
      email: 'test-adminA@email.com',
      name: 'Test User',
      familyName: 'User',
      givenName: 'Test',
      locale: 'test-locale'
  )

  @SpringBean
  TokenValidationService mockTokenValidationService = Mock {
    _ * validateAuthToken(_) >> testUser
  }
}
