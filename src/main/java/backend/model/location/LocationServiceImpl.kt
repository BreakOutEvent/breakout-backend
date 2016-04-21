package backend.model.location

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LocationServiceImpl : LocationService {

    private val locationRepository: LocationRepository

    @Autowired
    constructor(locationRepository: LocationRepository) {
        this.locationRepository = locationRepository
    }

    override fun findAll(): Iterable<Location> {
        return locationRepository.findAll()
    }

    override fun save(location: Location): Location {
        return locationRepository.save(location)
    }

}

