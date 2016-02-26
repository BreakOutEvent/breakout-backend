package backend.utils

import backend.model.misc.Coords
import com.grum.geocalc.*
import com.grum.geocalc.Point

/**
 * Used Library Geocalc:
 * Copyright (c) 2015, Grumlimited Ltd (Romain Gallet)
 *
 * https://github.com/grumlimited/geocalc
 *
 * Full License Text: https://github.com/grumlimited/geocalc/blob/master/LICENSE.txt
 */

fun distanceCoordsKM(from: Coords, to: Coords): Double {
    val fromPoint: Point = coordsToPoint(from)
    val toPoint: Point = coordsToPoint(to)

    return EarthCalc.getVincentyDistance(fromPoint, toPoint) / 1000
}

fun distanceCoordsListKM(list: List<Coords>): Double {
    return coordsListToPairList(list).fold(0.0) { total, next -> total + distanceCoordsKM(next.first, next.second) }
}

fun coordsListToPairList(list: List<Coords>): List<Pair<Coords, Coords>> {
    val coordPairs: MutableList<Pair<Coords, Coords>> = arrayListOf()

    var lastCoord: Coords? = null
    list.forEach { thisCoord ->
        if (lastCoord != null) {
            coordPairs.add(Pair(lastCoord!!, thisCoord))
        }
        lastCoord = thisCoord
    }

    return coordPairs
}

fun coordsToPoint(coords: Coords): Point {
    return Point(DegreeCoordinate(coords.latitude), DegreeCoordinate(coords.longitude))
}
