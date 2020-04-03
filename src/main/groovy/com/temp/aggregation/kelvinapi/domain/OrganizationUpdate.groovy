package com.temp.aggregation.kelvinapi.domain

import javax.persistence.MappedSuperclass
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@MappedSuperclass
class OrganizationUpdate {
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
}
