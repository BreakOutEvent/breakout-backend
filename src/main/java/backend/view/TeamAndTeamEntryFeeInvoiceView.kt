package backend.view

import backend.model.event.Team

class TeamAndTeamEntryFeeInvoiceView {

    var team: TeamView? = null
    var invoice: TeamEntryFeeInvoiceView? = null

    constructor()

    constructor(team: Team) {
        this.team = TeamView(team, null)
        this.invoice = team.invoice?.let(::TeamEntryFeeInvoiceView)
    }
}