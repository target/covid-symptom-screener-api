package com.temp.aggregation.kelvinapi.domain

import javax.validation.constraints.NotNull

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
