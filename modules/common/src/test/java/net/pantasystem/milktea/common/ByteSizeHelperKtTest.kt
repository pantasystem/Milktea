package net.pantasystem.milktea.common

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ByteSizeHelperKtTest {

    @Test
    fun convertToHumanReadable_Give_1024() {
        Assertions.assertEquals("1KB", 1024L.convertToHumanReadable())
    }

    @Test
    fun convertToHumanReadable_Give_2049() {
        Assertions.assertEquals("2KB", 2049L.convertToHumanReadable())
    }

    @Test
    fun convertToHumanReadable_Give_4096() {
        Assertions.assertEquals("4KB", 4096L.convertToHumanReadable())
    }

    @Test
    fun convertToHumanReadable_Give_1048576_Returns_1MB() {
        Assertions.assertEquals("1MB", 1048576L.convertToHumanReadable())
    }

    @Test
    fun convertToHumanReadable_Give_1073741824_Returns_1GB() {
        Assertions.assertEquals("1GB", 1073741824L.convertToHumanReadable())
    }


    @Test
    fun convertToHumanReadable_Give_1099511627776_Returns_1TB() {
        Assertions.assertEquals("1TB", 1099511627776L.convertToHumanReadable())
    }

    @Test
    fun convertToHumanReadable_Give_1125899906842624_Returns_1PB() {
        Assertions.assertEquals("1PB", 1125899906842624L.convertToHumanReadable())
    }

}