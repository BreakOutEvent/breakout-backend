package backend.model.user;

public interface User {

    String getFirstname();
    void setFirstname(String firstname);

    String getLastname();
    void setLastname(String lastname);

    String getEmail();
    void setEmail(String email);

    boolean isBlocked();
    void setIsBlocked(boolean isBlocked);

    String getPassword();
    void setPassword(String password);

    String getGender();
    void setGender(String gender);

    UserRole addRole(Class<? extends UserRole> clazz) throws Exception;
    UserRole getRole(Class<? extends UserRole> clazz);
    boolean hasRole(Class<? extends UserRole> clazz);
    UserRole removeRole(Class<? extends UserRole> clazz);

    UserCore getCore();
}
