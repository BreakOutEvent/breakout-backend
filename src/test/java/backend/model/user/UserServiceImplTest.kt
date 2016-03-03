package backend.model.user

import backend.services.ConfigurationService
import backend.services.MailService
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*

class UserServiceImplTest {

    private lateinit var mailService: MailService
    private lateinit var userServiceImpl: UserService
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        mailService = mock(MailService::class.java)
        userRepository = mock(UserRepository::class.java)
        val configurationService = mock(ConfigurationService::class.java)
        userServiceImpl = UserServiceImpl(userRepository, mailService, configurationService)

        `when`(configurationService.getRequired("org.breakout.api.host")).thenReturn("localhost")
        `when`(configurationService.getRequired("org.breakout.api.port")).thenReturn("8083")
    }

    @Test
    fun testGetUserById() {

    }

    @Test
    fun testGetUserByEmail() {

    }

    @Test
    fun testGetAllUsers() {

    }

    @Test
    fun testExists() {

    }

    @Test
    fun testExists1() {

    }

    @Test
    fun testCreate() {
        val expectedUser = User.create("mail@mail.de", "password")
        `when`(userRepository.save(any<UserCore>())).thenReturn(expectedUser.core)

        userServiceImpl.create("mail@mail.de", "password")

        verify(mailService).send(anyObject())
        verify(userRepository).save(anyObject<UserCore>())
    }

    // Workaround for Mockito with Kotlin
    // See: http://stackoverflow.com/a/30308199
    private fun <T> anyObject(): T {
        return Mockito.anyObject<T>()
    }

    @Test
    fun testCreateFromUserBody() {

    }

    @Test
    fun testSave() {

    }
}
