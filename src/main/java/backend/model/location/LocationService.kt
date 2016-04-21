package backend.model.location

interface LocationService {
    fun save(location: Location): Location
    fun findAll(): Iterable<Location>
}
