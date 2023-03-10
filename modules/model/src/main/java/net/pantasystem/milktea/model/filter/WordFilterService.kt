package net.pantasystem.milktea.model.filter

import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.muteword.WordFilterConfigRepository
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordFilterService @Inject constructor(
    private val mastodonWordFilterRepository: MastodonWordFilterRepository,
    private val wordFilterConfigRepository: WordFilterConfigRepository,
    private val accountRepository: AccountRepository,
    private val mastodonWordFilterService: MastodonFilterService
) {
    suspend fun isShouldFilterNote(pageable: Pageable, note: Note?): Boolean {
        note ?: return false
        val account = accountRepository.get(note.id.accountId).getOrNull()
            ?: return false

        if (note.userId == User.Id(account.accountId, account.remoteId)) {
            return false
        }
        val config = wordFilterConfigRepository.get().getOrNull()
        if (config != null) {
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
        }

        if (note.type !is Note.Type.Mastodon) {
            return false
        }

        val filter = mastodonWordFilterRepository.findAll(note.id.accountId).getOrNull()
            ?: return false
        return mastodonWordFilterService.isShouldFilterNote(pageable, filter, note)

    }

}