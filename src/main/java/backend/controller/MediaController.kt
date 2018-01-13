package backend.controller

import backend.controller.exceptions.NotFoundException
import backend.model.media.MediaService
import backend.services.ConfigurationService
import backend.util.CacheNames.POSTINGS
import backend.util.CacheNames.TEAMS
import com.cloudinary.Cloudinary
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Caching
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Is only called by the media recoding microservice
 */

@RestController
@RequestMapping("/media")
class MediaController(private val mediaService: MediaService,
                      private val configurationService: ConfigurationService) {

    private val CLOUDINARY_API_SECRET = configurationService.getRequired("org.breakout.cloudinary.api_secret");
    private val CLOUDINARY_API_KEY = configurationService.getRequired("org.breakout.cloudinary.api_key");

    /**
     * DELETE /media/{id}/
     * Allows Admin to delete all mediaSizes for media
     */
    @Caching(evict = arrayOf(CacheEvict(POSTINGS, allEntries = true), CacheEvict(TEAMS, allEntries = true)))
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping("/{id}/", method = arrayOf(RequestMethod.DELETE))
    fun adminDeletePosting(@PathVariable("id") id: Long): Map<String, String> {
        val media = mediaService.getByID(id) ?: throw NotFoundException("media with id $id does not exist")
        mediaService.delete(media)
        return mapOf("message" to "success")
    }

    @RequestMapping("/signCloudinaryParams/")
    fun getCloudinaryUploadHash(@RequestBody uploadOptions: MutableMap<String, Any>): Map<String, Any> {
        val cloudinary = Cloudinary();
        uploadOptions["timestamp"] = System.currentTimeMillis()
        uploadOptions["signature"] = cloudinary.apiSignRequest(uploadOptions, CLOUDINARY_API_SECRET)
        return uploadOptions
    }

}
