package backend.model.user

import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue("FINANCE_MANAGER")
class FinanceManager : UserRole {

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(account: UserAccount) : super(account)

    override fun getAuthority(): String = "FINANCE_MANAGER"
}