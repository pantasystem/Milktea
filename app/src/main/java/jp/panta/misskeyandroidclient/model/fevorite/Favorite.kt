package jp.panta.misskeyandroidclient.model.fevorite

import jp.panta.misskeyandroidclient.model.notes.Note

data class Favorite(val id: String, val createdAt: String, val note: Note, val noteId: String)