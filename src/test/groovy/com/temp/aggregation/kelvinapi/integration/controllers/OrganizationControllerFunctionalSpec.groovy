package com.temp.aggregation.kelvinapi.integration.controllers

import com.temp.aggregation.kelvinapi.domain.*
import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import com.temp.aggregation.kelvinapi.integration.testclients.OrganizationClient
import com.temp.aggregation.kelvinapi.repositories.OrganizationRepository
import com.temp.aggregation.kelvinapi.repositories.UserRoleRepository
import feign.FeignException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Unroll

import static com.temp.aggregation.kelvinapi.domain.ApprovalStatus.APPROVED
import static com.temp.aggregation.kelvinapi.domain.Role.ADMIN

class OrganizationControllerFunctionalSpec extends BaseIntegrationSpec {

  @Autowired
  OrganizationClient client

  @Autowired
  OrganizationRepository repository

  @Autowired
  UserRoleRepository userRoleRepository

  void setup() {
    cleanup()
    // ensure test user admin role is there
    // should be written at app startup, but protect from other tests
    userRoleRepository.save(
        new UserRole(emailAddress: 'test-adminA@email.com', role: ADMIN)
    )
  }

  void cleanup() {
    repository.deleteAll()
  }

  void 'create organization'() {
    setup:
    OrganizationUpdate update = new OrganizationUpdate(
        taxId: '123',
        contactName: 'Ops Guy',
        contactEmail: 'opsGuy@target.com',
        contactPhone: '555-555-5555',
        contactJobTitle: 'very important person',
        orgName: 'Target',
    )

    when:
    ResponseEntity<Organization> response = client.createOrganization(update)

    then:
    response.statusCode == HttpStatus.CREATED
    response.body.id
    response.body.approvalStatus == ApprovalStatus.APPLIED
    response.body.created
    response.body.createdBy
    response.body.lastModified
    response.body.lastModifiedBy
  }

  void 'get organization'() {
    setup:
    Organization organization = repository.save(new Organization(orgName: 'Target', authorizationCode: 'abc', taxId: '1', approvalStatus: ApprovalStatus.APPLIED))

    when:
    ResponseEntity<Organization> response = client.getOrganization(organization.id, 'abc')

    then:
    response.statusCode == HttpStatus.OK
    response.body.id == organization.id
  }

  void 'get organization with mismatched org auth code fails for non admin'() {
    setup:
    Organization organization = repository.save(new Organization(orgName: 'Target', authorizationCode: 'abc', taxId: '1', approvalStatus: ApprovalStatus.APPLIED))
    UserRole currentTestUserRole = userRoleRepository.findById('test-adminA@email.com').orElse(null)
    userRoleRepository.deleteById('test-adminA@email.com')

    when:
    client.getOrganization(organization.id, 'def')

    then:
    FeignException e = thrown(FeignException)
    e.status() == HttpStatus.FORBIDDEN.value()

    cleanup: 'restore admin role to test user'
    userRoleRepository.save(currentTestUserRole)
  }

  void 'update organization'() {
    setup:
    Organization organization = repository.save(new Organization(orgName: 'Target', authorizationCode: 'abc', taxId: '1', approvalStatus: ApprovalStatus.APPLIED))
    OrganizationUpdate update = new OrganizationUpdate(
        taxId: '123',
        contactName: 'Ops Guy',
        contactEmail: 'opsGuy@target.com',
        contactPhone: '555-555-5555',
        contactJobTitle: 'very important person',
        orgName: 'Target',
    )

    when:
    ResponseEntity<Organization> response = client.updateOrganization(organization.id, update)

    then:
    response.statusCode == HttpStatus.OK
    response.body.id == organization.id
    response.body.taxId == update.taxId
    response.body.contactName == update.contactName
    response.body.contactEmail == update.contactEmail
    response.body.contactPhone == update.contactPhone
    response.body.contactJobTitle == update.contactJobTitle
    response.body.created
    response.body.createdBy
    response.body.lastModified
    response.body.lastModifiedBy
  }

  void 'update fails for non-admin user'() {
    given: 'remove the admin role from the test user'
    UserRole currentTestUserRole = userRoleRepository.findById('test-adminA@email.com').orElse(null)
    userRoleRepository.deleteById('test-adminA@email.com')

    when:
    client.updateOrganization(
        'some-org',
        new OrganizationUpdate(
            taxId: 'tax-id',
            orgName: 'org-name',
            approvalStatus: APPROVED
        )
    )

    then:
    FeignException e = thrown(FeignException)
    e.status() == HttpStatus.FORBIDDEN.value()

    cleanup: 'restore admin role to test user'
    userRoleRepository.save(currentTestUserRole)
  }

  @Unroll
  void 'search organizations'() {
    setup:
    Pageable pageable = PageRequest.of(0, 20)
    repository.saveAll([
        new Organization(orgName: 'Target', authorizationCode: 'abc', taxId: '1', approvalStatus: ApprovalStatus.APPLIED),
        new Organization(orgName: 'Walmart', authorizationCode: 'cde', taxId: '2', approvalStatus: APPROVED),
        new Organization(orgName: 'Cub Foods', authorizationCode: 'def', taxId: '3', approvalStatus: ApprovalStatus.APPLIED),
        new Organization(orgName: 'Target Plaza', authorizationCode: 'efg', taxId: '4', approvalStatus: APPROVED),
        new Organization(orgName: 'Lunds & Byerlys', authorizationCode: 'fgh', taxId: '5', approvalStatus: APPROVED),
    ])

    when:
    ResponseEntity<ListResponse<Organization>> response =
        client.searchOrganizations(taxId, authorizationCode, orgName, status, 'orgAuthCode', pageable)

    then:
    response.statusCode == HttpStatus.OK
    response.body.total == expectedIds.size()
    response.body.results*.taxId == expectedIds

    where:
    authorizationCode | taxId | orgName  | status                 | expectedIds
    'abc'             | null  | null     | null                   | ['1']
    null              | '2'   | null     | null                   | ['2']
    null              | null  | 'Target' | null                   | ['1', '4']
    null              | '1'   | 'Target' | null                   | ['1']
    null              | null  | null     | ApprovalStatus.APPLIED | ['1', '3']
    null              | null  | 'Target' | APPROVED               | ['4']
    null              | null  | null     | APPROVED               | ['2', '4', '5']
  }

  void 'search by non-admin user filters by org auth code in header'() {
    given: 'remove the admin role from the test user'
    UserRole currentTestUserRole = userRoleRepository.findById('test-adminA@email.com').orElse(null)
    userRoleRepository.deleteById('test-adminA@email.com')
    List<Organization> savedOrgs = repository.saveAll([
        new Organization(authorizationCode: 'orgAuthCode', taxId: 'taxId', orgName: 'test org a', approvalStatus: APPROVED),
        new Organization(authorizationCode: 'otherAuthCode', taxId: 'taxId', orgName: 'test org b', approvalStatus: APPROVED)
    ])

    when:
    ResponseEntity<ListResponse<Organization>> response = client.searchOrganizations(null, null, null, APPROVED, 'orgAuthCode', PageRequest.of(0, 10))

    then:
    response.statusCode == HttpStatus.OK
    response.body.total == 1
    response.body.results*.id == savedOrgs.findAll { it.authorizationCode == 'orgAuthCode' }*.id

    cleanup: 'restore admin role to test user'
    userRoleRepository.save(currentTestUserRole)
  }
}
