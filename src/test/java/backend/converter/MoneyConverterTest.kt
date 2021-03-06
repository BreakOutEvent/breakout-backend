package backend.converter

import org.javamoney.moneta.Money
import org.junit.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals

class MoneyConverterTest {

    @Test
    fun testConvertToDatabaseColumn() {
        val r = Random().nextDouble()
        val money = Money.of(BigDecimal.valueOf(r), "EUR")
        val converter = MoneyConverter()

        val converted = converter.convertToDatabaseColumn(money)
        assertEquals(money.toString(), converted)
    }

    @Test
    fun testConvertToEntityAttribute() {
        val r = Random().nextDouble()
        val string = "EUR $r"
        val converter = MoneyConverter()

        val converted = converter.convertToEntityAttribute(string)
        assertEquals(Money.parse(string), converted)
    }

    @Test
    fun testConvertWorksWithoutDataLoss() {
        val r = Random().nextDouble()
        val money = Money.of(BigDecimal.valueOf(r), "EUR")
        val converter = MoneyConverter()
        val fromMoneyToString = converter.convertToDatabaseColumn(money)
        val fromStringToMoney = converter.convertToEntityAttribute(fromMoneyToString)

        assertEquals(money, fromStringToMoney)
    }

    @Test
    fun testConvertWorksWithOldValuesAndNewMonetaVersion() {
        val fromDB1 = "EUR 363.02"
        val fromDB2 = "EUR 2.8E+2"

        val converter = MoneyConverter()

        // should not fail
        converter.convertToEntityAttribute(fromDB1)
        converter.convertToEntityAttribute(fromDB2)
    }
}
