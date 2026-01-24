package com.example.tosspayment.payment.adapter.out.persistence

import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ExposedR2dbcConfiguration(
    @Value("\${spring.r2dbc.url}") private val url: String,
    @Value("\${spring.r2dbc.username}") private val username: String,
    @Value("\${spring.r2dbc.password}") private val password: String
) {

    @Bean
    fun r2dbcDatabase(): R2dbcDatabase {
        return R2dbcDatabase.connect(
            url = url,
            user = username,
            password = password
        )
    }
}
