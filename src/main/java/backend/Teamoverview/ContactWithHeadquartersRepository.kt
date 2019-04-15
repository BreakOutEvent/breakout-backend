package backend.teamoverview

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface ContactWithHeadquartersRepository : CrudRepository<ContactWithHeadquarters, Long> {

    fun findAllByTeamId(teamId: Long): Iterable<ContactWithHeadquarters>

    @Query("""
        select *
        from contact_with_headquarters
        where team_id = :id
        order by id desc
        limit 1
    """, nativeQuery = true)
    fun findLastContactByTeamId(@Param("id") id: Long): ContactWithHeadquarters?
}