package backend.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
public class UserCore implements User {

    @Id
    @GeneratedValue
    private Integer id;

    @NotEmpty
    private String firstname;

    @NotEmpty
    private String lastname;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    // TODO: Not sure how to annotate this one
    private boolean isBlocked = false;

    @NotEmpty
    private String password;

    @NotEmpty
    private String gender;

    /*
     * cascade all operations to children
     * orphanRemoval = true allows removing a role from the database
     * if it gets removed from Map userRoles and the core is saved!
     * See: http://stackoverflow.com/a/2011546
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Map<Class<? extends UserRole>, UserRole> userRoles = new HashMap<>();

    @Override
    public String getFirstname() {
        return firstname;
    }

    @Override
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    @Override
    public String getLastname() {
        return lastname;
    }

    @Override
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    @Override
    public void setIsBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getGender() {
        return gender;
    }

    @Override
    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Map<Class<? extends UserRole>, UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Map<Class<? extends UserRole>, UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    @Override
    public UserRole addRole(Class<? extends UserRole> clazz) throws Exception {

        UserRole role;

        if (userRoles.containsKey(clazz)) {
            return userRoles.get(clazz);
        } else {
            role = UserRole.createFor(clazz, this);
            userRoles.put(clazz, role);
            return role;
        }
    }

    @Override
    public UserRole getRole(Class<? extends UserRole> clazz) {
        return userRoles.get(clazz);
    }

    @Override
    public boolean hasRole(Class<? extends UserRole> clazz) {
        return userRoles.containsKey(clazz);
    }

    @Override
    public UserRole removeRole(Class<? extends UserRole> clazz) {
        if (userRoles.containsKey(clazz)) {
            return userRoles.remove(clazz);
        } else {
            return null;
        }
    }

    @Override
    @JsonIgnore
    public UserCore getCore() {
        return this;
    }
}
