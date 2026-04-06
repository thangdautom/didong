package com.example.didong

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ProductViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    val products = mutableStateListOf<Product>()

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        db.collection("products")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ProductViewModel", "Error fetching products", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    products.clear()
                    for (doc in snapshot.documents) {
                        val product = doc.toObject(Product::class.java)?.copy(id = doc.id)
                        if (product != null) {
                            products.add(product)
                        }
                    }
                    Log.d("ProductViewModel", "Fetched ${products.size} products")
                }
            }
    }

    fun addProduct(name: String, type: String, price: Double) {
        val product = Product(name = name, type = type, price = price)
        db.collection("products").add(product.toMap())
            .addOnSuccessListener { Log.d("ProductViewModel", "Product added") }
            .addOnFailureListener { Log.e("ProductViewModel", "Error adding product", it) }
    }

    fun updateProduct(id: String, name: String, type: String, price: Double) {
        val updates = mapOf(
            "name" to name,
            "type" to type,
            "price" to price
        )
        db.collection("products").document(id).update(updates)
            .addOnSuccessListener { Log.d("ProductViewModel", "Product updated") }
            .addOnFailureListener { Log.e("ProductViewModel", "Error updating product", it) }
    }

    fun deleteProduct(product: Product) {
        db.collection("products").document(product.id).delete()
            .addOnSuccessListener { Log.d("ProductViewModel", "Product deleted") }
            .addOnFailureListener { Log.e("ProductViewModel", "Error deleting product", it) }
    }
}
