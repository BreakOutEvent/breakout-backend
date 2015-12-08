package backend.model.user;

import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(JUnit4ClassRunner.class)
public class UserTest {

    /**
     * Create new user without any roles
     */
    @Test
    public void createUserWithoutRole() {
        User user = new UserCore();
        assertFalse(user.hasRole(Employee.class));
        assertNull(user.getRole(Employee.class));
        assertNull(user.removeRole(Employee.class));
    }

    /**
     * Create new user with role employee
     * @throws Exception
     */
    @Test
    public void createUserWithRoleEmployee() throws Exception {

        User user = new UserCore();
        user.addRole(Employee.class);

        assertTrue(user.hasRole(Employee.class));

        Employee emp = (Employee) user.getRole(Employee.class);

        assertNotNull(emp);
        assertEquals(emp, user.removeRole(Employee.class));
        assertNull(user.removeRole(Employee.class));
    }

    /**
     * Fail to create user with invalid role
     */
    @Test
    public void failToCreateUser() {

        User user = new UserCore();
        try {
            user.addRole(UserCore.class);
            fail();
        } catch (Exception e) {
            assertEquals("class backend.model.user.UserCore must extend UserRole", e.getMessage());
        }

        assertFalse(user.hasRole(UserCore.class));
        assertNull(user.getRole(Employee.class));
        assertNull(user.removeRole(Employee.class));
    }
}
