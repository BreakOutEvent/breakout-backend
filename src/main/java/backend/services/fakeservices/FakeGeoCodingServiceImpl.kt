package backend.services.fakeservices

import backend.model.misc.Coord
import backend.services.ConfigurationService
import backend.services.GeoCodingService
import backend.services.GeoCodingServiceImpl
import backend.util.Profiles.DEVELOPMENT
import backend.util.Profiles.STAGING
import backend.util.Profiles.TEST
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(DEVELOPMENT, TEST , STAGING)
open class FakeGeoCodingServiceImpl @Autowired constructor(configurationService: ConfigurationService) : GeoCodingService by GeoCodingServiceImpl(configurationService) {

    val logger: Logger = LoggerFactory.getLogger(FakeGeoCodingServiceImpl::class.java)

    override fun getGeoCoded(coord: Coord): Map<String, String> {
        logger.info("Faked Geocoding for (${coord.latitude}, ${coord.longitude})")

        return mapOf(
                "ADMINISTRATIVE_AREA_LEVEL_1" to "Bayern",
                "POLITICAL" to "Germany",
                "PREMISE" to "Hauptgebäude",
                "ADMINISTRATIVE_AREA_LEVEL_2" to "Oberbayern",
                "LOCALITY" to "München",
                "SUBLOCALITY_LEVEL_1" to "Maxvorstadt",
                "COUNTRY" to "Germany",
                "SUBLOCALITY" to "Maxvorstadt"
        )
    }
}
