package backend.controller

import backend.controller.exceptions.NotFoundException
import backend.model.media.MediaService
import backend.model.media.MediaSizeService
import backend.services.ConfigurationService
import backend.util.verifyJwtClaim
import backend.view.MediaSizeView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.POST
import javax.validation.Valid

/**
 * Is only called by the media recoding microservice
 */

@RestController
@RequestMapping("/media")
class MediaController(private val mediaSizeService: MediaSizeService,
                           private val mediaService: MediaService,
                           private val configurationService: ConfigurationService) {

    private val JWT_SECRET: String

    /**
     * POST /media/{id}/
     * Adds single MediaSize to Media
     */
    @PostMapping("/{id}/")
    @ResponseStatus(CREATED)
    fun createMediaSize(@PathVariable("id") id: Long,
                             @RequestHeader("X-UPLOAD-TOKEN") uploadToken: String,
                             @Valid @RequestBody body: MediaSizeView): MediaSizeView {

        verifyJwtClaim(JWT_SECRET, uploadToken, id.toString())
        val media = mediaService.getByID(id) ?: throw NotFoundException("No media with id $id")

        val mediaSize = mediaSizeService.createOrUpdate(media.id!!, body.url!!, body.width!!, body.height!!, body.length!!, body.size!!, body.type!!)
        return MediaSizeView(mediaSize)
    }

    /**
     * DELETE /media/{id}/
     * Allows Admin to delete all mediaSizes for media
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/{id}/", method = arrayOf(RequestMethod.DELETE))
    fun adminDeletePosting(@PathVariable("id") id: Long): Map<String, String> {
        val media = mediaService.getByID(id) ?: throw NotFoundException("media with id $id does not exist")
        mediaService.deleteSizes(media)
        return mapOf("message" to "success")
    }

    init {
        this.JWT_SECRET = configurationService.getRequired("org.breakout.api.jwt_secret")
    }
}
