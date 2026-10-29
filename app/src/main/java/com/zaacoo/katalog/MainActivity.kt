package com.zaacoo.katalog

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zaacoo.katalog.ui.ZaaCoApp
import com.zaacoo.katalog.ui.theme.ZaaCoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )

        val container = (application as KatalogApplication).container

        setContent {
            ZaaCoTheme {
                ZaaCoApp(container = container)
            }
        }
    }
}
