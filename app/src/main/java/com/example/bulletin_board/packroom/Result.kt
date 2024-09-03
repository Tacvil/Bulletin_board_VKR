package com.example.bulletin_board.packroom

sealed class Result<out T> {
    data class Success<out T>(
        val data: T,
    ) : Result<T>()

    data class Error(
        val exception: Throwable, // Используйте Throwable вместо Exception
    ) : Result<Nothing>()
}
