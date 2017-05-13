package backend.util

import backend.model.location.Location
import backend.model.misc.Coord
import com.grum.geocalc.DegreeCoordinate
import com.grum.geocalc.EarthCalc
import com.grum.geocalc.Point
import java.time.temporal.ChronoUnit


/**
 * Used Library Geocalc:
 * Copyright (c) 2015, Grumlimited Ltd (Romain Gallet)
 *
 * https://github.com/grumlimited/geocalc
 *
 * Full License Text: https://github.com/grumlimited/geocalc/blob/master/LICENSE.txt
 */

fun speedToLocation(toLocation: Location, fromLocation: Location): Double? {
    val millisDifference = toLocation.date.until(fromLocation.date, ChronoUnit.MILLIS)
    val distanceKm = distanceCoordsKM(fromLocation.coord, toLocation.coord)
    val speed = calculateSpeed(distanceKm, millisDifference)
    return speed
}

fun calculateSpeed(distanceKm: Double, millisDifference: Long): Double? {
    if (distanceKm > 0 && millisDifference > 0) {
        val speed = distanceKm / (millisDifference / (3600.0 * 1000.0))
        return speed
    } else {
        return null
    }
}

fun distanceCoordsKM(from: Coord, to: Coord): Double {
    val fromPoint: Point = coordToPoint(from)
    val toPoint: Point = coordToPoint(to)

    return EarthCalc.getVincentyDistance(fromPoint, toPoint) / 1000
}

fun distanceCoordsListKMfromStart(startingPoint: Coord, list: List<Coord>): Double {
    val coordsList = arrayListOf(startingPoint)
    coordsList.addAll(list)
    return distanceCoordsListKM(coordsList)
}

fun distanceCoordsListKM(list: List<Coord>): Double = coordsListToPairList(list).fold(0.0) { total, (first, second) -> total + distanceCoordsKM(first, second) }

fun coordsListToPairList(list: List<Coord>): List<Pair<Coord, Coord>> {
    val coordPairs: MutableList<Pair<Coord, Coord>> = arrayListOf()

    var lastCoord: Coord? = null
    list.forEach { thisCoord ->
        if (lastCoord != null) {
            coordPairs.add(Pair(lastCoord!!, thisCoord))
        }
        lastCoord = thisCoord
    }

    return coordPairs
}

fun coordToPoint(coord: Coord): Point = Point(DegreeCoordinate(coord.latitude), DegreeCoordinate(coord.longitude))