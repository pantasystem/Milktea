package net.pantasystem.milktea.model.filter

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.muteword.WordFilterConfigRepository
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordFilterService @Inject constructor(
    private val mastodonWordFilterRepository: MastodonWordFilterRepository,
    private val wordFilterConfigRepository: WordFilterConfigRepository,
    private val accountRepository: AccountRepository,
    private val mastodonWordFilterService: MastodonFilterService,
    private val clientWordFilterService: ClientWordFilterService,
) {

    suspend fun isShouldFilterNote(pageable: Pageable, noteRelation: NoteRelation?, a: Account? = null): Boolean {
        noteRelation ?: return false
        val account = a?: accountRepository.get(noteRelation.note.id.accountId).getOrNull()
            ?: return false
        return isShouldFilterNote(pageable, noteRelation.note, account)
                || isShouldFilterNote(pageable, noteRelation.reply, account)
                || isShouldFilterNote(pageable, noteRelation.renote, account)
    }

    suspend fun isShouldFilterNote(pageable: Pageable, note: Note?, a: Account? = null): Boolean {
        note ?: return false
        val account = a?: accountRepository.get(note.id.accountId).getOrNull()
            ?: return false

        if (note.userId == User.Id(account.accountId, account.remoteId)) {
            return false
        }
        val config = wordFilterConfigRepository.get().getOrNull()
        val isMatched = clientWordFilterService.isShouldFilterNote(config, note)
        if (isMatched) {
            return true
        }

        if (note.type !is Note.Type.Mastodon) {
            return false
        }

        val filter = mastodonWordFilterRepository.findAll(note.id.accountId).getOrNull()
            ?: return false
        return mastodonWordFilterService.isShouldFilterNote(pageable, filter, note)

    }

}