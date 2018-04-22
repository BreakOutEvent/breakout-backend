package backend.model

interface Reportable {
    var reported: Boolean
}

fun <T: Reportable> Iterable<T>.removeReported(): Iterable<T> {
    return this.filter { !it.reported }
}