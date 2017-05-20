package backend.services

import backend.model.misc.Coord
import backend.services.mail.MailServiceImpl
import backend.util.Profiles.PRODUCTION
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
@Profile(PRODUCTION)
class GeoCodingServiceImpl @Autowired constructor(configurationService: ConfigurationService) : GeoCodingService {

    private val apiKey: String = configurationService.getRequired("org.breakout.google.apikey")

    private val logger = LoggerFactory.getLogger(MailServiceImpl::class.java)

    private fun toGoogleLatLng(coord: Coord): LatLng {
        return LatLng(coord.latitude, coord.longitude)
    }

    override fun getGeoCoded(coord: Coord): Map<String, String> {

        val context: GeoApiContext = GeoApiContext().setApiKey(this.apiKey)
        val results: Array<GeocodingResult> = GeocodingApi.reverseGeocode(context, this.toGoogleLatLng(coord)).await()

        val geoCodeMap = mutableMapOf<String, String>()

        if (results.isNotEmpty()) {
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
