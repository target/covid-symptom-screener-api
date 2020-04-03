package com.temp.aggregation.kelvinapi.security

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.temp.aggregation.kelvinapi.domain.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@SuppressWarnings('UnnecessarySetter')
class TokenValidationService {
  @Value('${google-auth.client-id}')
  String clientId

  User validateAuthToken(String token) {
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
        new NetHttpTransport(), new JacksonFactory()
    ).setAudience([clientId]).build()
    GoogleIdToken googleIdToken = verifier.verify(token)
    User user = null
    if (googleIdToken) {
      GoogleIdToken.Payload payload = googleIdToken.getPayload()
      user = new User(
          userId: payload.getSubject(),
          email: payload.getEmail(),
          name: payload.get('name') as String,
          familyName: payload.get('family-name') as String,
          givenName: payload.get('given-name') as String,
          locale: payload.get('locale') as String
      )
    }
    return user
  }
}
