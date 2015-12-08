package backend.model.user;

import java.util.HashMap;
import java.util.Map;

public class UserCore implements User {

    private Map<Class, UserRole> userRoles = new HashMap<>();

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
