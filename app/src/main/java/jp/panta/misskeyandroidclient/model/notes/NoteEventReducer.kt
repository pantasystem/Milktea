package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.streaming.NoteUpdated

private fun Note.onUnReacted(note: Note, account: Account, e: NoteUpdated.Body.Unreacted): Note {
    val list = note.reactionCounts.toMutableList()
    val newList = list.asSequence().map {
        if(it.reaction == e.body.reaction) {
            it.copy(count = it.count - 1)
        }else{
            it
        }
    }.filter {
        it.count > 0
    }.toList()

    return note.copy(
        reactionCounts = newList,
        myReaction = if(e.body.userId == account.remoteId) null else e.body.reaction

    )
}

private fun Note.onReacted(note: Note, account: Account, e: NoteUpdated.Body.Reacted): Note {
    var hasItem = false
    var list = note.reactionCounts.map { count ->
        if(count.reaction == e.body.reaction) {
            hasItem = true
            count.copy(count = count.count + 1)
        }else{
            count
        }
    }
    if(!hasItem) {
        val added = list.toMutableList()
        added.add(ReactionCount(reaction = e.body.reaction, count = 1))
        list = added
    }
    val emojis = e.body.emoji?.let {
        note.emojis?.let {
            it.toMutableList().also { eList ->
                eList.add(e.body.emoji)
            }
        }
    }?: note.emojis

    return note.copy(
        reactionCounts = list,
        myReaction = if(e.body.userId == account.remoteId) e.body.reaction else note.myReaction,
        emojis = emojis
    )
}

private fun Note.onPollVoted(note: Note, account: Account, e: NoteUpdated.Body.PollVoted): Note {
    val poll = note.poll
    requireNotNull(poll){
        "pollがNULLです"
    }
    val updatedChoices = poll.choices.mapIndexed { index, choice ->
        if(index == e.body.choice) {
            choice.copy(
                votes = choice.votes + 1,
                isVoted = if(e.body.userId == account.remoteId) true else choice.isVoted
            )
        }else{
            choice
        }
    }
    return note.copy(
        poll = poll.copy(choices = updatedChoices)
    )
}