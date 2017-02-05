package backend.services

import backend.model.misc.Coord
import backend.util.Profiles.PRODUCTION
import backend.util.Profiles.STAGING
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.AddressComponent
import com.google.maps.model.GeocodingResult
import com.google.maps.model.LatLng
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(PRODUCTION, STAGING)
class GeoCodingServiceImpl : GeoCodingService {

    private val apiKey: String

    private val logger = LoggerFactory.getLogger(MailServiceImpl::class.java)

    @Autowired
    constructor(configurationService: ConfigurationService) {
        this.apiKey = configurationService.getRequired("org.breakout.google.apikey")
    }

    private fun toGoogleLatLng(coord: Coord): LatLng {
        return LatLng(coord.latitude, coord.longitude)
    }

    override fun getGeoCoded(coord: Coord): Map<String, String> {

        val context: GeoApiContext = GeoApiContext().setApiKey(this.apiKey)
        val results: Array<GeocodingResult> = GeocodingApi.reverseGeocode(context, this.toGoogleLatLng(coord)).await()

        val geoCodeMap = mutableMapOf<String, String>()

        if (results.size > 0) {
            results[0].addressComponents.forEach { comp: AddressComponent ->
                comp.types.forEach {
                    geoCodeMap[it.name] = comp.longName
                }
            }

            logger.info("Got GeoCoding for (${coord.latitude}, ${coord.longitude}) - ${geoCodeMap["ADMINISTRATIVE_AREA_LEVEL_1"]}, ${geoCodeMap["COUNTRY"]}")
        }

        return geoCodeMap
    }
}
