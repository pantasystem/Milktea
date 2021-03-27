package jp.panta.misskeyandroidclient.model.group

class GroupNotFoundException(groupId: Group.Id) : NoSuchElementException("${groupId}は存在しません。")