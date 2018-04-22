package backend.model

import backend.model.user.User

interface Blockable {
    fun isBlockedBy(userId: Long?): Boolean
}

interface Blocker {
    fun isBlocking(user: User?): Boolean
}

interface UserGenerated: Blockable, Blocker {
    fun getUser(): User?

    override fun isBlockedBy(userId: Long?): Boolean {
        return getUser()?.isBlockedBy(userId) ?: false
    }

    override fun isBlocking(user: User?): Boolean {
        return getUser()?.isBlocking(user) ?: false
    }
}

fun <T: Blockable> Iterable<T>.removeBlockedBy(userId: Long?): Iterable<T> {
    return this.filter { !it.isBlockedBy(userId) }
}

fun <T: Blocker> Iterable<T>.removeBlocking(user: User?): Iterable<T> {
    return this.filter { !it.isBlocking(user) }
}