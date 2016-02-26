package backend.services

import backend.model.misc.Coords
import backend.utils.coordsListToPairList
import backend.utils.coordsToPoint
import backend.utils.distanceCoordsKM
import backend.utils.distanceCoordsListKM
import com.grum.geocalc.DegreeCoordinate
import com.grum.geocalc.Point
import org.junit.Test
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import kotlin.test.assertEquals

@Service
@Profile("test")
class GeoToolsTest {

    @Test
    fun testCoordsListToPairList() {
        val coordsList: List<Coords> = listOf(Coords(1.0, 1.0), Coords(2.0, 2.0), Coords(3.0, 3.0), Coords(4.0, 4.0))

        assertEquals(listOf(Pair(Coords(1.0, 1.0), Coords(2.0, 2.0)), Pair(Coords(2.0, 2.0), Coords(3.0, 3.0)), Pair(Coords(3.0, 3.0), Coords(4.0, 4.0))), coordsListToPairList(coordsList))
    }

    @Test
    fun testDistanceCoordsKM() {
        val coord1: Coords = Coords(1.0, 1.0)
        val coord2: Coords = Coords(10.0, 10.0)

        assertEquals(1408, distanceCoordsKM(coord1, coord2).toInt())
    }

    @Test
    fun testDistanceCoordsListKM() {
        val coordsList: List<Coords> = listOf(Coords(1.0, 1.0), Coords(2.0, 2.0), Coords(3.0, 3.0), Coords(4.0, 4.0))

        assertEquals(470, distanceCoordsListKM(coordsList).toInt())
    }

    @Test
    fun testCoordsToPoint() {
        val coord: Coords = Coords(1.0, 1.0)

        assertEquals(Point(DegreeCoordinate(1.0), DegreeCoordinate(1.0)), coordsToPoint(coord))
    }
}
