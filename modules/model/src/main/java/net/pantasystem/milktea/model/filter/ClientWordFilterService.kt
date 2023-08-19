package net.pantasystem.milktea.model.filter

import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.muteword.WordFilterConfig
import javax.inject.Inject

class ClientWordFilterService @Inject constructor() {
    fun isShouldFilterNote(
        config: WordFilterConfig?,
        note: Note?,
    ): Boolean {
        note ?: return false
        config ?: return false
        val isMatched = config.checkMatchText(note.text)
                || config.checkMatchText(note.cw)
                || note.poll?.let { poll ->
            poll.choices.any { choice ->
                config.checkMatchText(choice.text)
            }
        } ?: false
        if (isMatched) {
            return true
        }
        return false
    }
}