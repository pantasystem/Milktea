package net.pantasystem.milktea.note.viewmodel

import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.note.R

object CwTextGenerator {

    operator fun invoke(note: NoteRelation?, isFolding: Boolean): StringSource {
        if (note == null) {
            return StringSource("()")
        }
        val textSize = note.note.text?.let {
            it.codePointCount(0, it.length)
        }
        val fileSize = note.note.fileIds?.size

        val buttonText = listOfNotNull(
            textSize?.let {
                StringSource(R.string.char_count, it)
            },
            fileSize?.let {
                StringSource(R.string.file_count, it)
            },
            note.note.poll?.let {
                StringSource(R.string.poll)
            },
        )
        val result = buttonText.reduceIndexed { index, acc, stringSource ->
            acc + if (index > 0) {
                StringSource("/") + stringSource
            } else {
                stringSource
            }
        }

        return if (isFolding) StringSource(R.string.show_more, result) else StringSource(R.string.hide)

    }
}