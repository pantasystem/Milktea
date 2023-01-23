package net.pantasystem.milktea.data.infrastructure.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.common.getPreferences
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.Theme
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class LocalConfigRepositoryImplTest {

    lateinit var localConfigRepository: LocalConfigRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        localConfigRepository = LocalConfigRepositoryImpl(
            sharedPreference = context.getPreferences()
        )

    }

    @Test
    fun get() {
        val config = localConfigRepository.get().getOrThrow()
        Assert.assertEquals(DefaultConfig.config, config)
        val expect = config.copy(isPostButtonAtTheBottom = false, isClassicUI = true)
        runBlocking {
            localConfigRepository.save(expect).getOrThrow()
        }
        Assert.assertEquals(expect, localConfigRepository.get().getOrThrow())
    }

    @Test
    fun save() {
        val expect = DefaultConfig.config.copy(
            isPostButtonAtTheBottom = false,
            isClassicUI = true,
            noteExpandedHeightSize = 100,
            theme = Theme.Black
        )
        runBlocking {
            localConfigRepository.save(expect).getOrThrow()
        }
        Assert.assertEquals(expect, localConfigRepository.get().getOrThrow())
    }
}