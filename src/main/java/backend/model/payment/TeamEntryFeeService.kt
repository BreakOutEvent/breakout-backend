package backend.model.payment

import backend.model.user.Admin
import backend.model.user.User
import org.javamoney.moneta.Money
import java.time.LocalDateTime

interface TeamEntryFeeService {

    fun findById(id: Long): TeamEntryFeeInvoice?

    fun findByPurposeOfTransferCode(purposeOfTransferCode: String): TeamEntryFeeInvoice?

    fun save(invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice

    fun addAdminPaymentToInvoice(admin: User, amount: Money, invoice: TeamEntryFeeInvoice, date: LocalDateTime?, fidorId: Long?): TeamEntryFeeInvoice

    fun addSepaPaymentToInvoice(admin: User, fidorId: Long, amount: Money, date: LocalDateTime?, invoice: TeamEntryFeeInvoice): TeamEntryFeeInvoice

    fun findAll(): Iterable<TeamEntryFeeInvoice>

}
