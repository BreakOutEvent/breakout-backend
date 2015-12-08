package backend.model.user;

import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(JUnit4ClassRunner.class)
public class UserTest {

    @Test
    public void createUserWithoutRole() {
        User user = new UserCore();
        assertFalse(user.hasRole("backend.model.user.Employee"));
        assertNull(user.getRole("backend.model.user.Employee"));
        assertNull(user.removeRole("backend.model.user.Employee"));
    }

    @Test
    public void createUserWithRoleEmployee() throws Exception {
        User user = new UserCore();
        user.addRole("backend.model.user.Employee");
        assertTrue(user.hasRole("backend.model.user.Employee"));
        Employee emp = (Employee) user.getRole("backend.model.user.Employee");
        assertNotNull(emp);
        assertEquals(emp, user.removeRole("backend.model.user.Employee"));
        assertNull(user.removeRole("backend.model.user.Employee"));
    }
}
