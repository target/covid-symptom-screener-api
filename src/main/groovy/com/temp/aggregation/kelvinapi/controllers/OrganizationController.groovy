package com.temp.aggregation.kelvinapi.controllers

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.Organization
import com.temp.aggregation.kelvinapi.domain.OrganizationUpdate
import com.temp.aggregation.kelvinapi.services.OrganizationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController()
class OrganizationController {
  @Autowired
  OrganizationService service

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
  Organization getOrganization(@PathVariable(value = 'id') String id) {
    return service.getOrganization(id)
  }

  @PutMapping('/organizations/{id}')
  @ResponseStatus(HttpStatus.OK)
  Organization updateOrganization(@PathVariable String id, @RequestBody OrganizationUpdate organizationUpdate) {
    return service.save(id, organizationUpdate)
  }

  @GetMapping('/organizations')
  @ResponseStatus(HttpStatus.OK)
  ListResponse<Organization> searchOrganizations(
      @RequestParam(name = 'tax_id', required = false) String taxId,
      @RequestParam(name = 'authorization_code', required = false) String authorizationCode,
      @RequestParam(name = 'name', required = false) String orgName,
      @RequestParam(name = 'approval_status', required = false) ApprovalStatus approvalStatus,
      Pageable pageable
  ) {
    Page<Organization> page = service.find(authorizationCode, taxId, orgName, approvalStatus, pageable)
    return new ListResponse<Organization>(results: page.content, total: page.totalElements)
  }
}
