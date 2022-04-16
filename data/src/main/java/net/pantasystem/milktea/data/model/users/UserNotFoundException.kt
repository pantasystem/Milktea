package net.pantasystem.milktea.data.model.users

class UserNotFoundException(userId: User.Id?, userName: String? = null, host: String? = null) : NoSuchElementException("ユーザーが見つかりませんでした: id:$userId, userName:$userName, host:$host")