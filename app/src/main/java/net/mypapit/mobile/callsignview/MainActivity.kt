package net.mypapit.mobile.callsignview

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import net.mypapit.mobile.callsignview.ui.navigation.MYCallsignApp
import net.mypapit.mobile.callsignview.ui.theme.MYCallsignTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        val container = (application as MYCallsignApplication).container
        setContent {
            MYCallsignTheme {
                MYCallsignApp(container = container)
            }
        }
    }
}
