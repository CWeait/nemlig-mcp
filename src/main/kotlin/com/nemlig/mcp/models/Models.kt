package com.nemlig.mcp.models

import kotlinx.serialization.Serializable

/**
 * Product model representing a Nemlig product
 */
@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val unit: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val inStock: Boolean = true,
    val nutritionalInfo: NutritionalInfo? = null
)

/**
 * Nutritional information for a product
 */
@Serializable
data class NutritionalInfo(
    val energyKj: Double? = null,
    val energyKcal: Double? = null,
    val fat: Double? = null,
    val saturatedFat: Double? = null,
    val carbohydrates: Double? = null,
    val sugar: Double? = null,
    val protein: Double? = null,
    val salt: Double? = null,
    val fiber: Double? = null
)

/**
 * Shopping cart model
 */
@Serializable
data class Cart(
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val itemCount: Int = 0
)

/**
 * Cart item model
 */
@Serializable
data class CartItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val totalPrice: Double
)

/**
 * Order model
 */
@Serializable
data class Order(
    val id: String,
    val orderNumber: String,
    val date: String,
    val status: OrderStatus,
    val totalPrice: Double,
    val subTotal: Double = 0.0,
    val deliveryAddress: String? = null,
    val deliveryTime: String? = null,
    val isEditable: Boolean = false,
    val isCancellable: Boolean = false,
    val items: List<OrderItem> = emptyList()
)

/**
 * Order item model
 */
@Serializable
data class OrderItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double
)

/**
 * Detailed order with line items, returned by GetOrderHistory/{orderId}
 */
@Serializable
data class OrderDetail(
    val id: String,
    val orderNumber: String,
    val orderDate: String,
    val status: OrderStatus,
    val total: Double,
    val subTotal: Double,
    val shippingPrice: Double,
    val depositPrice: Double,
    val packagingPrice: Double,
    val couponDiscount: Double,
    val totalProductDiscount: Double,
    val numberOfProducts: Int,
    val deliveryDate: String? = null,
    val deliveryTimeStart: String? = null,
    val deliveryTimeEnd: String? = null,
    val isEditable: Boolean = false,
    val isCancellable: Boolean = false,
    val hasInvoice: Boolean = false,
    val notes: String? = null,
    val lines: List<OrderLine> = emptyList(),
    val couponLines: List<CouponLine> = emptyList()
)

/**
 * A single product line within an order
 */
@Serializable
data class OrderLine(
    val productNumber: String,
    val productName: String,
    val groupName: String,
    val quantity: Int,
    val description: String? = null,
    val averageItemPrice: Double,
    val amount: Double,
    val discountAmount: Double = 0.0,
    val isProductLine: Boolean = true,
    val isDepositLine: Boolean = false,
    val campaignName: String? = null,
    val soldOut: Int = 0
)

/**
 * Coupon line within an order
 */
@Serializable
data class CouponLine(
    val type: String,
    val name: String,
    val couponNumber: String
)

/**
 * Order status enum
 */
@Serializable
enum class OrderStatus(val code: Int) {
    PENDING(0),
    CONFIRMED(1),
    PROCESSING(2),
    DELIVERED(3),
    CANCELLED(4);

    companion object {
        fun fromCode(code: Int): OrderStatus = entries.find { it.code == code } ?: PENDING
    }
}

/**
 * Search result model
 */
@Serializable
data class SearchResult(
    val query: String,
    val products: List<Product>,
    val totalResults: Int,
    val page: Int = 1,
    val pageSize: Int = 20
)

/**
 * API error response
 */
@Serializable
data class ApiError(
    val message: String,
    val code: String? = null,
    val details: String? = null
)
