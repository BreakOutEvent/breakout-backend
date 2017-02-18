package backend.util

import java.util.stream.Stream

fun <T> Collection<T>.parallelStream(): Stream<T> {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")
    return (this as java.util.Collection<T>).parallelStream()
}

fun <T> Collection<T>.stream(): Stream<T> {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")
    return (this as java.util.Collection<T>).stream()
}