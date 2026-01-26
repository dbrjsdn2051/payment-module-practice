package com.example.tosspayment.common

import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

val objectMapper: JsonMapper = JsonMapper.builder()
    .addModule(kotlinModule())
    .build()
