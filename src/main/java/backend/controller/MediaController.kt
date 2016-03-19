package backend.controller

import backend.controller.exceptions.UnauthorizedException
import backend.model.media.MediaService
import backend.model.media.MediaSize
import backend.model.media.MediaSizeService
import backend.model.media.MediaType
import backend.services.ConfigurationService
import backend.view.MediaSizeView
import com.auth0.jwt.JWTVerifier
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.*
import java.security.SignatureException
import javax.validation.Valid

@RestController
@RequestMapping("/media")
class MediaController {

    private val mediaSizeService: MediaSizeService
    private val mediaService: MediaService
    private val configurationService: ConfigurationService
    private val logger: Logger
    private var JWT_SECRET: String

    @Autowired
    constructor(mediaSizeService: MediaSizeService,
                mediaService: MediaService,
                configurationService: ConfigurationService) {

        this.mediaService = mediaService
        this.mediaSizeService = mediaSizeService
        this.configurationService = configurationService
        this.logger = Logger.getLogger(PostingController::class.java)
        this.JWT_SECRET = configurationService.getRequired("org.breakout.api.jwt_secret")
    }

    /**
     * POST /media/id/
     */
    @RequestMapping("/{id}/", method = arrayOf(RequestMethod.POST))
    @ResponseStatus(CREATED)
    fun createMediaSize(@PathVariable("id") id: Long,
                        @RequestHeader("X-UPLOAD-TOKEN") uploadToken: String,
                        @Valid @RequestBody body: MediaSizeView): MediaSizeView {

        try {
            if (!(JWTVerifier(JWT_SECRET, "audience").verify(uploadToken)["subject"] as String).equals(id.toString())) {
                throw UnauthorizedException("Invalid JWT token")
            }
        } catch (e: SignatureException) {
            throw UnauthorizedException(e.message ?: "Invalid JWT token")
        } catch (e: IllegalStateException) {
            throw UnauthorizedException(e.message ?: "Invalid JWT token")
        }

        val media = mediaService.getByID(id);

        //Delete Size, if it already exists
        var mediaSizeFound: MediaSize?
        if (body.width!! > body.height!!) {
            logger.info("findByWidthAndMediaId")
            mediaSizeFound = mediaSizeService.findByWidthAndMediaAndMediaType(body.width!!, media!!, MediaType.valueOf(body.type!!.toUpperCase()));
        } else {
            logger.info("findByHeightAndMediaId")
            mediaSizeFound = mediaSizeService.findByHeightAndMediaAndMediaType(body.height!!, media!!, MediaType.valueOf(body.type!!.toUpperCase()));
        }
        if (mediaSizeFound == null) {
            var mediaSize = mediaSizeService.createAndSaveMediaSize(media, body.url!!, body.width!!, body.height!!, body.length!!, body.size!!, body.type!!)
            return MediaSizeView(mediaSize)
        } else {
            mediaSizeFound.url = body.url!!
            mediaSizeFound.width = body.width
            mediaSizeFound.height = body.height
            mediaSizeFound.length = body.length
            mediaSizeFound.size = body.size
            mediaSizeFound.mediaType = MediaType.valueOf(body.type!!.toUpperCase())
            mediaSizeService.save(mediaSizeFound)
            return MediaSizeView(mediaSizeFound)
        }
    }
}
