package backend.controller.RequestBodies;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class Post {

    @NotNull
    private Date created;

    @NotNull
    private Date sent;

    @NotNull
    private String text;

    @NotNull
    @Valid
    private Location location;

    @NotNull
    private String challengeId;

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getChallengeId() {
        return challengeId;
    }

    @JsonProperty("challenge_id")
    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }
}
