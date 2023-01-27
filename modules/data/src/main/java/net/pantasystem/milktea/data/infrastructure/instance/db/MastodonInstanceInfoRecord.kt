package net.pantasystem.milktea.data.infrastructure.instance.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import net.pantasystem.milktea.model.instance.MastodonInstanceInfo

@Entity(
    tableName = "mastodon_instance_info"
)
data class MastodonInstanceInfoRecord(
    @PrimaryKey(autoGenerate = false) val uri: String,
    val title: String,
    val description: String,
    val email: String,
    val version: String,
    @Embedded(prefix = "urls_") val urls: Urls,
    @Embedded(prefix = "configuration_") val configuration: Configuration? = null,
) {
    companion object;

    data class Configuration(
        @Embedded(prefix = "statuses_") val statuses: Statuses? = null,
        @Embedded(prefix = "polls_")val polls: Polls? = null
    ) {

        data class Statuses(
            val maxCharacters: Int? = null,
            val maxMediaAttachments: Int? = null,
        )

        data class Polls(
            val maxOptions: Int? = null,
            val maxCharactersPerOption: Int? = null,
            val minExpiration: Int? = null,
            val maxExpiration: Int? = null,
        )

    }

    data class Urls(
        val streamingApi: String? = null,
    )
}


fun MastodonInstanceInfoRecord.from(model: MastodonInstanceInfo): MastodonInstanceInfoRecord {
    return MastodonInstanceInfoRecord(
        uri = model.uri,
        title = model.title,
        description = model.description,
        email = model.email,
        urls = model.urls.let {
            MastodonInstanceInfoRecord.Urls(
                streamingApi = it.streamingApi
            )
        },
        version = model.version,
        configuration = model.configuration?.let { config ->
            MastodonInstanceInfoRecord.Configuration(
                statuses = config.statuses?.let {
                    MastodonInstanceInfoRecord.Configuration.Statuses(
                        maxCharacters = it.maxCharacters,
                        maxMediaAttachments = it.maxMediaAttachments,
                    )
                },
                polls = config.polls?.let {
                    MastodonInstanceInfoRecord.Configuration.Polls(
                        maxOptions = it.maxOptions,
                        maxCharactersPerOption = it.maxCharactersPerOption,
                        maxExpiration = it.maxExpiration,
                        minExpiration = it.minExpiration,
                    )
                }
            )
        }
    )
}

fun MastodonInstanceInfoRecord.toModel(): MastodonInstanceInfo {
    return MastodonInstanceInfo(
        uri = uri,
        title = title,
        description = description,
        email = email,
        urls = urls.let {
            MastodonInstanceInfo.Urls(
                streamingApi = it.streamingApi
            )
        },
        version = version,
        configuration = configuration?.let { config ->
            MastodonInstanceInfo.Configuration(
                statuses = config.statuses?.let {
                    MastodonInstanceInfo.Configuration.Statuses(
                        maxCharacters = it.maxCharacters,
                        maxMediaAttachments = it.maxMediaAttachments,
                    )
                },
                polls = config.polls?.let {
                    MastodonInstanceInfo.Configuration.Polls(
                        maxOptions = it.maxOptions,
                        maxCharactersPerOption = it.maxCharactersPerOption,
                        maxExpiration = it.maxExpiration,
                        minExpiration = it.minExpiration,
                    )
                }
            )
        }
    )
}