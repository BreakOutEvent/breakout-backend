package backend.model.user;

public interface User {

    Integer getId();
    void setId(Integer id);

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

    UserRole addRole(Class clazz) throws Exception;
    UserRole getRole(Class clazz);
    boolean hasRole(Class clazz);
    UserRole removeRole(Class clazz);

    UserCore getCore();
}
