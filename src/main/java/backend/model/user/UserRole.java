package backend.model.user;

public abstract class UserRole implements User {

    private UserCore core;

    public UserRole(UserCore core) {
        this.core = core;
    }

    @Override
    public UserRole addRole(Class clazz) throws Exception{
        return this.core.addRole(clazz);
    }

    @Override
    public UserRole getRole(Class clazz) {
        return this.core.getRole(clazz);
    }

    @Override
    public boolean hasRole(Class clazz) {
        return this.core.hasRole(clazz);
    }

    @Override
    public UserRole removeRole(Class clazz) {
        return this.core.removeRole(clazz);
    }

    public static UserRole createFor(Class clazz, UserCore core) throws Exception {
        if(UserRole.class.isAssignableFrom(clazz)) {
            return (UserRole) clazz.asSubclass(clazz).getConstructor(UserCore.class).newInstance(core);
        } else {
            throw new Exception(clazz + " must extend UserRole");
        }
    }
}