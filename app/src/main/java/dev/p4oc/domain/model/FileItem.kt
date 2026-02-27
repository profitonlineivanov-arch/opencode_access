package dev.p4oc.domain.model

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val extension: String = name.substringAfterLast(".", "")
)

data class FileContent(
    val path: String,
    val content: String,
    val lineCount: Int,
    val language: String
)
