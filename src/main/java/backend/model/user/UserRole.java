package backend.model.user;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

@Entity
@Inheritance
@DiscriminatorColumn(name = "ROLE_NAME")
public abstract class UserRole implements User, GrantedAuthority {

    @Id
    @GeneratedValue()
    private Integer roleID;

    @ManyToOne
    private UserCore core;

    public UserRole() {

    }

    public UserRole(UserCore core) {
        this.core = core;
    }

    public static UserRole createFor(Class<? extends UserRole> clazz, UserCore core) throws Exception {
        return clazz.asSubclass(clazz).getConstructor(UserCore.class).newInstance(core);
    }

    @Override
    public UserCore getCore() {
        return this.core;
    }

    public void setCore(UserCore core) {
        this.core = core;
    }

    public Integer getRoleID() {
        return roleID;
    }

    public void setRoleID(Integer id) {
        this.roleID = id;
    }

    @Override
    public String getFirstname() {
        return this.core.getFirstname();
    }

    @Override
    public void setFirstname(String firstname) {
        this.core.setFirstname(firstname);
    }

    @Override
    public String getLastname() {
        return this.core.getLastname();
    }

    @Override
    public void setLastname(String lastname) {
        this.core.setLastname(lastname);
    }

    @Override
    public String getEmail() {
        return this.core.getEmail();
    }

    @Override
    public void setEmail(String email) {
        this.core.setEmail(email);
    }

    @Override
    public boolean isBlocked() {
        return this.core.isBlocked();
    }

    @Override
    public void setIsBlocked(boolean isBlocked) {
        this.core.setIsBlocked(isBlocked);
    }

    @Override
    public String getPassword() {
        return this.core.getPassword();
    }

    @Override
    public void setPassword(String password) {
        this.core.setPassword(password);
    }

    @Override
    public String getGender() {
        return this.core.getGender();
    }

    @Override
    public void setGender(String gender) {
        this.core.setGender(gender);
    }

    @Override
    public UserRole addRole(Class<? extends UserRole> clazz) throws Exception {
        return this.core.addRole(clazz);
    }

    @Override
    public UserRole getRole(Class<? extends UserRole> clazz) {
        return this.core.getRole(clazz);
    }

    @Override
    public boolean hasRole(Class<? extends UserRole> clazz) {
        return this.core.hasRole(clazz);
    }

    @Override
    public UserRole removeRole(Class<? extends UserRole> clazz) {
        return this.core.removeRole(clazz);
    }
}