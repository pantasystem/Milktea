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
}