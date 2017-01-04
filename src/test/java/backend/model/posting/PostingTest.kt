package backend.model.posting

import backend.controller.exceptions.ConflictException
import backend.model.location.Location
import backend.model.user.UserAccount
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(PowerMockRunner::class)
@PrepareForTest(Location::class, UserAccount::class)
class PostingTest {

    private lateinit var now: LocalDateTime
    private lateinit var later: LocalDateTime
    private lateinit var someLocation: Location
    private lateinit var creator: UserAccount
    private lateinit var posting: Posting


    @Before
    fun before() {
        now = LocalDateTime.now()
        later = now.plusMinutes(10)
        someLocation = mock(Location::class.java)
        creator = mock(UserAccount::class.java)
        posting = Posting(text = "Test",
                date = now,
                location = someLocation,
                user = creator,
                media = mutableListOf())
    }

    @Test
    fun like() {
        posting.like(later, creator)
        assertEquals(posting.likes.count(), 1)
    }

    @Test
    fun multipleLikes() {

        val other = mock(UserAccount::class.java)

        PowerMockito.`when`(creator.id).thenReturn(1)
        PowerMockito.`when`(other.id).thenReturn(2)

        posting.like(later, creator)
        posting.like(later, other)

        assertEquals(posting.likes.count(), 2)
    }

    fun cantLikeTwice() {

        PowerMockito.`when`(creator.id).thenReturn(1)

        posting.like(later, creator)

        assertFailsWith<ConflictException> {
            posting.like(later, creator)
        }
    }

    @Test
    fun unlike() {
        posting.like(later, creator)
        posting.unlike(creator)
    }

    @Test
    fun cantUnlikeNotLiked() {
        assertFailsWith<ConflictException> {
            posting.unlike(creator)
        }
    }

    @Test
    @Ignore("Posting::hasLikesBy needs to be refactored first")
    fun hasLikesBy() {

    }

}
