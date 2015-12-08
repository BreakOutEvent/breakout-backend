package backend.model.user;

public interface User {
    UserRole addRole(Class clazz) throws Exception;
    UserRole getRole(Class clazz);
    boolean hasRole(Class clazz);
    UserRole removeRole(Class clazz);
}
