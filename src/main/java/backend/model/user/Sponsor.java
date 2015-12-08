package backend.model.user;

public class Sponsor extends UserRole {

    public Sponsor(UserCore core) {
        super(core);
    }

    private String company;
    private String logo;
    private String url;
    private String address;
    private String isHidden;

    public Sponsor(UserCore core, String company, String logo, String url, String address, String isHidden) {
        super(core);
        this.company = company;
        this.logo = logo;
        this.url = url;
        this.address = address;
        this.isHidden = isHidden;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(String isHidden) {
        this.isHidden = isHidden;
    }
}
