package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.Organization
import com.temp.aggregation.kelvinapi.domain.OrganizationDTO
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

  OrganizationDTO create(OrganizationDTO organizationDTO) {
    if (repository.findByTaxId(organizationDTO.taxId)) {
      throw new ServiceException(ServiceError.ORGANIZATION_CONFLICT, organizationDTO.taxId)
    }
    Organization organization = new Organization()
    InvokerHelper.setProperties(organization, organizationDTO.properties)
    organization.approvalStatus = APPLIED
    Organization saved = repository.save(organization)
    OrganizationDTO dto = new OrganizationDTO()
    InvokerHelper.setProperties(dto, saved.properties)
    return dto
  }

  OrganizationDTO getOrganization(String id) {
    Organization organization = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'Organization')
    }
    OrganizationDTO dto = new OrganizationDTO()
    InvokerHelper.setProperties(dto, organization.properties)
    return dto
  }

  OrganizationDTO save(String id, OrganizationDTO organizationDTO) {
    Organization organization = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'Organization')
    }

    validateStateChange(organization, organizationDTO)

    if (!organization.authorizationCode && organizationDTO.approvalStatus == APPROVED) {
      organization.authorizationCode = generateAuthorizationCode()
    }

    copyUpdateablePropertiesToExisting(organization, organizationDTO)

    Organization saved = repository.save(organization)
    OrganizationDTO dto = new OrganizationDTO()
    InvokerHelper.setProperties(dto, saved.properties)
    return dto
  }

  Page<OrganizationDTO> find(String authorizationCode, String taxId, String name, ApprovalStatus status, Pageable pageable) {
    ExampleMatcher matcher = ExampleMatcher
        .matchingAll()
        .withMatcher('orgName', contains().ignoreCase())
    Organization example = new Organization(
        authorizationCode: authorizationCode,
        taxId: taxId,
        orgName: name,
        approvalStatus: status)
    Page<Organization> found = repository.findAll(Example.of(example, matcher), pageable)
    List<OrganizationDTO> organizationDTOS = found.content.collect { organization ->
      OrganizationDTO dto = new OrganizationDTO()
      InvokerHelper.setProperties(dto, organization.properties)
      return dto
    }
    return new PageImpl<>(organizationDTOS, found.pageable, found.totalElements)
  }

  OrganizationDTO getApprovedOrganizationByAuthCode(String authorizationCode) {
    Organization org = repository.findByApprovalStatusAndAuthorizationCode(APPROVED, authorizationCode)
    if (!org) {
      throw new ServiceException(ServiceError.ORGANIZATION_NOT_APPROVED)
    }
    OrganizationDTO dto = new OrganizationDTO()
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

  private void validateStateChange(Organization current, OrganizationDTO dto) {
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

  private void copyUpdateablePropertiesToExisting(Organization existing, OrganizationDTO dto) {
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
