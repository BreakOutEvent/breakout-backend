package backend.model.user;

public interface User {
    UserRole addRole(String role) throws Exception;
    UserRole getRole(String role);
    boolean hasRole(String role);
    UserRole removeRole(String role);
}
