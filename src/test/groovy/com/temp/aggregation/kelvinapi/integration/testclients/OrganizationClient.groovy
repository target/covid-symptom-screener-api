package com.temp.aggregation.kelvinapi.integration.testclients

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.Organization
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import static org.springframework.web.bind.annotation.RequestMethod.*

@FeignClient(
    name = 'organization',
    url = '${feign.app.url}',
    decode404 = true
)
interface OrganizationClient {

  @RequestMapping(method = POST, value = '/organizations')
  ResponseEntity<Organization> createOrganization(@RequestBody Organization organization)

  @RequestMapping(method = PUT, value = '/organizations/{id}')
  ResponseEntity<Organization> updateOrganization(@PathVariable(value = 'id') String id, @RequestBody Organization organization)

  @RequestMapping(method = GET, value = '/organizations/{id}')
  ResponseEntity<Organization> getOrganization(@PathVariable(value = 'id') String id,
                                               @RequestHeader(value = 'x-organization-pin') String organizationPin
  )

  @RequestMapping(method = GET, value = '/organizations')
  @SuppressWarnings('ParameterCount')
  ResponseEntity<ListResponse<Organization>> searchOrganizations(
      @RequestParam(name = 'tax_id', required = false) String taxId,
      @RequestParam(name = 'authorization_code', required = false) String authorizationCode,
      @RequestParam(name = 'name', required = false) String orgName,
      @RequestParam(name = 'approval_status', required = false) ApprovalStatus approvalStatus,
      @RequestHeader(name = 'x-organization-pin', required = false) String organizationPin,
      Pageable pageable
  )
}
