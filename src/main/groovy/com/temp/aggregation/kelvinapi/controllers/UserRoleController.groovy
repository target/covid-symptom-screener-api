package com.temp.aggregation.kelvinapi.controllers

import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.Role
import com.temp.aggregation.kelvinapi.domain.UserRole
import com.temp.aggregation.kelvinapi.domain.UserRoleUpdate
import com.temp.aggregation.kelvinapi.security.UserRoleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*

@RestController
class UserRoleController {
  @Autowired
  UserRoleService userRoleService

  @InitBinder
  void initBinder(WebDataBinder dataBinder) {
    dataBinder.registerCustomEditor(Role, new CaseInsensitiveEnumConverter(Role))
  }

  @GetMapping('/user-roles')
  @ResponseStatus(HttpStatus.OK)
  ListResponse<UserRole> findUserRoles(@RequestParam(name = 'role', required = false) Role role,
                                       @RequestParam(name = 'email_address', required = false) String emailAddress,
                                       Pageable pageable) {
    Page<UserRole> page = userRoleService.findBy(role, emailAddress, pageable)
    return new ListResponse<UserRole>(results: page.content, total: page.totalElements)
  }

  @GetMapping('/user-roles/current')
  @ResponseStatus(HttpStatus.OK)
  UserRole getCurrentUserRole() {
    return userRoleService.getCurrentUserRole()
  }

  @PostMapping('/user-roles')
  @ResponseStatus(HttpStatus.CREATED)
  UserRole createOrUpdateUserRole(@RequestBody UserRoleUpdate userRoleUpdate) {
    userRoleService.requireAdmin()
    return userRoleService.save(userRoleUpdate)
  }

  @DeleteMapping('/user-roles')
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void deleteUserRole(@RequestParam(value = 'email_address') String emailAddress) {
    userRoleService.requireAdmin()
    userRoleService.deleteUserRole(emailAddress)
  }
}
