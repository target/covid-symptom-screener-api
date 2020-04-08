package com.temp.aggregation.kelvinapi.integration.controllers

import com.temp.aggregation.kelvinapi.domain.*
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
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

import static com.temp.aggregation.kelvinapi.domain.ApprovalStatus.APPLIED
import static com.temp.aggregation.kelvinapi.domain.ApprovalStatus.APPROVED
import static com.temp.aggregation.kelvinapi.domain.ApprovalStatus.REJECTED
import static com.temp.aggregation.kelvinapi.domain.ApprovalStatus.SUSPENDED
import static com.temp.aggregation.kelvinapi.domain.OrganizationSector.OTHER_PRIVATE_BUSINESS
import static com.temp.aggregation.kelvinapi.domain.Role.ADMIN

class OrganizationsControllerFunctionalSpec extends BaseIntegrationSpec {

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
        new UserRoleDTO(emailAddress: 'test-adminA@email.com', role: ADMIN)
    )
  }

  void cleanup() {
    repository.deleteAll()
    userRoleRepository.deleteAll()
  }

  void 'create organization'() {
    setup:
    Organization update = new Organization(
        taxId: '123',
        contactName: 'Ops Guy',
        contactEmail: 'opsGuy@target.com',
        contactPhone: '555-555-5555',
        contactJobTitle: 'very important person',
        orgName: 'Target',
        sector: OTHER_PRIVATE_BUSINESS
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
    OrganizationDTO organizationDTO = repository.save(new OrganizationDTO(
        orgName: 'Target',
        authorizationCode: 'abc',
        taxId: '1',
        contactName: 'Joe',
        contactEmail: 'joe@test.com',
        approvalStatus: ApprovalStatus.APPLIED,
        sector: OTHER_PRIVATE_BUSINESS)
    )

    when:
    ResponseEntity<Organization> response = client.getOrganization(organizationDTO.id, 'abc')

    then:
    response.statusCode == HttpStatus.OK
    response.body.id == organizationDTO.id
  }

  void 'get organization with mismatched org auth code fails for non admin'() {
    setup:
    OrganizationDTO organizationDTO = repository.save(new OrganizationDTO(
        orgName: 'Target',
        authorizationCode: 'abc',
        taxId: '1',
        contactName: 'Joe',
        contactEmail: 'joe@test.com',
        approvalStatus: ApprovalStatus.APPLIED,
        sector: OTHER_PRIVATE_BUSINESS)
    )
    UserRoleDTO currentTestUserRole = userRoleRepository.findById('test-adminA@email.com').orElse(null)
    userRoleRepository.deleteById('test-adminA@email.com')

    when:
    client.getOrganization(organizationDTO.id, 'def')

    then:
    FeignException e = thrown(FeignException)
    e.status() == HttpStatus.FORBIDDEN.value()

    cleanup: 'restore admin role to test user'
    userRoleRepository.save(currentTestUserRole)
  }

  void 'update organization succeeds'() {
    setup:
    OrganizationDTO organizationDTO = repository.save(new OrganizationDTO(
        orgName: 'Target',
        authorizationCode: 'abc',
        taxId: '1',
        contactName: 'Joe',
        contactEmail: 'joe@test.com',
        approvalStatus: ApprovalStatus.APPLIED,
        sector: OTHER_PRIVATE_BUSINESS)
    )
    Organization update = new Organization(
        taxId: '123',
        contactName: 'Ops Guy',
        contactEmail: 'opsGuy@target.com',
        contactPhone: '555-555-5555',
        contactJobTitle: 'very important person',
        orgName: 'Target',
        sector: OTHER_PRIVATE_BUSINESS
    )

    when:
    ResponseEntity<Organization> response = client.updateOrganization(organizationDTO.id, update)

    then:
    response.statusCode == HttpStatus.OK
    response.body.id == organizationDTO.id
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

  @Unroll
  void 'organization update with invalid state change throws exception'() {
    given:
    OrganizationDTO organizationDTO = repository.save(new OrganizationDTO(
        orgName: 'Target',
        authorizationCode: 'abc',
        taxId: '123',
        contactName: 'Joe',
        contactPhone: '555-555-5555',
        contactEmail: 'joe@test.com',
        approvalStatus: initialStatus,
        sector: OTHER_PRIVATE_BUSINESS)
    )
    Organization update = new Organization(
        taxId: '123',
        contactName: 'Joe',
        contactEmail: 'joe@target.com',
        contactPhone: '555-555-5555',
        contactJobTitle: 'very important person',
        orgName: 'Target',
        approvalStatus: newStatus,
        sector: OTHER_PRIVATE_BUSINESS
    )

    when:
    client.updateOrganization(organizationDTO.id, update)

    then:
    FeignException e = thrown(FeignException)
    e.status() == ServiceError.INVALID_ORGANIZATION_STATE_CHANGE.httpStatus.value()

    cleanup:
    repository.deleteById(organizationDTO.id)

    where:
    initialStatus | newStatus
    APPLIED       | SUSPENDED
    APPROVED      | APPLIED
    APPROVED      | REJECTED
    REJECTED      | APPROVED
    REJECTED      | SUSPENDED
    SUSPENDED     | APPLIED
    SUSPENDED     | REJECTED
  }

  void 'update fails for non-admin user'() {
    given: 'remove the admin role from the test user'
    UserRoleDTO currentTestUserRole = userRoleRepository.findById('test-adminA@email.com').orElse(null)
    userRoleRepository.deleteById('test-adminA@email.com')

    when:
    client.updateOrganization(
        'some-org',
        new Organization(
            taxId: 'tax-id',
            orgName: 'org-name',
            approvalStatus: APPROVED,
            sector: OTHER_PRIVATE_BUSINESS,
            contactName: 'Test Contact',
            contactEmail: 'test-contact@email.com'
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
        new OrganizationDTO(
            orgName: 'Target',
            authorizationCode: 'abc',
            taxId: '1',
            contactName: 'Joe',
            contactEmail: 'joe@test.com',
            approvalStatus: ApprovalStatus.APPLIED,
            sector: OTHER_PRIVATE_BUSINESS
        ),
        new OrganizationDTO(
            orgName: 'Walmart',
            authorizationCode: 'cde',
            taxId: '2',
            contactName: 'Joe',
            contactEmail: 'joe@test.com',
            approvalStatus: APPROVED,
            sector: OTHER_PRIVATE_BUSINESS
        ),
        new OrganizationDTO(
            orgName: 'Cub Foods',
            authorizationCode: 'def',
            taxId: '3',
            contactName: 'Joe',
            contactEmail: 'joe@test.com',
            approvalStatus: ApprovalStatus.APPLIED,
            sector: OTHER_PRIVATE_BUSINESS
        ),
        new OrganizationDTO(
            orgName: 'Target Plaza',
            authorizationCode: 'efg',
            taxId: '4',
            contactName: 'Joe',
            contactEmail: 'joe@test.com',
            approvalStatus: APPROVED,
            sector: OTHER_PRIVATE_BUSINESS
        ),
        new OrganizationDTO(
            orgName: 'Lunds & Byerlys',
            authorizationCode: 'fgh',
            taxId: '5',
            contactName: 'Joe',
            contactEmail: 'joe@test.com',
            approvalStatus: APPROVED,
            sector: OTHER_PRIVATE_BUSINESS
        ),
    ])

    when:
    ResponseEntity<ListResponse<Organization>> response =
        client.searchOrganizations(taxId, authorizationCode, orgName, status, orgPin, pageable)

    then:
    response.statusCode == HttpStatus.OK
    response.body.total == expectedIds.size()
    response.body.results*.taxId.containsAll(expectedIds)

    where:
    authorizationCode | orgPin | taxId | orgName  | status   | expectedIds
    'abc'             | null   | null  | null     | null     | ['1']
    'abc'             | 'fgh'  | null  | null     | null     | ['1']
    null              | 'fgh'  | null  | null     | null     | ['5']
    null              | null   | '2'   | null     | null     | ['2']
    null              | null   | null  | 'Target' | null     | ['1', '4']
    null              | null   | '1'   | 'Target' | null     | ['1']
    null              | null   | null  | null     | APPLIED  | ['1', '3']
    null              | null   | null  | 'Target' | APPROVED | ['4']
    null              | null   | null  | null     | APPROVED | ['2', '4', '5']
  }

  void 'search by non-admin user filters by org auth code in header'() {
    given: 'remove the admin role from the test user'
    UserRoleDTO currentTestUserRole = userRoleRepository.findById('test-adminA@email.com').orElse(null)
    userRoleRepository.deleteById('test-adminA@email.com')
    List<OrganizationDTO> organizationDTOs = repository.saveAll([
        new OrganizationDTO(
            authorizationCode: 'orgAuthCode',
            taxId: 'taxIdA',
            orgName: 'test org a',
            contactName: 'Joe',
            contactEmail: 'joe@test.com',
            approvalStatus: APPROVED,
            sector: OTHER_PRIVATE_BUSINESS
        ),
        new OrganizationDTO(
            authorizationCode: 'otherAuthCode',
            taxId: 'taxIdB',
            orgName: 'test org b',
            contactName: 'Joe',
            contactEmail: 'joe@test.com',
            approvalStatus: APPROVED,
            sector: OTHER_PRIVATE_BUSINESS
        )
    ])

    when:
    ResponseEntity<ListResponse<Organization>> response = client.searchOrganizations(null, null, null, APPROVED, 'orgAuthCode', PageRequest.of(0, 10))

    then:
    response.statusCode == HttpStatus.OK
    response.body.total == 1
    response.body.results*.id == organizationDTOs.findAll { it.authorizationCode == 'orgAuthCode' }*.id

    cleanup: 'restore admin role to test user'
    userRoleRepository.save(currentTestUserRole)
  }

  void 'search by non-admin requires auth code in header'() {
    given: 'remove the admin role from the test user'
    UserRoleDTO currentTestUserRole = userRoleRepository.findById('test-adminA@email.com').orElse(null)
    userRoleRepository.deleteById('test-adminA@email.com')

    when:
    client.searchOrganizations(null, 'orgAuthCode', null, APPROVED, null, PageRequest.of(0, 10))

    then:
    FeignException e = thrown(FeignException)
    e.status() == HttpStatus.FORBIDDEN.value()

    cleanup: 'restore admin role to test user'
    userRoleRepository.save(currentTestUserRole)
  }
}
