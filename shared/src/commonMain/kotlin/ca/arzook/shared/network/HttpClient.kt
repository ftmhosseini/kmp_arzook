package ca.arzook.shared.network

import io.ktor.client.*

expect fun httpClientEngine(): HttpClient

fun createHttpClient(): HttpClient = httpClientEngine()
