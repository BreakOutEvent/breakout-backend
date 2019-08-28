package backend.controller

import backend.model.event.EventService
import backend.model.misc.Coord
import backend.model.posting.PostingService
import backend.model.user.UserService
import backend.util.Profiles
import org.javamoney.moneta.Money
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.*

@Profile(Profiles.DEVELOPMENT)
@RestController
@RequestMapping("/createdummydata")
class DummyDataController(val userService: UserService,
                          val eventService: EventService,
                          val postingService: PostingService) {

    @PostMapping("/")
    fun createDummyData() {
        createUsers(100)
        createEvent(2)
        createPosting(400, 100)
    }

    fun createUsers(count: Int): Iterable<Long> {
        return (0..count).map {
            val user = userService.create("email$it@mail.com", "password")
            val token = user.createActivationToken()
            user.activate(token)
            return@map user.account.id!!
        }
    }

    fun createEvent(count: Int): Iterable<Long> {
        return (0..count).map {
            eventService.createEvent("Eventtitle$it", LocalDateTime.now(), "City$count", Coord(0.0, 0.0), 36).id!!
        }
    }

    fun createPosting(count: Int, max: Int): Iterable<Long> {
        return (0..count).map {
            val random = Random()
            val id = random.nextInt(max - 1).toLong() + 1
            val user = userService.getUserById(id)?.account
            if (user != null) {
                postingService.savePostingWithLocationAndMedia(
                        text = "text$it",
                        postingLocation = Coord(0.0, 0.0),
                        media = null,
                        user = user,
                        date = LocalDateTime.now()).id!!
            } else 1
        }
    }
}
