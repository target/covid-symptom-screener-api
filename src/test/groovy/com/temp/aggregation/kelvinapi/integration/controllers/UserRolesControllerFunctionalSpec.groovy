package com.temp.aggregation.kelvinapi.integration.controllers

import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.UserRoleDTO
import com.temp.aggregation.kelvinapi.domain.UserRole
import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import com.temp.aggregation.kelvinapi.integration.testclients.UserRoleClient
import com.temp.aggregation.kelvinapi.repositories.UserRoleRepository
import feign.FeignException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Unroll

import static com.temp.aggregation.kelvinapi.domain.Role.ADMIN
import static com.temp.aggregation.kelvinapi.domain.Role.USER

class UserRolesControllerFunctionalSpec extends BaseIntegrationSpec {
  @Autowired
  UserRoleRepository userRoleRepository

  @Autowired
  UserRoleClient client

  void setup() {
    // ensure test user admin role is there
    // should be written at app startup, but protect from other tests
    userRoleRepository.save(
        new UserRoleDTO(emailAddress: 'test-adminA@email.com', role: ADMIN)
    )
  }

  void 'can get UserRole for current user'() {
    when:
    ResponseEntity<UserRole> userRoleResponse = client.getCurrentUserRole()

    then:
    userRoleResponse.statusCode == HttpStatus.OK
    userRoleResponse.body.emailAddress == 'test-adminA@email.com'
  }

  void 'can get a list of all user roles'() {
    given:
    userRoleRepository.saveAll([
        new UserRoleDTO(emailAddress: 'testA@email.com', role: ADMIN),
        new UserRoleDTO(emailAddress: 'testB@email.com', role: ADMIN)
    ])

    when:
    ResponseEntity<ListResponse<UserRole>> response = client.findUserRoles(null, null)

    then:
    response.statusCode == HttpStatus.OK
    response.body.results.find { it.emailAddress == 'testA@email.com' }
    response.body.results.find { it.emailAddress == 'testB@email.com' }

    cleanup:
    // deleting all will break tests for pre-authorized admins
    userRoleRepository.deleteById('testA@email.com')
    userRoleRepository.deleteById('testB@email.com')
  }

  @Unroll
  void 'can get a list of user roles by role, email address, or all'() {
    given:
    userRoleRepository.saveAll([
        new UserRoleDTO(emailAddress: 'testA@email.com', role: ADMIN),
        new UserRoleDTO(emailAddress: 'testB@email.com', role: ADMIN)
    ])

    when:
    ResponseEntity<ListResponse<UserRole>> response = client.findUserRoles(role, emailAddress)

    then:
    response.statusCode == HttpStatus.OK
    response.body.results*.emailAddress.containsAll(expected)
    response.body.results*.role.unique() == [ADMIN]
    response.body.results*.created
    response.body.results*.createdBy
    response.body.results*.lastModified
    response.body.results*.lastModifiedBy

    cleanup:
    // deleting all will break tests for pre-authorized admins
    userRoleRepository.deleteById('testA@email.com')
    userRoleRepository.deleteById('testB@email.com')

    where:
    role  | emailAddress      | expected
    null  | null              | ['testA@email.com', 'testB@email.com']
    ADMIN | null              | ['testA@email.com', 'testB@email.com']
    null  | 'testA@email.com' | ['testA@email.com']
    null  | 'testB@email.com' | ['testB@email.com']
    ADMIN | 'testA@email.com' | ['testA@email.com']
  }

  void 'can save a user role'() {
    given:
    userRoleRepository.saveAll([
        new UserRoleDTO(emailAddress: 'testA@email.com', role: ADMIN),
        new UserRoleDTO(emailAddress: 'testB@email.com', role: ADMIN)
    ])

    when: 'save new role'
    ResponseEntity<UserRole> newUserRoleResponse = client.createOrUpdateUserRole(
        new UserRole(emailAddress: 'testC@email.com', role: ADMIN)
    )

    then:
    newUserRoleResponse.statusCode == HttpStatus.CREATED
    userRoleRepository.findById('testC@email.com')

    when: 'save over existing'
    ResponseEntity<UserRole> resavedUserRoleResponse = client.createOrUpdateUserRole(
        new UserRole(emailAddress: 'testA@email.com', role: ADMIN)
    )

    then:
    resavedUserRoleResponse.statusCode == HttpStatus.CREATED
    userRoleRepository.findById('testA@email.com')

    cleanup:
    // deleting all will break tests for pre-authorized admins
    userRoleRepository.deleteById('testA@email.com')
    userRoleRepository.deleteById('testB@email.com')
    userRoleRepository.deleteById('testC@email.com')
  }

  void 'save fails for non-admin user'() {
    given: 'remove the admin role from the test user'
    UserRoleDTO currentTestUserRole = userRoleRepository.findById('test-adminA@email.com').orElse(null)
    userRoleRepository.deleteById('test-adminA@email.com')

    when:
    client.createOrUpdateUserRole(new UserRole(emailAddress: 'any@email.com', role: ADMIN))

    then:
    FeignException e = thrown(FeignException)
    e.status() == HttpStatus.FORBIDDEN.value()

    cleanup: 'restore admin role for test user'
    userRoleRepository.save(currentTestUserRole)
  }

  void 'can delete a user role'() {
    given:
    userRoleRepository.saveAll([
        new UserRoleDTO(emailAddress: 'testA@email.com', role: ADMIN),
        new UserRoleDTO(emailAddress: 'testB@email.com', role: ADMIN)
    ])

    when:
    ResponseEntity<Void> response = client.deleteUserRole('testA@email.com')

    then:
    response.statusCode == HttpStatus.NO_CONTENT

    and:
    !userRoleRepository.findAll().find { it.emailAddress == 'testA@email.com' }

    cleanup:
    // deleting all will break tests for pre-authorized admins
    userRoleRepository.deleteById('testB@email.com')
  }

  void 'delete fails for non-admin user'() {
    given: 'remove the admin role from the test user'
    UserRoleDTO currentTestUserRole = userRoleRepository.findById('test-adminA@email.com').orElse(null)
    userRoleRepository.deleteById('test-adminA@email.com')

    when:
    client.deleteUserRole('any@email.com')

    then:
    FeignException e = thrown(FeignException)
    e.status() == HttpStatus.FORBIDDEN.value()

    cleanup: 'restore admin role for test user'
    userRoleRepository.save(currentTestUserRole)
  }

  void 'get current user role returns saved role for ADMIN'() {
    when:
    ResponseEntity<UserRole> response = client.getCurrentUserRole()

    then:
    response.statusCode == HttpStatus.OK
    response.body.emailAddress == 'test-adminA@email.com'
    response.body.role == ADMIN
  }

  void 'get current user role returns USER of not an admin'() {
    given: 'remove the admin role from the test user'
    UserRoleDTO currentTestUserRole = userRoleRepository.findById('test-adminA@email.com').orElse(null)
    userRoleRepository.deleteById('test-adminA@email.com')

    when:
    ResponseEntity<UserRole> response = client.getCurrentUserRole()

    then:
    response.statusCode == HttpStatus.OK
    response.body.emailAddress == 'test-adminA@email.com'
    response.body.role == USER

    cleanup: 'restore admin role for test user'
    userRoleRepository.save(currentTestUserRole)
  }
}
