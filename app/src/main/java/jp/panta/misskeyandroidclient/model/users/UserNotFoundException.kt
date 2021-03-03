package jp.panta.misskeyandroidclient.model.users

class UserNotFoundException(userId: User.Id) : NoSuchElementException("ユーザーが見つかりませんでした: $userId")