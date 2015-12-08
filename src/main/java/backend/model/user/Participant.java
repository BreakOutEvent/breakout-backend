package backend.model.user;

public class Participant extends UserRole {

    public Participant(UserCore core) {
        super(core);
    }

    private String emergencynumber;
    private String tshirtsize;
    private String hometown;
    private String phonenumber;

    public String getEmergencynumber() {
        return emergencynumber;
    }

    public void setEmergencynumber(String emergencynumber) {
        this.emergencynumber = emergencynumber;
    }

    public String getTshirtsize() {
        return tshirtsize;
    }

    public void setTshirtsize(String tshirtsize) {
        this.tshirtsize = tshirtsize;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
}
