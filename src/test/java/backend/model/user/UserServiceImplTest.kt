package backend.model.user

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
        userServiceImpl = UserServiceImpl(userRepository, mailService)
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

        val result = userServiceImpl.create("mail@mail.de", "password")

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
