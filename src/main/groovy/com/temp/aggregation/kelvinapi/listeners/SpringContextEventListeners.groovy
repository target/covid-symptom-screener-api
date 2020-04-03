package com.temp.aggregation.kelvinapi.listeners

import com.temp.aggregation.kelvinapi.domain.UserRole
import com.temp.aggregation.kelvinapi.repositories.UserRoleRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

import static com.temp.aggregation.kelvinapi.domain.Role.ADMIN

@Component
class SpringContextEventListeners {
  @Value('${authorization.preauthorized-admins}')
  String[] preauthedAdmins

  @EventListener
  void applicationReadyListener(ApplicationReadyEvent applicationReadyEvent) {
    applicationReadyEvent.applicationContext.getBean(UserRoleRepository).saveAll(
        preauthedAdmins.collect {
          new UserRole(emailAddress: it, role: ADMIN)
        }
    )
  }
}
