package com.zaacoo.katalog

import android.content.Context
import com.zaacoo.katalog.data.ApiClient
import com.zaacoo.katalog.data.CartStore
import com.zaacoo.katalog.data.KatalogRepository
import com.zaacoo.katalog.data.Prefs

class AppContainer(context: Context) {
    val prefs = Prefs(context)
    val api = ApiClient(prefs)
    val repository = KatalogRepository(api, prefs)
    val cart = CartStore(prefs)
}
