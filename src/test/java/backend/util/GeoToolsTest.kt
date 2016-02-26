package backend.util

import backend.model.misc.Coord
import backend.utils.coordToPoint
import backend.utils.coordsListToPairList
import backend.utils.distanceCoordsKM
import backend.utils.distanceCoordsListKM
import com.grum.geocalc.DegreeCoordinate
import com.grum.geocalc.Point
import org.junit.Test
import kotlin.test.assertEquals

class GeoToolsTest {

    @Test
    fun testCoordsListToPairList() {
        val coordList: List<Coord> = listOf(Coord(1.0, 1.0), Coord(2.0, 2.0), Coord(3.0, 3.0), Coord(4.0, 4.0))

        assertEquals(listOf(Pair(Coord(1.0, 1.0), Coord(2.0, 2.0)), Pair(Coord(2.0, 2.0), Coord(3.0, 3.0)), Pair(Coord(3.0, 3.0), Coord(4.0, 4.0))), coordsListToPairList(coordList))
    }

    @Test
    fun testDistanceCoordsKM() {
        val coord1: Coord = Coord(1.0, 1.0)
        val coord2: Coord = Coord(10.0, 10.0)

        assertEquals(1408, distanceCoordsKM(coord1, coord2).toInt())
    }

    @Test
    fun testDistanceCoordsListKM() {
        val coordList: List<Coord> = listOf(Coord(1.0, 1.0), Coord(2.0, 2.0), Coord(3.0, 3.0), Coord(4.0, 4.0))

        assertEquals(470, distanceCoordsListKM(coordList).toInt())
    }

    @Test
    fun testDistanceCoordsListKMOneCoord() {
        val coordList: List<Coord> = listOf(Coord(1.0, 1.0))

        assertEquals(0, distanceCoordsListKM(coordList).toInt())
    }

    @Test
    fun testDistanceCoordsListKMNoCoords() {
        val coordList: List<Coord> = listOf()

        assertEquals(0, distanceCoordsListKM(coordList).toInt())
    }

    @Test
    fun testCoordToPoint() {
        val coord: Coord = Coord(1.0, 1.0)

        assertEquals(Point(DegreeCoordinate(1.0), DegreeCoordinate(1.0)), coordToPoint(coord))
    }
}