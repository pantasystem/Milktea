package net.pantasystem.milktea.data.model.group

class GroupNotFoundException(groupId: Group.Id) : NoSuchElementException("${groupId}は存在しません。")