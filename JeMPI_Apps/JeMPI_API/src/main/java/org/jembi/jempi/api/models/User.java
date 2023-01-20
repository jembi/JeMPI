package org.jembi.jempi.api.models;

import org.keycloak.representations.AccessToken;

public class User {
    private String id;
    private String username;
    private String email;
    private String familyName;
    private String givenName;

    public User(String id, String username, String email, String familyName, String givenName) {
        this.setId(id);
        this.setUsername(username);
        this.setEmail(email);
        this.setFamilyName(familyName);
        this.setGivenName(givenName);
    }

    public String getUsername() {
        return username;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public static User buildUserFromToken(AccessToken token) {
        String familyName = token.getFamilyName();
        String givenName = token.getGivenName();
        User user = new User(
                null,
                token.getPreferredUsername(),
                token.getEmail(),
                familyName != null ? familyName : "",
                givenName != null ? givenName : ""
        );
        return user;
    }
}