package backend.util

import backend.model.misc.Coord
import com.grum.geocalc.DegreeCoordinate
import com.grum.geocalc.EarthCalc
import com.grum.geocalc.Point

/**
 * Used Library Geocalc:
 * Copyright (c) 2015, Grumlimited Ltd (Romain Gallet)
 *
 * https://github.com/grumlimited/geocalc
 *
 * Full License Text: https://github.com/grumlimited/geocalc/blob/master/LICENSE.txt
 */
fun distanceCoordsKM(from: Coord, to: Coord): Double {
    val fromPoint: Point = coordToPoint(from)
    val toPoint: Point = coordToPoint(to)

    return EarthCalc.getVincentyDistance(fromPoint, toPoint) / 1000
}

fun distanceCoordsListKMfromStart(startingPoint: Coord, list: List<Coord>): Double {
    var coordsList = arrayListOf(startingPoint)
    coordsList.addAll(list)
    return distanceCoordsListKM(coordsList)
}

fun distanceCoordsListKM(list: List<Coord>): Double = coordsListToPairList(list).fold(0.0) { total, next -> total + distanceCoordsKM(next.first, next.second) }

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