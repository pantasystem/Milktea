package net.pantasystem.milktea.data.model.notes

import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.data.streaming.NoteUpdated

fun Note.onUnReacted(account: Account, e: NoteUpdated.Body.Unreacted): Note {
    val list = this.reactionCounts.toMutableList()
    val newList = list.asSequence().map {
        if(it.reaction == e.body.reaction) {
            it.copy(count = it.count - 1)
        }else{
            it
        }
    }.filter {
        it.count > 0
    }.toList()

    return this.copy(
        reactionCounts = newList,
        myReaction = if(e.body.userId == account.remoteId) null else e.body.reaction

    )
}

fun Note.onReacted(account: Account, e: NoteUpdated.Body.Reacted): Note {
    var hasItem = false
    var list = this.reactionCounts.map { count ->
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
        this.emojis?.let {
            it.toMutableList().also { eList ->
                eList.add(e.body.emoji)
            }
        }
    }?: this.emojis

    return this.copy(
        reactionCounts = list,
        myReaction = if(e.body.userId == account.remoteId) e.body.reaction else this.myReaction,
        emojis = emojis
    )
}

fun Note.onPollVoted(account: Account, e: NoteUpdated.Body.PollVoted): Note {
    val poll = this.poll
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
    return this.copy(
        poll = poll.copy(choices = updatedChoices)
    )
}

fun Note.onIReacted(reaction: String) : Note{
    var hasItem = false
    var list = this.reactionCounts.map { count ->
        if(count.reaction == reaction) {
            hasItem = true
            count.copy(count = count.count + 1)
        }else{
            count
        }
    }
    if(!hasItem) {
        val added = list.toMutableList()
        added.add(ReactionCount(reaction = reaction, count = 1))
        list = added
    }

    return this.copy(
        reactionCounts = list,
        myReaction = reaction,
        emojis = emojis
    )
}

fun Note.onIUnReacted() : Note {
    val list = this.reactionCounts.toMutableList()
    val newList = list.asSequence().map {
        if(it.reaction == myReaction) {
            it.copy(count = it.count - 1)
        }else{
            it
        }
    }.filter {
        it.count > 0
    }.toList()

    return this.copy(
        reactionCounts = newList,
        myReaction = null

    )
}