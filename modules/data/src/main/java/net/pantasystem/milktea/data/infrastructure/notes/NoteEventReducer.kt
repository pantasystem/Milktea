package net.pantasystem.milktea.data.infrastructure.notes

import net.pantasystem.milktea.api_streaming.NoteUpdated
import net.pantasystem.milktea.api_streaming.mastodon.EmojiReaction
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.reaction.ReactionCount

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
        myReaction = if(e.body.userId == account.remoteId) null else this.myReaction
    )
}

fun Note.onReacted(account: Account, e: NoteUpdated.Body.Reacted): Note {
    val hasItem = this.reactionCounts.any { count ->
        count.reaction == e.body.reaction
    }
    var list = this.reactionCounts.map { count ->
        if(count.reaction == e.body.reaction) {
            count.copy(count = count.count + 1)
        }else{
            count
        }
    }

    if(!hasItem) {
        list = list + ReactionCount(reaction = e.body.reaction, count = 1)
    }

    val emojis =when (val emoji = e.body.emoji) {
        null -> this.emojis
        else -> (this.emojis ?: emptyList()) + emoji
    }

    return this.copy(
        reactionCounts = list,
        myReaction = if(e.body.userId == account.remoteId) e.body.reaction else this.myReaction,
        emojis = emojis?.distinct()
    )
}

fun Note.onEmojiReacted(account: Account, e: EmojiReaction): Note {
    val reactionCount = ReactionCount(e.reaction, e.count)
    val hasItem = reactionCounts.any {
        it.reaction == e.reaction
    }
    var list = reactionCounts.map { count ->
        if (count.reaction == e.reaction) {
            reactionCount
        } else {
            count
        }
    }

    if (!hasItem) {
        list = list + reactionCount
    }

    val emojis = when(val emoji = e.toEmoji()) {
        null -> this.emojis
        else -> (this.emojis ?: emptyList()) + emoji
    }
    return this.copy(
        reactionCounts = list.filter {
            it.count > 0
        },
        myReaction = e.myReaction(account.remoteId),
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

fun Note.onIReacted(reaction: String) : Note {
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