package com.temp.aggregation.kelvinapi.integration.testclients

import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.Role
import com.temp.aggregation.kelvinapi.domain.UserRoleDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

import static org.springframework.web.bind.annotation.RequestMethod.*

@FeignClient(name = 'user-roles',
    url = '${feign.app.url}',
    decode404 = true
)
interface UserRoleClient {
  @RequestMapping(method = GET, value = '/user-roles')
  ResponseEntity<ListResponse<UserRoleDTO>> findUserRoles(@RequestParam(value = 'role', required = false) Role role, @RequestParam(value = 'email_address', required = false) String emailAddress)

  @RequestMapping(method = GET, value = '/user-roles/current')
  ResponseEntity<UserRoleDTO> getCurrentUserRole()

  @RequestMapping(method = POST, value = '/user-roles')
  ResponseEntity<UserRoleDTO> createOrUpdateUserRole(@RequestBody UserRoleDTO userRoleUpdate)

  @RequestMapping(method = DELETE, value = '/user-roles')
  ResponseEntity<Void> deleteUserRole(@RequestParam(value = 'email_address') String emailAddress)
}
