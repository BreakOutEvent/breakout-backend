package backend.util

import org.javamoney.moneta.Money

fun euroOf(value: Number): Money {
    return Money.of(value, "EUR")
}
