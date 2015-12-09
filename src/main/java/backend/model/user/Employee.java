package backend.model.user;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue("EMPLOYEE")
public class Employee extends UserRole {

    @OneToOne
    private Address address;

    @Column(name = "emp_tshirtsize")
    private String tshirtSize;
    private String title;
    private String phonenumber;

    public Employee() {
        super();
    }

    public Employee(UserCore core) {
        super(core);
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getTshirtSize() {
        return tshirtSize;
    }

    public void setTshirtSize(String tshirtSize) {
        this.tshirtSize = tshirtSize;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    @Override
    public String getAuthority() {
        return "EMPLOYEE";
    }
}
