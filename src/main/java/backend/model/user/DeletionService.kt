package backend.model.user

import backend.model.event.Team

interface DeletionService {
    fun delete(user: User)
    fun deleteUserAccount(user: User)
    fun delete(team: Team)
}