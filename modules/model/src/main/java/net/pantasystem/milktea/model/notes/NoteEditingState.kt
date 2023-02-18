package net.pantasystem.milktea.model.notes


import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.notes.poll.CreatePoll
import java.util.*



sealed interface PollExpiresAt : java.io.Serializable {
    object Infinity : PollExpiresAt
    data class DateAndTime(val expiresAt: Date) : PollExpiresAt {
        constructor(expiresAt: Instant) : this(Date(expiresAt.toEpochMilliseconds()))
        val year = expiresAt.let {
            val cal = Calendar.getInstance()
            cal.time = it
            cal.get(Calendar.YEAR)
        }
        val month = expiresAt.let {
            val cal = Calendar.getInstance()
            cal.time = it
            cal.get(Calendar.MONTH) + 1
        }
        val dayOfMonth = expiresAt.let {
            val cal = Calendar.getInstance()
            cal.time = it
            cal.get(Calendar.DAY_OF_MONTH)
        }

        val hour = expiresAt.let {
            val cal = Calendar.getInstance()
            cal.time = it
            cal.get(Calendar.HOUR)
        }

        val minutes = expiresAt.let {
            val cal = Calendar.getInstance()
             cal.time = it
            cal.get(Calendar.MINUTE)
        }
    }

    fun asDate(): Date? {
        return this.expiresAt()?.toEpochMilliseconds()?.let {
            Date(it)
        }
    }
}

fun PollExpiresAt.expiresAt(): Instant? {
    return when (this) {
        is PollExpiresAt.Infinity -> null
        is PollExpiresAt.DateAndTime -> Instant.fromEpochMilliseconds(this.expiresAt.time)
    }
}

data class PollEditingState(
    val choices: List<PollChoiceState>,
    val multiple: Boolean,
    val expiresAt: PollExpiresAt = PollExpiresAt.Infinity
) : java.io.Serializable {

    val isExpiresAtDateTime: Boolean
        get() = expiresAt is PollExpiresAt.DateAndTime

    fun checkValidate(): Boolean {
        return choices.all {
            it.text.isNotBlank()
        } && this.choices.size >= 2
    }

    fun toggleMultiple(): PollEditingState {
        return this.copy(
            multiple = !this.multiple
        )
    }
}

data class PollChoiceState(
    val text: String,
    val id: UUID = UUID.randomUUID()
) : java.io.Serializable

fun PollEditingState.toCreatePoll(): CreatePoll {
    return CreatePoll(
        choices = this.choices.map {
            it.text
        },
        multiple = multiple,
        expiresAt = expiresAt.expiresAt()?.toEpochMilliseconds()
    )
}




fun List<AppFile>.toggleFileSensitiveStatus(appFile: AppFile.Local): List<AppFile> {
    return this.map {
        if (it === appFile || it is AppFile.Local && it.isAttributeSame(appFile)) {
            appFile.copy(isSensitive = !appFile.isSensitive)
        } else {
            it
        }
    }
}


fun List<AppFile>.updateFileName(appFile: AppFile.Local, name: String): List<AppFile> {
    return this.map {
        if (it === appFile || it is AppFile.Local && it.isAttributeSame(appFile)) {
            appFile.copy(name = name)
        } else {
            it
        }
    }
}

fun List<AppFile>.updateFileComment(appFile: AppFile.Local, comment: String): List<AppFile> {
    return this.map {
        if (it === appFile || it is AppFile.Local && it.isAttributeSame(appFile)) {
            appFile.copy(comment = comment)
        } else {
            it
        }
    }
}
fun String?.addMentionUserNames(userNames: List<String>, pos: Int): Pair<String?, Int> {
    val mentionBuilder = StringBuilder()
    userNames.forEachIndexed { index, userName ->
        if (index < userNames.size - 1) {
            // NOTE: 次の文字がつながらないようにする
            mentionBuilder.appendLine("$userName ")
        } else {
            // NOTE: 次の文字がつながらないようにする
            mentionBuilder.append("$userName ")
        }
    }
    val builder = StringBuilder(this ?: "")
    builder.insert(pos, mentionBuilder.toString())
    val nextPos = pos + mentionBuilder.length
    return builder.toString() to nextPos
}

fun List<AppFile>.removeFile(appFile: AppFile): List<AppFile> {
    return toMutableList().apply {
        remove(appFile)
    }
}


fun PollEditingState?.removePollChoice(id: UUID): PollEditingState? {
    return this?.copy(
        choices = this.choices.filterNot { choice ->
            choice.id == id
        }
    )
}

fun PollEditingState?.updatePollChoice(id: UUID, text: String): PollEditingState? {
    return this?.copy(
        choices = choices.map { choice ->
            if (choice.id == id) {
                choice.copy(
                    text = text
                )
            } else {
                choice
            }
        }
    )
}

fun PollEditingState?.addPollChoice(): PollEditingState? {
    return this?.copy(
        choices = choices.toMutableList().also { list ->
            list.add(
                PollChoiceState("")
            )
        }
    )
}