package com.example.didong

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ProductScreen(
    viewModel: ProductViewModel, 
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Hộp thoại xác nhận xóa
    if (showDeleteDialog && productToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false 
                productToDelete = null
            },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa sản phẩm '${productToDelete?.name}' không?") },
            confirmButton = {
                Button(
                    onClick = {
                        productToDelete?.let { viewModel.deleteProduct(it) }
                        showDeleteDialog = false
                        productToDelete = null
                        scope.launch {
                            snackbarHostState.showSnackbar("Đã xóa sản phẩm")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    productToDelete = null
                }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Hộp thoại xác nhận đăng xuất
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Xác nhận đăng xuất") },
            text = { Text("Bạn có chắc chắn muốn đăng xuất không?") },
            confirmButton = {
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Đăng xuất")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Dữ liệu sản phẩm", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Chào: ${currentUser?.email ?: "Người dùng"}", fontSize = 12.sp, color = Color.Gray)
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên sản phẩm") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Loại sản phẩm") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Giá") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (name.isBlank() || type.isBlank() || price.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng điền đầy đủ thông tin")
                            }
                            return@Button
                        }
                        
                        val priceValue = price.toDoubleOrNull()
                        if (priceValue == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Giá phải là một số")
                            }
                            return@Button
                        }

                        if (editingProduct == null) {
                            viewModel.addProduct(name, type, priceValue)
                            scope.launch {
                                snackbarHostState.showSnackbar("Thêm sản phẩm thành công")
                            }
                        } else {
                            viewModel.updateProduct(
                                editingProduct!!.id,
                                name,
                                type,
                                priceValue
                            )
                            editingProduct = null
                            scope.launch {
                                snackbarHostState.showSnackbar("Cập nhật sản phẩm thành công")
                            }
                        }
                        name = ""
                        type = ""
                        price = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (editingProduct == null) "THÊM SẢN PHẨM" else "CẬP NHẬT SẢN PHẨM")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Danh sách sản phẩm:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.products) { product ->
                        ProductItem(
                            product = product,
                            onEdit = {
                                editingProduct = product
                                name = product.name
                                type = product.type
                                price = product.price.toString()
                            },
                            onDelete = { 
                                productToDelete = product
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            // Hiển thị Snackbar ở phía trên cùng
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ProductItem(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Tên sp: ${product.name}", fontWeight = FontWeight.Bold)
                Text(text = "Giá sp: ${product.price}")
                Text(text = "Loại sp: ${product.type}")
            }
            Column {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Yellow)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}
