package net.pantasystem.milktea.data.infrastructure

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val testDb = "test-db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        DataBase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrate24To25() {
        helper.createDatabase(testDb, 24)
        helper.runMigrationsAndValidate(testDb, 25, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate23To25() {
        helper.createDatabase(testDb, 23)
        helper.runMigrationsAndValidate(testDb, 25, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate22To25() {
        helper.createDatabase(testDb, 22)
        helper.runMigrationsAndValidate(testDb, 25, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate21To25() {
        helper.createDatabase(testDb, 21)
        helper.runMigrationsAndValidate(testDb, 25, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To25() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 25, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate25To26() {
        helper.createDatabase(testDb, 25)
        helper.runMigrationsAndValidate(testDb, 26, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To26() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 26, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate26To27() {
        helper.createDatabase(testDb, 26)
        helper.runMigrationsAndValidate(testDb, 27, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate25To27() {
        helper.createDatabase(testDb, 25)
        helper.runMigrationsAndValidate(testDb, 27, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate24To27() {
        helper.createDatabase(testDb, 24)
        helper.runMigrationsAndValidate(testDb, 27, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To27() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 27, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To28() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 28, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To29() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 29, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To30() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 30, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To31() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 31, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To32() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 32, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To33() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 33, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To34() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 34, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To35() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 35, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To36() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 36, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To37() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 37, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To38() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 38, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To39() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 39, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To40() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 40, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To41() {
        helper.createDatabase(testDb, 11)
        helper.runMigrationsAndValidate(testDb, 41, true)
    }


    @Test
    @Throws(IOException::class)
    fun migrate40To41() {
        helper.createDatabase(testDb, 40)
        helper.runMigrationsAndValidate(testDb, 41, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate41To42() {
        helper.createDatabase(testDb, 41)
        helper.runMigrationsAndValidate(testDb, 42, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate42To43() {
        helper.createDatabase(testDb, 42)
        helper.runMigrationsAndValidate(testDb, 43, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate43To44() {
        helper.createDatabase(testDb, 43)
        helper.runMigrationsAndValidate(testDb, 44, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate44To45() {
        helper.createDatabase(testDb, 44)
        helper.runMigrationsAndValidate(testDb, 45, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate45To46() {
        helper.createDatabase(testDb, 45)
        helper.runMigrationsAndValidate(testDb, 46, true)

    }

    @Test
    @Throws(IOException::class)
    fun migrate46To47() {
        helper.createDatabase(testDb, 46)
        helper.runMigrationsAndValidate(testDb, 47, true)

    }

    @Test
    @Throws(IOException::class)
    fun migrate47To48() {
        helper.createDatabase(testDb, 47)
        helper.runMigrationsAndValidate(testDb, 48, true)

    }

    @Test
    @Throws(IOException::class)
    fun migrate48To49() {
        helper.createDatabase(testDb, 48)
        helper.runMigrationsAndValidate(testDb, 49, true)

    }

    @Test
    fun migrate49To50() {
        helper.createDatabase(testDb, 49)
        helper.runMigrationsAndValidate(testDb, 50, true)
    }

    @Test
    fun migrate50To51() {
        helper.createDatabase(testDb, 50)
        helper.runMigrationsAndValidate(testDb, 51, true)
    }

    @Test
    fun migrate51To52() {
        helper.createDatabase(testDb, 51)
        helper.runMigrationsAndValidate(testDb, 52, true, MIGRATION_51_52)
    }

    @Test
    fun migrate51To54() {
        helper.createDatabase(testDb, 51)
        helper.runMigrationsAndValidate(testDb, 54, true, MIGRATION_51_52)
    }

}