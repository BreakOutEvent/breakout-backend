package backend.model.user;

import java.util.HashMap;
import java.util.Map;

public class UserCore implements User {

    private Map<String, UserRole> userRoles = new HashMap<>();

    @Override
    public UserRole addRole(String spec) throws Exception {

        UserRole role;

        if(userRoles.containsKey(spec)) {
            return userRoles.get(spec);
        } else {
            role = UserRole.createFor(spec, this);
            userRoles.put(spec, role);
            return role;
        }
    }

    @Override
    public UserRole getRole(String spec) {
        return userRoles.get(spec);
    }

    @Override
    public boolean hasRole(String spec) {
        return userRoles.containsKey(spec);
    }

    @Override
    public UserRole removeRole(String spec) {
        if(userRoles.containsKey(spec)) {
            return userRoles.remove(spec);
        } else {
            return null;
        }
    }
}
