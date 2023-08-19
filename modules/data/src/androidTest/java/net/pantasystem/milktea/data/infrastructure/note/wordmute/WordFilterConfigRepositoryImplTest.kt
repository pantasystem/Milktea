package net.pantasystem.milktea.data.infrastructure.note.wordmute

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.model.note.muteword.FilterConditionType
import net.pantasystem.milktea.model.note.muteword.WordFilterConfig
import net.pantasystem.milktea.model.note.muteword.WordFilterConfigRepository
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class WordFilterConfigRepositoryImplTest {

    lateinit var repository: WordFilterConfigRepository
    lateinit var scope: CoroutineScope
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        scope = CoroutineScope(SupervisorJob())
        val database = Room.inMemoryDatabaseBuilder(context, DataBase::class.java).build()
        repository = WordFilterConfigRepositoryImpl(scope, database.wordFilterConfigDao(), object : Logger.Factory {
            override fun create(tag: String): Logger {
                return object : Logger {
                    override val defaultTag: String
                        get() = ""

                    override fun debug(msg: String, tag: String, e: Throwable?) {
                    }

                    override fun error(msg: String, e: Throwable?, tag: String) {
                    }

                    override fun info(msg: String, tag: String, e: Throwable?) {
                    }

                    override fun warning(msg: String, tag: String, e: Throwable?) {
                    }

                    override fun debug(tag: String, e: Throwable?, message: () -> String) {

                    }

                }
            }

        }, WordFilterConfigCache(), Dispatchers.Default)
    }

    @Test
    fun saveAndGet() {
        val config = WordFilterConfig(
            listOf(
                FilterConditionType.Normal(listOf("hoge", "fizz", "panta")),
                FilterConditionType.Normal(listOf("piyo", "buzz")),
                FilterConditionType.Normal(listOf("fuga", "moga")),
                FilterConditionType.Regex("\\hogepiyo\\")
            )
        )
        runBlocking {
            repository.save(config).getOrThrow()
            Assert.assertEquals(config, repository.get().getOrThrow())

        }

    }
}