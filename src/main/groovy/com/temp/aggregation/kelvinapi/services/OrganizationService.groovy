package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.Organization
import com.temp.aggregation.kelvinapi.domain.OrganizationUpdate
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.exceptions.ServiceException
import com.temp.aggregation.kelvinapi.repositories.OrganizationRepository
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

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

  Page<Organization> find(String name, ApprovalStatus status, Pageable pageable) {
    // TODO simplify this. Predicates?
    if (name) {
      if (status) {
        return repository.findAllByApprovalStatusAndOrgNameContainingIgnoreCase(status, name, pageable)
      }
      return repository.findAllByOrgNameContainingIgnoreCase(name, pageable)
    }
    if (status) {
      return repository.findAllByApprovalStatus(status, pageable)
    }
    return repository.findAll(pageable)
  }
}
