package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.Organization
import com.temp.aggregation.kelvinapi.domain.OrganizationUpdate
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.exceptions.ServiceException
import com.temp.aggregation.kelvinapi.repositories.OrganizationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

class OrganizationServiceSpec extends Specification {

  OrganizationService service = new OrganizationService(
      repository: Mock(OrganizationRepository)
  )

  void 'create'() {
    setup:
    OrganizationUpdate update = new OrganizationUpdate(
        taxId: '123',
        contactName: 'Ops Guy',
        contactEmail: 'opsGuy@target.com',
        contactPhone: '555-555-5555',
        contactJobTitle: 'very important person',
        orgName: 'Target',
    )

    Organization expected = new Organization(id: 'o1')

    when:
    Organization organization = service.create(update)

    then:
    1 * service.repository.findByTaxId(update.taxId) >> null
    1 * service.repository.save({
      assert it.taxId == update.taxId
      assert it.contactName == update.contactName
      assert it.contactEmail == update.contactEmail
      assert it.contactPhone == update.contactPhone
      assert it.contactJobTitle == update.contactJobTitle
      assert it.orgName == update.orgName
      assert it.approvalStatus == ApprovalStatus.APPLIED
      return true
    }) >> expected
    0 * _

    organization == expected
  }

  void 'create does not allow duplicate tax id'() {
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
    service.create(update)

    then:
    ServiceException e = thrown(ServiceException)
    e.serviceError == ServiceError.ORGANIZATION_CONFLICT
    1 * service.repository.findByTaxId(update.taxId) >> new Organization()
    0 * _
  }

  void 'get organization'() {
    when:
    Organization organization = service.getOrganization('o1')

    then:
    1 * service.repository.findById('o1') >> Optional.of(new Organization(id: 'o1'))
    0 * _

    organization.id == 'o1'
  }

  void 'find by criteria'() {
    setup:
    PageRequest pageRequest = PageRequest.of(0, 20)
    Page<Organization> expected = new PageImpl<>([])

    when:
    Page<Organization> results = service.find(orgName, status, pageRequest)

    then:
    count1 * service.repository.findAllByApprovalStatusAndOrgNameContainingIgnoreCase(status, orgName, pageRequest) >> expected
    count2 * service.repository.findAllByApprovalStatus(status, pageRequest) >> expected
    count3 * service.repository.findAllByOrgNameContainingIgnoreCase(orgName, pageRequest) >> expected
    count4 * service.repository.findAll(pageRequest) >> expected
    0 * _

    results == expected

    where:
    orgName  | status                 | count1 | count2 | count3 | count4
    null     | null                   | 0      | 0      | 0      | 1
    'Target' | null                   | 0      | 0      | 1      | 0
    null     | ApprovalStatus.APPLIED | 0      | 1      | 0      | 0
    'Target' | ApprovalStatus.APPLIED | 1      | 0      | 0      | 0
  }
}
