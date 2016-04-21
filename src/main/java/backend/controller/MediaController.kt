package backend.controller

import backend.model.media.MediaService
import backend.model.media.MediaSize
import backend.model.media.MediaSizeService
import backend.model.media.MediaType
import backend.services.ConfigurationService
import backend.util.verifyJwtClaim
import backend.view.MediaSizeView
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.POST
import javax.validation.Valid

/**
 * Is only called by the media recoding microservice
 */

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
     * Adds single MediaSize to Media
     */
    @RequestMapping("/{id}/", method = arrayOf(POST))
    @ResponseStatus(CREATED)
    fun createMediaSize(@PathVariable("id") id: Long,
                        @RequestHeader("X-UPLOAD-TOKEN") uploadToken: String,
                        @Valid @RequestBody body: MediaSizeView): MediaSizeView {

        verifyJwtClaim(JWT_SECRET, uploadToken, id.toString())
        val media = mediaService.getByID(id);

        //TODO: move to mediaService
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
