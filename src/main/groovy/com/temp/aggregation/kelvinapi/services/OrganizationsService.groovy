package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.OrganizationDTO
import com.temp.aggregation.kelvinapi.domain.Organization
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.exceptions.ServiceException
import com.temp.aggregation.kelvinapi.repositories.OrganizationRepository
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.*
import org.springframework.stereotype.Service

import java.nio.ByteBuffer
import java.security.SecureRandom

import static com.temp.aggregation.kelvinapi.domain.ApprovalStatus.*
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains

@Service
class OrganizationsService {

  @Autowired
  OrganizationRepository repository

  private static final Random RANDOM_NUM =
      new SecureRandom(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array())

  Organization create(Organization organization) {
    if (repository.findByTaxId(organization.taxId)) {
      throw new ServiceException(ServiceError.ORGANIZATION_CONFLICT, organization.taxId)
    }
    OrganizationDTO organizationDTO = new OrganizationDTO()
    InvokerHelper.setProperties(organizationDTO, organization.properties)
    organizationDTO.approvalStatus = APPLIED
    OrganizationDTO saved = repository.save(organizationDTO)
    Organization created = new Organization()
    InvokerHelper.setProperties(created, saved.properties)
    return created
  }

  Organization getOrganization(String id) {
    OrganizationDTO organizationDTO = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'Organization')
    }
    Organization organization = new Organization()
    InvokerHelper.setProperties(organization, organizationDTO.properties)
    return organization
  }

  Organization save(String id, Organization organization) {
    OrganizationDTO organizationDTO = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'Organization')
    }

    validateStateChange(organizationDTO, organization)

    if (!organizationDTO.authorizationCode && organization.approvalStatus == APPROVED) {
      organizationDTO.authorizationCode = generateAuthorizationCode()
    }

    copyUpdateablePropertiesToExisting(organizationDTO, organization)

    OrganizationDTO saved = repository.save(organizationDTO)
    Organization updated = new Organization()
    InvokerHelper.setProperties(updated, saved.properties)
    return updated
  }

  Page<Organization> find(String authorizationCode, String taxId, String name, ApprovalStatus status, Pageable pageable) {
    ExampleMatcher matcher = ExampleMatcher
        .matchingAll()
        .withMatcher('orgName', contains().ignoreCase())
    OrganizationDTO example = new OrganizationDTO(
        authorizationCode: authorizationCode,
        taxId: taxId,
        orgName: name,
        approvalStatus: status)
    Page<OrganizationDTO> found = repository.findAll(Example.of(example, matcher), pageable)
    List<Organization> organizations = found.content.collect { organizationDTO ->
      Organization organization = new Organization()
      InvokerHelper.setProperties(organization, organizationDTO.properties)
      return organization
    }
    return new PageImpl<>(organizations, found.pageable, found.totalElements)
  }

  Organization getApprovedOrganizationByAuthCode(String authorizationCode) {
    OrganizationDTO organizationDTO = repository.findByApprovalStatusAndAuthorizationCode(APPROVED, authorizationCode)
    if (!organizationDTO) {
      throw new ServiceException(ServiceError.ORGANIZATION_NOT_APPROVED)
    }
    Organization organization = new Organization()
    InvokerHelper.setProperties(organization, organizationDTO.properties)
    return organization
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

  private void validateStateChange(OrganizationDTO current, Organization organization) {
    boolean valid
    switch (current.approvalStatus) {
      case APPLIED:
        valid = [APPLIED, APPROVED, REJECTED].contains(organization.approvalStatus)
        break
      case APPROVED:
        valid = [APPROVED, SUSPENDED].contains(organization.approvalStatus)
        break
      case REJECTED:
        valid = [REJECTED, APPLIED].contains(organization.approvalStatus)
        break
      case SUSPENDED:
        valid = [SUSPENDED, APPROVED].contains(organization.approvalStatus)
        break
      default:
        // we have bad data; shouldn't happen
        throw new ServiceException(ServiceError.UNEXPECTED_ERROR)
    }

    if (!valid) {
      throw new ServiceException(ServiceError.INVALID_ORGANIZATION_STATE_CHANGE)
    }
  }

  private void copyUpdateablePropertiesToExisting(OrganizationDTO existing, Organization organization) {
    existing.approvalStatus = organization.approvalStatus
    existing.contactEmail = organization.contactEmail
    existing.contactJobTitle = organization.contactJobTitle
    existing.contactName = organization.contactName
    existing.contactPhone = organization.contactPhone
    existing.sector = organization.sector
    existing.taxId = organization.taxId
    existing.orgName = organization.orgName
  }
}
