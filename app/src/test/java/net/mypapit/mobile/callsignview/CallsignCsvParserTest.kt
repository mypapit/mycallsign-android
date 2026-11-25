package net.mypapit.mobile.callsignview

import net.mypapit.mobile.callsignview.data.csv.CallsignCsvParser
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.StringReader

class CallsignCsvParserTest {

    @Test
    fun parsesQuotedCommasEmbeddedNewlinesAndDates() {
        val sample = """
            No.,Assignment Holder,Call Sign,Assign No,Expiry Date
            1,A RASHAD BIN MOHAMED,9m2vot,0523774,31/12/2031
            2357,"CHIEW KIN YAN, DARRYN",9W2XQN,0760475,31/12/2027
            9758,"KELAB KOMUNIKASI RADIO AMATUR KUALA MUDA, KEDAH (SP HAM 
            RADIO)",9M4CKM,0760342,31/12/2027
            9999,"TEST ""CLUB"", MALAYSIA",9W6TST,1234567,01/02/2020
            10000,CLASS C OPERATOR,9W3ABC,7654321,31/12/2030
            10068,MOHAMMAD SULAIMAN BIN ESA,9W4GBF,3720618,31/12/2026
        """.trimIndent()

        val result = CallsignCsvParser.parse(StringReader(sample))
        assertEquals(6, result.size)

        assertEquals("9M2VOT", result[0].callsign)
        assertEquals("2031-12-31", result[0].expiryDateIso)
        assertEquals("9M2", result[0].prefixGroup)

        assertEquals("CHIEW KIN YAN, DARRYN", result[1].holderName)
        assertEquals("9W2", result[1].prefixGroup)

        assertEquals(
            "KELAB KOMUNIKASI RADIO AMATUR KUALA MUDA, KEDAH (SP HAM RADIO)",
            result[2].holderName
        )
        assertEquals("9M4", result[2].prefixGroup)

        assertEquals("TEST \"CLUB\", MALAYSIA", result[3].holderName)
        assertEquals("2020-02-01", result[3].expiryDateIso)
        assertEquals("9W6", result[3].prefixGroup)

        assertEquals("9W3ABC", result[4].callsign)
        assertEquals("9W3", result[4].prefixGroup)

        assertEquals("9W4GBF", result[5].callsign)
        assertEquals("9W4", result[5].prefixGroup)
    }
}
