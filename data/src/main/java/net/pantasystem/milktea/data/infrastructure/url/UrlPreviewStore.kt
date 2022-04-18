package net.pantasystem.milktea.data.infrastructure.url

interface UrlPreviewStore{

    fun get(url: String): UrlPreview?
}