package org.jembi.jempi.shared.models;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
//@JsonIgnoreProperties(ignoreUnknown = true)

public class MatchForReview {

    private int id;
    private String given_name;
    private String family_name;
    private String reason;
    private int match;
    private String state;
    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getMatch() {
        return match;
    }

    public void setMatch(int match) {
        this.match = match;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGiven_name() {
        return given_name;
    }

    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }

    public String getFamily_name() {
        return family_name;
    }

    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    public MatchForReview(int id, String given_name, String family_name, String reason, int match, String state, String date) {
        this.id = id;
        this.given_name = given_name;
        this.family_name = family_name;
        this.reason = reason;
        this.match = match;
        this.date = date;
        this.state = state;
    }




    public MatchForReview() {
    }
}
