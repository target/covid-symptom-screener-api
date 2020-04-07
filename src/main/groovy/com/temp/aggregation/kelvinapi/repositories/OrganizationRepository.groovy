package com.temp.aggregation.kelvinapi.repositories

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.OrganizationDTO
import org.springframework.data.jpa.repository.JpaRepository

interface OrganizationRepository extends JpaRepository<OrganizationDTO, String> {

  OrganizationDTO findByTaxId(String taxId)

  boolean existsByAuthorizationCode(String authorizationCode)

  OrganizationDTO findByApprovalStatusAndAuthorizationCode(ApprovalStatus status, String authorizationCode)
}
