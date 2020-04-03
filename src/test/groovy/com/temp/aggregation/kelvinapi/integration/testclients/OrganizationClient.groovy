package com.temp.aggregation.kelvinapi.integration.testclients

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.Organization
import com.temp.aggregation.kelvinapi.domain.OrganizationUpdate
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.POST
import static org.springframework.web.bind.annotation.RequestMethod.PUT

@FeignClient(
    name = 'organization',
    url = '${feign.app.url}',
    decode404 = true
)
interface OrganizationClient {

  @RequestMapping(method = POST, value = '/organizations')
  ResponseEntity<Organization> createOrganization(@RequestBody OrganizationUpdate organizationUpdate)

  @RequestMapping(method = PUT, value = '/organizations/{id}')
  ResponseEntity<Organization> updateOrganization(@PathVariable(value = 'id') String id, @RequestBody OrganizationUpdate organizationUpdate)

  @RequestMapping(method = GET, value = '/organizations/{id}')
  ResponseEntity<Organization> getOrganization(@PathVariable(value = 'id') String id,
                                               @RequestHeader(value = 'x-authorization-code') String organizationAuthCode
  )

  @RequestMapping(method = GET, value = '/organizations')
  @SuppressWarnings('ParameterCount')
  ResponseEntity<ListResponse<Organization>> searchOrganizations(
      @RequestParam(name = 'tax_id', required = false) String taxId,
      @RequestParam(name = 'authorization_code', required = false) String authorizationCode,
      @RequestParam(name = 'name', required = false) String orgName,
      @RequestParam(name = 'approval_status', required = false) ApprovalStatus approvalStatus,
      @RequestHeader(name = 'x-authorization-code', required = false) String organizationAuthCode,
      Pageable pageable
  )
}
