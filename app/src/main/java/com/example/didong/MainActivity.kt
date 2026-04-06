package com.example.didong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.didong.ui.theme.DidongTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DidongTheme {
                var isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
                
                Scaffold { innerPadding ->
                    if (!isLoggedIn) {
                        AuthScreen(onLoginSuccess = { isLoggedIn = true })
                    } else {
                        val productViewModel: ProductViewModel = viewModel()
                        ProductScreen(
                            viewModel = productViewModel,
                            onLogout = { isLoggedIn = false },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
