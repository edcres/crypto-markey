package com.example.cryptomarket.data

import com.example.cryptomarket.data.coinsapi.CoinsApi
import com.example.cryptomarket.data.coinsapi.ticker.HistoricalTicker
import com.example.cryptomarket.data.newsapi.NewsApi

class Repository {
    suspend fun getCoins() = CoinsApi.coinsApiService.getCoins()

    suspend fun getGlobalData() = CoinsApi.coinsApiService.getGlobalData()

    suspend fun getNewsPosts() = NewsApi.newsApiService.getNewsPosts()

    suspend fun getHistoricalTickers(startTime: String, interval: String): List<HistoricalTicker> {
        return CoinsApi.coinsApiService.getHistoricalTickers(startTime, interval)
    }
}