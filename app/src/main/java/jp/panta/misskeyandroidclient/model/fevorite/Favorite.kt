package jp.panta.misskeyandroidclient.model.fevorite

import jp.panta.misskeyandroidclient.api.notes.Note

data class Favorite(val id: String, val createdAt: String, val note: Note, val noteId: String)