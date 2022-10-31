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
}