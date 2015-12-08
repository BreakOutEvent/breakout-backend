package backend.model.user;

public abstract class UserRole implements User {

    private UserCore core;

    public UserRole(UserCore core) {
        this.core = core;
    }

    @Override
    public UserRole addRole(String role) throws Exception{
        return this.core.addRole(role);
    }

    @Override
    public UserRole getRole(String role) {
        return this.core.getRole(role);
    }

    @Override
    public boolean hasRole(String role) {
        return this.core.hasRole(role);
    }

    @Override
    public UserRole removeRole(String role) {
        return this.core.removeRole(role);
    }

    public static UserRole createFor(String role, UserCore core) throws Exception {
        try {
            Class<UserRole> c = (Class<UserRole>) Class.forName(role);
            return c.asSubclass(c).getConstructor(UserCore.class).newInstance(core);
        } catch (Exception e) {
            throw new Exception("Can't create object for role "+role, e);
        }
    }
}