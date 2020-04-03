package com.temp.aggregation.kelvinapi.controllers

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.Organization
import com.temp.aggregation.kelvinapi.domain.OrganizationUpdate
import com.temp.aggregation.kelvinapi.security.UserRoleService
import com.temp.aggregation.kelvinapi.services.OrganizationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*

import static com.temp.aggregation.kelvinapi.domain.Role.ADMIN

@RestController
class OrganizationController {
  @Autowired
  OrganizationService service

  @Autowired
  UserRoleService userRoleService

  @InitBinder
  void initBinder(WebDataBinder dataBinder) {
    dataBinder.registerCustomEditor(ApprovalStatus, new CaseInsensitiveEnumConverter(ApprovalStatus))
  }

  @PostMapping('/organizations')
  @ResponseStatus(HttpStatus.CREATED)
  Organization createOrganization(@RequestBody OrganizationUpdate organizationUpdate) {
    return service.create(organizationUpdate)
  }

  @GetMapping('/organizations/{id}')
  @ResponseStatus(HttpStatus.OK)
  Organization getOrganization(@PathVariable(value = 'id') String id,
                               @RequestHeader(value = 'x-authorization-code', required = false) String organizationCode
  ) {
    Organization organization = service.getOrganization(id)
    if (!organizationCode || organization?.authorizationCode != organizationCode) {
      userRoleService.requireAdmin()
    }
    return organization
  }

  @PutMapping('/organizations/{id}')
  @ResponseStatus(HttpStatus.OK)
  Organization updateOrganization(@PathVariable String id, @RequestBody OrganizationUpdate organizationUpdate) {
    userRoleService.requireAdmin()
    return service.save(id, organizationUpdate)
  }

  @GetMapping('/organizations')
  @ResponseStatus(HttpStatus.OK)
  @SuppressWarnings('ParameterCount')
  ListResponse<Organization> searchOrganizations(
      @RequestParam(name = 'tax_id', required = false) String taxId,
      @RequestParam(name = 'authorization_code', required = false) String authorizationCode,
      @RequestParam(name = 'name', required = false) String orgName,
      @RequestParam(name = 'approval_status', required = false) ApprovalStatus approvalStatus,
      @RequestHeader(value = 'x-authorization-code', required = false) String organisationCode,
      Pageable pageable
  ) {
    if (userRoleService.currentUserHasRole(ADMIN)) {
      Page<Organization> page = service.find(authorizationCode, taxId, orgName, approvalStatus, pageable)
      return new ListResponse<Organization>(results: page.content, total: page.totalElements)
    }
    // filter by auth code in header
    Page<Organization> page = service.find(organisationCode, taxId, orgName, approvalStatus, pageable)
    return new ListResponse<Organization>(results: page.content, total: page.totalElements)
  }
}
