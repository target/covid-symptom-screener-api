package com.temp.aggregation.kelvinapi.security

import com.temp.aggregation.kelvinapi.domain.User
import org.springframework.stereotype.Component

@Component
class RequestContext {

  private static final String USER_CONTEXT = 'userContext'

  private static final ThreadLocal<Map<String, Object>> REQUEST_CONTEXT_HOLDER = new ThreadLocal<>()

  void setUserContext(User user) {
    setField(USER_CONTEXT, user)
  }

  User getUserContext() {
    return getField(USER_CONTEXT) as User
  }

  private void setField(String propertyName, Object value) {
    if (!REQUEST_CONTEXT_HOLDER.get()) {
      REQUEST_CONTEXT_HOLDER.set([:])
    }
    REQUEST_CONTEXT_HOLDER.get().put(propertyName, value)
  }

  private Object getField(String propertyName) {
    if (!REQUEST_CONTEXT_HOLDER.get()) {
      REQUEST_CONTEXT_HOLDER.set([:])
    }
    REQUEST_CONTEXT_HOLDER.get().get(propertyName)
  }

}
