package com.example.tosspayment.payment.adapter.`in`.web.response

import org.springframework.http.HttpStatus

data class ApiResponse<T>(
    val statue: Int = 200,
    val message: String = "",
    val data: T? = null
) {

    companion object {
        fun <T> with(httpStatus: HttpStatus, message: String, data: T?): ApiResponse<T> {
            return ApiResponse(statue = httpStatus.value(), message = message, data = data)
        }
    }
}