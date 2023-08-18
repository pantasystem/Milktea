package net.pantasystem.milktea.data.infrastructure.notes.wordmute

import net.pantasystem.milktea.model.note.muteword.WordFilterConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordFilterConfigCache @Inject constructor() {
    private var _config: WordFilterConfig? = null
    fun get(): WordFilterConfig? {
        return _config
    }

    fun put(config: WordFilterConfig?) {
        _config = config
    }
}