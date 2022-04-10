package backend.controller

import backend.configuration.CustomUserDetails
import backend.controller.exceptions.NotFoundException
import backend.controller.exceptions.UnauthorizedException
import backend.exceptions.DomainException
import backend.model.event.TeamService
import backend.model.event.TeamSummaryProjection
import backend.model.user.Participant
import backend.model.user.UserService
import backend.view.user.UserView
import io.swagger.annotations.Api
import backend.model.user.*
import backend.view.TeamView
import backend.model.event.Team
import org.springframework.web.bind.annotation.*
import backend.view.TeamEntryFeeInvoiceView
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Api
@RestController
@RequestMapping("/team")
class TeamControllerV2(val userService: UserService, val teamService: TeamService, val deletionService: DeletionService) {


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{teamId}/startingfee")
    fun getInvoiceForTeam(@PathVariable teamId: Long,
                          @AuthenticationPrincipal customUserDetails: CustomUserDetails): TeamEntryFeeInvoiceView {

        val user = userService.getUserFromCustomUserDetails(customUserDetails)
        val team = teamService.findOne(teamId) ?: throw NotFoundException("Team with id $teamId not found")

        user.getRole(Participant::class)?.let {
            if (team.isMember(it)) {
                val invoice = team.invoice ?: throw DomainException("Team has no invoice")
                return TeamEntryFeeInvoiceView(invoice)
            } else {
                throw UnauthorizedException("Participant is not a member of team $teamId")
            }
        }

        throw UnauthorizedException("User is no participant")
    }

    @GetMapping("/")
    fun getAllTeamsOverview(): Iterable<TeamSummaryProjection> {
        return teamService.findAllTeamSummaryProjections()
    }

      /**
     * DELETE /team/{id}/
     * Deletes team with given id
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/")
    fun deleteTeam(@PathVariable id: Long,
                   @AuthenticationPrincipal customUserDetails: CustomUserDetails): TeamView {

        val deleter = userService.getUserFromCustomUserDetails(customUserDetails)

        // TODO: Hier check, ob user im jeweiligen Team! und wegen weiteren Bedinungen bei Josi nachfragen
       // if (deleter.account.id != id ) // && !deleter.hasRole(Admin::class)
      //      throw UnauthorizedException("authenticated user and requested resource mismatch")

        val team = teamService.findOne(id) ?: throw NotFoundException("Team with id $id not found")
        deletionService.delete(team)

        return TeamView(team, customUserDetails.id)
    }
}
