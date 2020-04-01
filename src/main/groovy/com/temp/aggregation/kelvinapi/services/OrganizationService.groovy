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

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains

@Service
class OrganizationService {

  @Autowired
  OrganizationRepository repository

  Organization create(OrganizationUpdate organizationUpdate) {
    if (repository.findByTaxId(organizationUpdate.taxId)) {
      throw new ServiceException(ServiceError.ORGANIZATION_CONFLICT, organizationUpdate.taxId)
    }
    Organization organization = new Organization()
    InvokerHelper.setProperties(organization, organizationUpdate.properties)
    return repository.save(organization)
  }

  Organization getOrganization(String id) {
    return repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'Organization')
    }
  }

  Organization save(String id, OrganizationUpdate organizationUpdate) {
    Organization organization = getOrganization(id)
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
}
