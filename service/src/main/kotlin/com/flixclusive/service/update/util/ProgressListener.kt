package com.flixclusive.service.update.util

interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}