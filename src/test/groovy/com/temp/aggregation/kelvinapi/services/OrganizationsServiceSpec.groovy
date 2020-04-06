package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.Organization
import com.temp.aggregation.kelvinapi.domain.OrganizationUpdate
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.exceptions.ServiceException
import com.temp.aggregation.kelvinapi.repositories.OrganizationRepository
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification
import spock.lang.Unroll

class OrganizationsServiceSpec extends Specification {

  OrganizationsService service = new OrganizationsService(
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

  void 'save sets an auth code when status is approved'() {
    setup:
    OrganizationUpdate update = new OrganizationUpdate(
        taxId: '123',
        orgName: 'Target',
        approvalStatus: ApprovalStatus.APPROVED
    )

    Organization expected = new Organization(id: 'o1')

    when:
    Organization organization = service.save('o1', update)

    then:
    1 * service.repository.findById('o1') >> Optional.of(expected)
    1 * service.repository.existsByAuthorizationCode(_ as String) >> true
    1 * service.repository.existsByAuthorizationCode(_ as String) >> false
    1 * service.repository.save({
      assert it.taxId == update.taxId
      assert it.orgName == update.orgName
      assert it.approvalStatus == ApprovalStatus.APPROVED
      assert it.authorizationCode
      return true
    }) >> expected
    0 * _

    organization == expected
  }

  void 'save throws exception if organization is not found'() {
    setup:
    OrganizationUpdate update = new OrganizationUpdate(
        taxId: '123',
        orgName: 'Target'
    )
    when:
    service.save('o1', update)

    then:
    1 * service.repository.findById('o1') >> Optional.empty()
    0 * _

    ServiceException e = thrown()
    e.serviceError == ServiceError.NOT_FOUND
  }

  void 'save fails when unique auth code is not generated on approve update'() {
    setup:
    OrganizationUpdate update = new OrganizationUpdate(
        taxId: '123',
        orgName: 'Target',
        approvalStatus: ApprovalStatus.APPROVED
    )

    Organization expected = new Organization(id: 'o1')

    when:
    service.save('o1', update)

    then:
    1 * service.repository.findById('o1') >> Optional.of(expected)
    11 * service.repository.existsByAuthorizationCode(_ as String) >> true
    0 * _

    ServiceException e = thrown()
    e.serviceError == ServiceError.UNEXPECTED_ERROR
  }

  void 'get organization'() {
    when:
    Organization organization = service.getOrganization('o1')

    then:
    1 * service.repository.findById('o1') >> Optional.of(new Organization(id: 'o1'))
    0 * _

    organization.id == 'o1'
  }

  @Unroll
  void 'find by criteria'() {
    setup:
    PageRequest pageRequest = PageRequest.of(0, 20)
    Page<Organization> expected = new PageImpl<>([])

    when:
    Page<Organization> results = service.find(authorizationCode, taxId, orgName, status, pageRequest)

    then:
    1 * service.repository.findAll({ Example example ->
      assert example.probe.authorizationCode == authorizationCode
      assert example.probe.taxId == taxId
      assert example.probe.orgName == orgName
      assert example.probe.approvalStatus == status
      assert example.matcher.allMatching
      assert example.matcher.propertySpecifiers.propertySpecifiers.size() == 1
      assert example.matcher.propertySpecifiers.propertySpecifiers.orgName.ignoreCase
      assert example.matcher.propertySpecifiers.propertySpecifiers.orgName.stringMatcher.name() == 'CONTAINING'
      return true
    }, pageRequest
    ) >> expected

    0 * _

    results == expected

    where:
    authorizationCode | taxId | orgName  | status
    'abcd'            | '123' | null     | null
    null              | '123' | null     | null
    null              | '123' | 'Target' | null
    null              | '123' | null     | ApprovalStatus.APPLIED
    null              | '123' | 'Target' | ApprovalStatus.APPLIED
  }

  void 'generate auth code'() {
    when: 'A code is retrieved'
    List<String> codes = (0..100000).collect {
      return service.generateCode()
    }

    then: 'Random codes matches spec'
    codes.each { String code ->
      assert code.length() == 5
      assert code.matches('^[a-z0-9]+$')
    }
    0 * _
  }
}
