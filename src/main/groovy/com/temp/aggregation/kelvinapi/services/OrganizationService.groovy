package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.Organization
import com.temp.aggregation.kelvinapi.domain.OrganizationUpdate
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.exceptions.ServiceException
import com.temp.aggregation.kelvinapi.repositories.OrganizationRepository
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

import java.nio.ByteBuffer
import java.security.SecureRandom

import static com.temp.aggregation.kelvinapi.domain.ApprovalStatus.APPLIED
import static com.temp.aggregation.kelvinapi.domain.ApprovalStatus.APPROVED
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains

@Service
class OrganizationService {

  @Autowired
  OrganizationRepository repository

  private static final Random RANDOM_NUM =
      new SecureRandom(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array())

  Organization create(OrganizationUpdate organizationUpdate) {
    if (repository.findByTaxId(organizationUpdate.taxId)) {
      throw new ServiceException(ServiceError.ORGANIZATION_CONFLICT, organizationUpdate.taxId)
    }
    Organization organization = new Organization()
    InvokerHelper.setProperties(organization, organizationUpdate.properties)
    organization.approvalStatus = APPLIED
    return repository.save(organization)
  }

  Organization getOrganization(String id) {
    return repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'Organization')
    }
  }

  Organization save(String id, OrganizationUpdate organizationUpdate) {
    Organization organization = getOrganization(id)
    if (!organization.authorizationCode && organizationUpdate.approvalStatus == APPROVED) {
      organization.authorizationCode = generateAuthorizationCode()
    }

    InvokerHelper.setProperties(organization, organizationUpdate.properties)
    return repository.save(organization)
  }

  Page<Organization> find(String authorizationCode, String taxId, String name, ApprovalStatus status, Pageable pageable) {
    ExampleMatcher matcher = ExampleMatcher
        .matchingAll()
        .withMatcher('orgName', contains().ignoreCase())
    Organization example = new Organization(
        authorizationCode: authorizationCode,
        taxId: taxId,
        orgName: name,
        approvalStatus: status)
    return repository.findAll(Example.of(example, matcher), pageable)
  }

  Organization getApprovedOrganizationByAuthCode(String authorizationCode) {
    Organization org = repository.findByApprovalStatusAndAuthorizationCode(APPROVED, authorizationCode)
    if (!org) {
      throw new ServiceException(ServiceError.ORGANIZATION_NOT_APPROVED)
    }
    return org
  }

  private String generateAuthorizationCode() {
    String authCode = (0..10).findResult {
      String code = generateCode()
      return repository.existsByAuthorizationCode(code) ? null : code
    }

    if (!authCode) {
      throw new ServiceException(ServiceError.UNEXPECTED_ERROR, 'Unable to generate authorization code. Try again.')
    }
    return authCode
  }

  private String generateCode() {
    String base36 = Integer.toString(RANDOM_NUM.nextInt(Integer.MAX_VALUE), 36)
    Integer.toString(RANDOM_NUM.nextInt(Integer.MAX_VALUE), 36)
    return base36.padLeft(5, '0').take(5)
  }
}
