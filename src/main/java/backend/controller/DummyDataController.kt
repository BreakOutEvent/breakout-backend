package backend.controller

import backend.model.event.EventService
import backend.model.misc.Coord
import backend.model.posting.PostingService
import backend.model.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.POST
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/createdummydata")
class DummyDataController {

    val userService: UserService
    val eventService: EventService
    val postingService: PostingService

    @Autowired
    constructor(userService: UserService, eventService: EventService, postingService: PostingService) {
        this.userService = userService
        this.eventService = eventService
        this.postingService = postingService
    }

    @RequestMapping("/", method = arrayOf(POST))
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
            return@map user.core.id!!
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
            val user = userService.getUserById(id)?.core
            if (user != null) {
                postingService.createPosting(
                        text = "text$it",
                        postingLocation = Coord(0.0, 0.0),
                        media = null,
                        user = user,
                        distance = 0.0).id!!
            } else 1
        }
    }
}
