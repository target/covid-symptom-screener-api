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

  Organization create(Organization organizationDTO) {
    if (repository.findByTaxId(organizationDTO.taxId)) {
      throw new ServiceException(ServiceError.ORGANIZATION_CONFLICT, organizationDTO.taxId)
    }
    OrganizationDTO organization = new OrganizationDTO()
    InvokerHelper.setProperties(organization, organizationDTO.properties)
    organization.approvalStatus = APPLIED
    OrganizationDTO saved = repository.save(organization)
    Organization dto = new Organization()
    InvokerHelper.setProperties(dto, saved.properties)
    return dto
  }

  Organization getOrganization(String id) {
    OrganizationDTO organization = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'Organization')
    }
    Organization dto = new Organization()
    InvokerHelper.setProperties(dto, organization.properties)
    return dto
  }

  Organization save(String id, Organization organizationDTO) {
    OrganizationDTO organization = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'Organization')
    }

    validateStateChange(organization, organizationDTO)

    if (!organization.authorizationCode && organizationDTO.approvalStatus == APPROVED) {
      organization.authorizationCode = generateAuthorizationCode()
    }

    copyUpdateablePropertiesToExisting(organization, organizationDTO)

    OrganizationDTO saved = repository.save(organization)
    Organization dto = new Organization()
    InvokerHelper.setProperties(dto, saved.properties)
    return dto
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
    List<Organization> organizationDTOS = found.content.collect { organization ->
      Organization dto = new Organization()
      InvokerHelper.setProperties(dto, organization.properties)
      return dto
    }
    return new PageImpl<>(organizationDTOS, found.pageable, found.totalElements)
  }

  Organization getApprovedOrganizationByAuthCode(String authorizationCode) {
    OrganizationDTO org = repository.findByApprovalStatusAndAuthorizationCode(APPROVED, authorizationCode)
    if (!org) {
      throw new ServiceException(ServiceError.ORGANIZATION_NOT_APPROVED)
    }
    Organization dto = new Organization()
    InvokerHelper.setProperties(dto, org.properties)
    return dto
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

  private void validateStateChange(OrganizationDTO current, Organization dto) {
    boolean valid
    switch (current.approvalStatus) {
      case APPLIED:
        valid = [APPLIED, APPROVED, REJECTED].contains(dto.approvalStatus)
        break
      case APPROVED:
        valid = [APPROVED, SUSPENDED].contains(dto.approvalStatus)
        break
      case REJECTED:
        valid = [REJECTED, APPLIED].contains(dto.approvalStatus)
        break
      case SUSPENDED:
        valid = [SUSPENDED, APPROVED].contains(dto.approvalStatus)
        break
      default:
        // we have bad data; shouldn't happen
        throw new ServiceException(ServiceError.UNEXPECTED_ERROR)
    }

    if (!valid) {
      throw new ServiceException(ServiceError.INVALID_ORGANIZATION_STATE_CHANGE)
    }
  }

  private void copyUpdateablePropertiesToExisting(OrganizationDTO existing, Organization dto) {
    existing.approvalStatus = dto.approvalStatus
    existing.contactEmail = dto.contactEmail
    existing.contactJobTitle = dto.contactJobTitle
    existing.contactName = dto.contactName
    existing.contactPhone = dto.contactPhone
    existing.sector = dto.sector
    existing.taxId = dto.taxId
    existing.orgName = dto.orgName
  }
}
