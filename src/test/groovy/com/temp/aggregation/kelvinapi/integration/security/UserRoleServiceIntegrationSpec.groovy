package com.temp.aggregation.kelvinapi.integration.security

import com.temp.aggregation.kelvinapi.domain.UserRole
import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import com.temp.aggregation.kelvinapi.repositories.UserRoleRepository
import com.temp.aggregation.kelvinapi.security.UserRoleService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import static com.temp.aggregation.kelvinapi.domain.Role.ADMIN

class UserRoleServiceIntegrationSpec extends BaseIntegrationSpec {
  @Autowired
  UserRoleRepository userRoleRepository

  @Autowired
  UserRoleService service

  void cleanup() {
    userRoleRepository.deleteAll()
  }

  @Unroll
  void 'can check if user has role'() {
    given:
    userRoleRepository.saveAll([
        new UserRole(emailAddress: 'testA@email.com', role: ADMIN),
        new UserRole(emailAddress: 'testB@email.com', role: ADMIN)
    ])

    expect:
    service.userHasRole(email, role) == expected

    where:
    email             | role  | expected
    'testA@email.com' | ADMIN | true
    'testB@email.com' | ADMIN | true
    'testA@email.com' | null  | false
    'bad@email.com'   | ADMIN | false
  }
}
