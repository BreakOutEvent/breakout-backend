package backend.model.user;

import backend.TestBackendConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestBackendConfiguration.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class TestUser {

    @Autowired
    private UserRepository userRepository;

    @Before
    public void setUp() {
        Iterable<UserCore> users = userRepository.findAll();
        userRepository.deleteAll();
    }

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
     *
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
     *
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

    @Test
    public void createAndSaveUser() throws Exception {

        // Create user with role and save it
        User user = new UserCore().addRole(Employee.class);
        user.setFirstname("Florian");
        user.setLastname("Schmidt");
        user.setEmail("florian.schmidt.1995@icloud.com");
        user.setIsBlocked(false);
        user.setPassword("Lorem ipsum");
        user.setGender("Male");
        userRepository.save(user.getCore());

        // Check if saved user can be found again
        User user1 = userRepository.findByEmail("florian.schmidt.1995@icloud.com");
        assertEquals(user.getCore().getId(), user1.getCore().getId());
        assertTrue(user.hasRole(Employee.class));

        // Add and remove roles from user and save
        user1.addRole(Participant.class);
        user1.removeRole(Employee.class);
        userRepository.save(user1.getCore());

        // Check if found user has correct roles
        User user2 = userRepository.findByEmail("florian.schmidt.1995@icloud.com");
        assertEquals(user.getCore().getId(), user2.getCore().getId());
        assertTrue(user2.hasRole(Participant.class));
        assertFalse(user2.hasRole(Employee.class));

    }
}
