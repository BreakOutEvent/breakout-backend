package backend

import backend.model.user.User

interface Blockable {
    fun isBlockedBy(userId: Long?): Boolean
}

interface UserGenerated: Blockable {
    fun getUser(): User?

    override fun isBlockedBy(userId: Long?): Boolean {
        return getUser()?.isBlockedBy(userId) ?: false
    }
}

fun <T: Blockable> Iterable<T>.removeBlockedBy(userId: Long?): Iterable<T> {
    return this.filter { it.isBlockedBy(userId) }
}