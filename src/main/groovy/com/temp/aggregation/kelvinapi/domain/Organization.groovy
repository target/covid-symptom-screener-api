package com.temp.aggregation.kelvinapi.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Organization implements Audited {
  String id
  String authorizationCode
  @NotNull(message = 'tax_id must not be null')
  String taxId
  @NotBlank(message = 'org_name must not be blank')
  String orgName
  @NotBlank(message = 'contact_name must not be blank')
  String contactName
  @NotBlank(message = 'contact_email must not be blank')
  String contactEmail
  String contactJobTitle
  String contactPhone
  ApprovalStatus approvalStatus = ApprovalStatus.APPLIED
  @NotNull(message = 'sector must not be null')
  OrganizationSector sector
}
