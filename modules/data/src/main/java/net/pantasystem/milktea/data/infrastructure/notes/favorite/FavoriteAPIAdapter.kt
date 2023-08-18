package net.pantasystem.milktea.data.infrastructure.notes.favorite

import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.api.misskey.notes.favorite.CreateFavorite
import net.pantasystem.milktea.api.misskey.notes.favorite.DeleteFavorite
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.note.Note
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteAPIAdapter @Inject constructor(
    val misskeyAPIProvider: MisskeyAPIProvider,
    val mastodonAPIProvider: MastodonAPIProvider,
    val getAccount: GetAccount,
) {

    suspend fun create(noteId: Note.Id): SuccessfulResponseData {
        val account = getAccount.get(noteId.accountId)
        return when(account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                misskeyAPIProvider.get(account).createFavorite(CreateFavorite(i = account.token, noteId = noteId.noteId))
                    .throwIfHasError()
                SuccessfulResponseData.Misskey
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                val status = mastodonAPIProvider.get(account).favouriteStatus(noteId.noteId)
                    .throwIfHasError()
                    .body()
                SuccessfulResponseData.Mastodon(requireNotNull(status))
            }
        }
    }

    suspend fun delete(noteId: Note.Id): SuccessfulResponseData {
        val account = getAccount.get(noteId.accountId)
        return when(account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                misskeyAPIProvider.get(account).deleteFavorite(DeleteFavorite(i = account.token, noteId = noteId.noteId))
                    .throwIfHasError()
                SuccessfulResponseData.Misskey
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                val status = mastodonAPIProvider.get(account).unfavouriteStatus(noteId.noteId)
                    .throwIfHasError()
                    .body()
                SuccessfulResponseData.Mastodon(requireNotNull(status))
            }
        }
    }
}

sealed interface SuccessfulResponseData {
    object Misskey : SuccessfulResponseData
    data class Mastodon(val tootStatusDTO: TootStatusDTO) : SuccessfulResponseData
}