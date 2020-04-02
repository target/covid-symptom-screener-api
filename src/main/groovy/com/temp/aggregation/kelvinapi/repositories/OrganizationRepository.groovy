package com.temp.aggregation.kelvinapi.repositories

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.Organization
import org.springframework.data.jpa.repository.JpaRepository

interface OrganizationRepository extends JpaRepository<Organization, String> {

  Organization findByTaxId(String taxId)

  boolean existsByAuthorizationCode(String authorizationCode)

  Organization findByApprovalStatusAndAuthorizationCode(ApprovalStatus status, String authorizationCode)
}
