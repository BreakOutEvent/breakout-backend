package backend.services

import backend.model.misc.Coord

interface GeoCodingService {
    fun getGeoCoded(coord: Coord): Map<String, String>
}
