package net.pantasystem.milktea.model.user

class UserNotFoundException(userId: User.Id?, userName: String? = null, host: String? = null) : NoSuchElementException("ユーザーが見つかりませんでした: id:$userId, userName:$userName, host:$host")