
const notificationType = {
    follow: "follow",
    mention: "mention",
    reply: "reply",
    renote: "renote",
    quote: "quote",
    reaction: "reaction",
    pollVote: "pollVote",
    receiveFollowRequest: "receiveFollowRequest",
    followRequestAccepted: "followRequestAccepted"

};

function buildTitle(res, notification) {
    if(notification.type == 'reaction')  {
        return res.__('title.reaction', {name : getDisplayUserName(notification.user), reaction: notification.reaction });
    }

    return res._(`title.${notification.type}`, {name: getDisplayUserName(notification.user)});
}

function getShortMessageFromNote(note) {
    if(note.text || note.cw) {
        return note.cw ?? note.text;
    }
    if(note.files && note.files.length) {
        return `files: ${note.files.length}`;
    }
    if(note.poll != null) {
        return `choices: ${note.poll.choices}`;
    }
}

function buildBody(_, notification) {
    let type = notification.type;
    if(type == notificationType.follow) {
        return getDisplayUserName(notification.user);
    }
    if(type == notificationType.mention) {
        return getShortMessageFromNote(notification.note);
    }
    if(type == notificationType.reply) {
        return getShortMessageFromNote(notification.note);
    }
    if(type == notificationType.renote) {
        return getShortMessageFromNote(notification.note.renote);
    }
    if(type == notificationType.quote) {
        return getShortMessageFromNote(notification.note);
    }
    if(type == notificationType.reaction) {
        return getShortMessageFromNote(notification.note);
    }
    if(type == notificationType.pollVote) {
        return getShortMessageFromNote(notification.note);
    }
    return null;
}

class MessagingNotification {
    constructor(type, title, body) {
        this.type = type;
        this.title = title;
        this.body = body;
    }
}


module.exports = function generateNotification(res, notification) {

    let type = notification.type;
    let title = buildTitle(res, notification);
    let body = buildBody(res, notification);
    return new MessagingNotification(type, title, body);
}

function getDisplayUserName(user) {
    if(user.name) {
        return user.name;
    }
    return user.userName;
}