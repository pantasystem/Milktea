package jp.panta.misskeyandroidclient.gettters

import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.users.UserRepository

class Getters(
    noteRepository: NoteRepository,
    userRepository: UserRepository
) {
    val noteRelationGetter = NoteRelationGetter(noteRepository, userRepository)
}