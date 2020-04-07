package com.temp.aggregation.kelvinapi.controllers

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.OrganizationDTO
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.exceptions.ServiceException
import com.temp.aggregation.kelvinapi.security.UserRoleService
import com.temp.aggregation.kelvinapi.services.OrganizationsService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*

import javax.validation.Valid

import static com.temp.aggregation.kelvinapi.domain.Role.ADMIN

@RestController
@Slf4j
class OrganizationsController {
  @Autowired
  OrganizationsService service

  @Autowired
  UserRoleService userRoleService

  @InitBinder
  void initBinder(WebDataBinder dataBinder) {
    dataBinder.registerCustomEditor(ApprovalStatus, new CaseInsensitiveEnumConverter(ApprovalStatus))
  }

  @PostMapping('/organizations')
  @ResponseStatus(HttpStatus.CREATED)
  OrganizationDTO createOrganization(@Valid @RequestBody OrganizationDTO organizationDTO) {
    log.info("Request to create an organization with name ${organizationDTO.orgName}")
    return service.create(organizationDTO)
  }

  @GetMapping('/organizations/{id}')
  @ResponseStatus(HttpStatus.OK)
  OrganizationDTO getOrganization(@PathVariable(value = 'id') String id,
                                  @RequestHeader(value = 'x-organization-pin', required = false) String organizationCode
  ) {
    log.info("Request to get an organization for id $id")
    OrganizationDTO organizationDTO = service.getOrganization(id)
    if (!organizationCode || organizationDTO?.authorizationCode != organizationCode) {
      userRoleService.requireAdmin()
    }
    return organizationDTO
  }

  @PutMapping('/organizations/{id}')
  @ResponseStatus(HttpStatus.OK)
  OrganizationDTO updateOrganization(@PathVariable String id, @Valid @RequestBody OrganizationDTO organizationDTO) {
    log.info("Request to update an organization for id $id to status ${organizationDTO.approvalStatus}")
    userRoleService.requireAdmin()
    return service.save(id, organizationDTO)
  }

  @GetMapping('/organizations')
  @ResponseStatus(HttpStatus.OK)
  @SuppressWarnings('ParameterCount')
  ListResponse<OrganizationDTO> searchOrganizations(
      @RequestParam(name = 'tax_id', required = false) String taxId,
      @RequestParam(name = 'authorization_code', required = false) String authorizationCode,
      @RequestParam(name = 'name', required = false) String orgName,
      @RequestParam(name = 'approval_status', required = false) ApprovalStatus approvalStatus,
      @RequestHeader(value = 'x-organization-pin', required = false) String organizationPin,
      Pageable pageable
  ) {
    log.info('Request to list organizations')

    if (userRoleService.currentUserHasRole(ADMIN)) {
      String orgPin = authorizationCode ?: organizationPin
      Page<OrganizationDTO> page = service.find(orgPin, taxId, orgName, approvalStatus, pageable)
      return new ListResponse<OrganizationDTO>(results: page.content, total: page.totalElements)
    } else if (!organizationPin) {
      throw new ServiceException(ServiceError.UNAUTHORIZED)
    }

    // filter by auth code in header
    Page<OrganizationDTO> page = service.find(organizationPin, taxId, orgName, approvalStatus, pageable)
    return new ListResponse<OrganizationDTO>(results: page.content, total: page.totalElements)
  }
}
