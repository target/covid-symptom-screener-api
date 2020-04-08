package com.temp.aggregation.kelvinapi.controllers

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.Organization
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.exceptions.ServiceException
import com.temp.aggregation.kelvinapi.security.UserRoleService
import com.temp.aggregation.kelvinapi.services.OrganizationsService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*

import javax.validation.Valid

import static com.temp.aggregation.kelvinapi.domain.Role.ADMIN

@RestController
@Slf4j
@Validated
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
  Organization createOrganization(@Valid @RequestBody Organization organization) {
    log.info("Request to create an organization with name ${organization.orgName}")
    return service.create(organization)
  }

  @GetMapping('/organizations/{id}')
  @ResponseStatus(HttpStatus.OK)
  Organization getOrganization(@PathVariable(value = 'id') String id,
                               @RequestHeader(value = 'x-organization-pin', required = false) String organizationCode
  ) {
    log.info("Request to get an organization for id $id")
    Organization organization = service.getOrganization(id)
    if (!organizationCode || organization?.authorizationCode != organizationCode) {
      userRoleService.requireAdmin()
    }
    return organization
  }

  @PutMapping('/organizations/{id}')
  @ResponseStatus(HttpStatus.OK)
  Organization updateOrganization(@PathVariable String id, @Valid @RequestBody Organization organization) {
    log.info("Request to update an organization for id $id to status ${organization.approvalStatus}")
    userRoleService.requireAdmin()
    return service.save(id, organization)
  }

  @GetMapping('/organizations')
  @ResponseStatus(HttpStatus.OK)
  @SuppressWarnings('ParameterCount')
  ListResponse<Organization> searchOrganizations(
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
      Page<Organization> page = service.find(orgPin, taxId, orgName, approvalStatus, pageable)
      return new ListResponse<Organization>(results: page.content, total: page.totalElements)
    } else if (!organizationPin) {
      throw new ServiceException(ServiceError.UNAUTHORIZED)
    }

    // filter by auth code in header
    Page<Organization> page = service.find(organizationPin, taxId, orgName, approvalStatus, pageable)
    return new ListResponse<Organization>(results: page.content, total: page.totalElements)
  }
}
