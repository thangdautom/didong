package com.example.didong

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (isLogin) "Đăng nhập" else "Đăng ký tài khoản", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (!isLogin) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Họ và tên") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Số điện thoại") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Vui lòng nhập đầy đủ thông tin"
                        return@Button
                    }
                    isLoading = true
                    errorMessage = ""
                    
                    if (isLogin) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener { 
                                isLoading = false
                                onLoginSuccess() 
                            }
                            .addOnFailureListener { 
                                isLoading = false
                                errorMessage = it.message ?: "Lỗi đăng nhập" 
                            }
                    } else {
                        if (fullName.isEmpty() || phoneNumber.isEmpty()) {
                            errorMessage = "Vui lòng nhập đầy đủ thông tin cá nhân"
                            isLoading = false
                            return@Button
                        }
                        
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener { authResult ->
                                val userId = authResult.user?.uid
                                if (userId != null) {
                                    val userProfile = hashMapOf(
                                        "uid" to userId,
                                        "email" to email,
                                        "fullName" to fullName,
                                        "phoneNumber" to phoneNumber,
                                        "createdAt" to System.currentTimeMillis()
                                    )
                                    
                                    db.collection("users").document(userId)
                                        .set(userProfile)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            onLoginSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            errorMessage = "Lỗi lưu thông tin: ${e.message}"
                                        }
                                }
                            }
                            .addOnFailureListener { 
                                isLoading = false
                                errorMessage = it.message ?: "Lỗi đăng ký" 
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLogin) "Đăng nhập" else "Đăng ký")
            }
        }
        
        TextButton(onClick = { 
            isLogin = !isLogin 
            errorMessage = ""
        }) {
            Text(if (isLogin) "Chưa có tài khoản? Đăng ký ngay" else "Đã có tài khoản? Đăng nhập")
        }
    }
}
