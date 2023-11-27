package org.jembi.jempi.api.user;

import org.keycloak.representations.AccessToken;

public class User {
   private String id;
   private String username;
   private String email;
   private String familyName;
   private String givenName;

   public User(
           final String id,
           final String username,
           final String email,
           final String familyName,
           final String givenName) {
      this.setId(id);
      this.setUsername(username);
      this.setEmail(email);
      this.setFamilyName(familyName);
      this.setGivenName(givenName);
   }

   public static User buildUserFromToken(final AccessToken token) {
      String familyName = token.getFamilyName();
      String givenName = token.getGivenName();
      return new User(
            null,
            token.getPreferredUsername(),
            token.getEmail(),
            familyName != null
                  ? familyName
                  : "",
            givenName != null
                  ? givenName
                  : ""
      );
   }
   public String getUsername() {
      return username;
   }

   void setUsername(final String username) {
      this.username = username;
   }

   String getId() {
      return id;
   }

   void setId(final String id) {
      this.id = id;
   }

   public String getEmail() {
      return email;
   }

   void setEmail(final String email) {
      this.email = email;
   }

   public String getFamilyName() {
      return familyName;
   }

   void setFamilyName(final String familyName) {
      this.familyName = familyName;
   }

   public String getGivenName() {
      return givenName;
   }

   void setGivenName(final String givenName) {
      this.givenName = givenName;
   }

}
