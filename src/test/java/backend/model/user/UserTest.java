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

    /**
     * Create user and add some details
     * Check if role delegates calls to core
     * @throws Exception
     */
    @Test
    public void createUserWithDetails() throws Exception {
        User user = new UserCore();
        user.setFirstname("Florian");
        user.setLastname("Schmidt");
        user.setEmail("florian.schmidt.1994@icloud.com");
        user.setIsBlocked(false);
        user.setPassword("Lorem ipsum");
        user.setGender("Male");

        Employee emp = (Employee) user.addRole(Employee.class);

        // Check user
        assertEquals("Florian", user.getFirstname());
        assertEquals("Schmidt", user.getLastname());
        assertEquals("florian.schmidt.1994@icloud.com", user.getEmail());
        assertEquals(false, user.isBlocked());
        assertEquals("Lorem ipsum", user.getPassword());
        assertEquals("Male", user.getGender());

        // Check it's roles getter
        assertEquals("Florian", emp.getFirstname());
        assertEquals("Schmidt", emp.getLastname());
        assertEquals("florian.schmidt.1994@icloud.com", emp.getEmail());
        assertEquals(false, emp.isBlocked());
        assertEquals("Lorem ipsum", emp.getPassword());
        assertEquals("Male", emp.getGender());

        // Check it's roles setter
        emp.setFirstname("_Florian");
        emp.setLastname("_Schmidt");
        emp.setEmail("_florian.schmidt.1994@icloud.com");
        emp.setIsBlocked(true);
        emp.setPassword("_Lorem ipsum");
        emp.setGender("_Male");

        assertEquals("_Florian", user.getFirstname());
        assertEquals("_Schmidt", user.getLastname());
        assertEquals("_florian.schmidt.1994@icloud.com", user.getEmail());
        assertEquals(true, user.isBlocked());
        assertEquals("_Lorem ipsum", user.getPassword());
        assertEquals("_Male", user.getGender());
    }
}
