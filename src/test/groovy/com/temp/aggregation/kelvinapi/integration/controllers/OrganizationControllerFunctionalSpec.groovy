package com.temp.aggregation.kelvinapi.integration.controllers

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.Organization
import com.temp.aggregation.kelvinapi.domain.OrganizationUpdate
import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import com.temp.aggregation.kelvinapi.integration.testclients.OrganizationClient
import com.temp.aggregation.kelvinapi.repositories.OrganizationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Unroll

class OrganizationControllerFunctionalSpec extends BaseIntegrationSpec {

  @Autowired
  OrganizationClient client

  @Autowired
  OrganizationRepository repository

  void setup() {
    cleanup()
  }

  void cleanup() {
    repository.deleteAll()
  }

  void 'create organziation'() {
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
    ResponseEntity<Organization> response = client.getOrganization(organization.id)

    then:
    response.statusCode == HttpStatus.OK
    response.body.id == organization.id
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

  @Unroll
  void 'search organizations'() {
    setup:
    Pageable pageable = PageRequest.of(0, 20)
    repository.saveAll([
        new Organization(orgName: 'Target', authorizationCode: 'abc', taxId: '1', approvalStatus: ApprovalStatus.APPLIED),
        new Organization(orgName: 'Walmart', authorizationCode: 'cde', taxId: '2', approvalStatus: ApprovalStatus.APPROVED),
        new Organization(orgName: 'Cub Foods', authorizationCode: 'def', taxId: '3', approvalStatus: ApprovalStatus.APPLIED),
        new Organization(orgName: 'Target Plaza', authorizationCode: 'efg', taxId: '4', approvalStatus: ApprovalStatus.APPROVED),
        new Organization(orgName: 'Lunds & Byerlys', authorizationCode: 'fgh', taxId: '5', approvalStatus: ApprovalStatus.APPROVED),
    ])

    when:
    ResponseEntity<ListResponse<Organization>> response = client.searchOrganizations(taxId, authorizationCode, orgName, status, pageable)

    then:
    response.statusCode == HttpStatus.OK
    response.body.total == expectedIds.size()
    response.body.results*.taxId == expectedIds

    where:
    authorizationCode | taxId | orgName  | status                  | expectedIds
    'abc'             | null  | null     | null                    | ['1']
    null              | '2'   | null     | null                    | ['2']
    null              | null  | 'Target' | null                    | ['1', '4']
    null              | '1'   | 'Target' | null                    | ['1']
    null              | null  | null     | ApprovalStatus.APPLIED  | ['1', '3']
    null              | null  | 'Target' | ApprovalStatus.APPROVED | ['4']
    null              | null  | null     | ApprovalStatus.APPROVED | ['2', '4', '5']
  }
}
