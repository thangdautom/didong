package com.example.didong

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    val products = mutableStateListOf<Product>()
    private var productListener: ListenerRegistration? = null
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin = _isAdmin.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                val email = user.email?.trim() ?: ""
                Log.d("AuthCheck", "User logged in: $email")
                checkAndSetAdmin(user.uid, email)
            } else {
                stopListening()
                products.clear()
                _isAdmin.value = false
                Log.d("AuthCheck", "User logged out")
            }
        }
    }

    private fun checkAndSetAdmin(uid: String, email: String) {
        if (email.equals("vana@gmail.com", ignoreCase = true)) {
            val adminData = Admin(uid, email)
            db.collection("admins").document(uid).set(adminData.toMap())
                .addOnSuccessListener { 
                    _isAdmin.value = true
                    _toastMessage.value = "Chào ADMIN vana@gmail.com"
                    fetchAllProducts() 
                }
                .addOnFailureListener { e ->
                    Log.e("AdminError", "Lỗi tạo Admin: ${e.message}")
                    _toastMessage.value = "Lỗi tạo Admin: ${e.message}"
                }
        } else {
            db.collection("admins").document(uid).get().addOnSuccessListener { doc ->
                val isUserAdmin = doc.exists()
                _isAdmin.value = isUserAdmin
                if (isUserAdmin) {
                    _toastMessage.value = "Đăng nhập quyền Admin"
                    fetchAllProducts()
                } else {
                    startListening(uid)
                }
            }.addOnFailureListener {
                Log.e("AdminError", "Lỗi kiểm tra quyền: ${it.message}")
                startListening(uid) // Mặc định về user thường nếu lỗi
            }
        }
    }

    private fun fetchAllProducts() {
        stopListening()
        productListener = db.collection("products")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirestoreError", "Admin fetch error: ${e.message}")
                    _toastMessage.value = "Lỗi tải tất cả SP: ${e.message}"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    products.clear()
                    for (doc in snapshot.documents) {
                        val product = doc.toObject(Product::class.java)?.apply { id = doc.id }
                        if (product != null) products.add(product)
                    }
                }
            }
    }

    private fun startListening(userId: String) {
        stopListening()
        productListener = db.collection("products")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirestoreError", "User fetch error: ${e.message}")
                    _toastMessage.value = "Lỗi tải SP cá nhân: ${e.message}"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    products.clear()
                    for (doc in snapshot.documents) {
                        val product = doc.toObject(Product::class.java)?.apply { id = doc.id }
                        if (product != null) products.add(product)
                    }
                }
            }
    }

    private fun stopListening() {
        productListener?.remove()
        productListener = null
    }

    fun clearToast() { _toastMessage.value = null }

    fun addProduct(name: String, type: String, price: Double) {
        val currentUser = auth.currentUser ?: return
        _isLoading.value = true
        val product = Product(userId = currentUser.uid, name = name, type = type, price = price)
        db.collection("products").add(product.toMap())
            .addOnSuccessListener { 
                _isLoading.value = false 
                _toastMessage.value = "Thêm thành công"
            }
            .addOnFailureListener {
                _isLoading.value = false
                _toastMessage.value = "Lỗi thêm: ${it.message}"
            }
    }

    fun updateProduct(id: String, name: String, type: String, price: Double) {
        _isLoading.value = true
        val updates = mapOf("name" to name, "type" to type, "price" to price)
        db.collection("products").document(id).update(updates)
            .addOnSuccessListener { 
                _isLoading.value = false 
                _toastMessage.value = "Cập nhật thành công"
            }
            .addOnFailureListener {
                _isLoading.value = false
                _toastMessage.value = "Lỗi cập nhật: ${it.message}"
            }
    }

    fun deleteProduct(product: Product) {
        db.collection("products").document(product.id).delete()
            .addOnSuccessListener { _toastMessage.value = "Đã xóa" }
            .addOnFailureListener { _toastMessage.value = "Lỗi xóa: ${it.message}" }
    }
}
