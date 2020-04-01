package com.temp.aggregation.kelvinapi.domain

import javax.persistence.MappedSuperclass
import javax.validation.constraints.NotNull

@MappedSuperclass
class OrganizationUpdate {
  @NotNull
  String taxId
  @NotNull
  String orgName
  String contactName
  String contactEmail
  String contactJobTitle
  String contactPhone
  ApprovalStatus approvalStatus = ApprovalStatus.APPLIED
}
