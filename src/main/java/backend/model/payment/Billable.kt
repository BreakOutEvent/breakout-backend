package backend.model.payment

import org.javamoney.moneta.Money

interface Billable {
    fun billableAmount(): Money
}

fun List<Billable>.billableAmount(): Money {
    return this.foldRight(backend.util.euroOf(0.0)) { a, b ->
        a.billableAmount().add(b)
    }
}
