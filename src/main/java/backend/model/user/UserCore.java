package backend.model.user;

import java.util.HashMap;
import java.util.Map;

public class UserCore implements User {

    private Map<Class, UserRole> userRoles = new HashMap<>();

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

    private String firstname;
    private String lastname;
    private String email;
    private boolean isBlocked = false;
    private String password;
    private String gender;

    @Override
    public UserRole addRole(Class clazz) throws Exception {

        UserRole role;

        if(userRoles.containsKey(clazz)) {
            return userRoles.get(clazz);
        } else {
            role = UserRole.createFor(clazz, this);
            userRoles.put(clazz, role);
            return role;
        }
    }

    @Override
    public UserRole getRole(Class clazz) {
        return userRoles.get(clazz);
    }

    @Override
    public boolean hasRole(Class clazz) {
        return userRoles.containsKey(clazz);
    }

    @Override
    public UserRole removeRole(Class clazz) {
        if(userRoles.containsKey(clazz)) {
            return userRoles.remove(clazz);
        } else {
            return null;
        }
    }
}
