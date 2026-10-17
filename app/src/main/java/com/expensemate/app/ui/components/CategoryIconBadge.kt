package com.expensemate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CategoryIconBadge(
    iconName: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFF2A3B90)
    }

    val iconVector = getIconForName(iconName)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(categoryColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = categoryColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

fun getIconForName(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "restaurant", "food" -> Icons.Default.Restaurant
        "shopping_cart", "groceries" -> Icons.Default.ShoppingCart
        "directions_car", "transport" -> Icons.Default.DirectionsCar
        "local_gas_station", "fuel" -> Icons.Default.LocalGasStation
        "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
        "home", "housing" -> Icons.Default.Home
        "bolt", "utilities" -> Icons.Default.Bolt
        "wifi", "internet" -> Icons.Default.Wifi
        "local_hospital", "health" -> Icons.Default.LocalHospital
        "school", "education" -> Icons.Default.School
        "movie", "entertainment" -> Icons.Default.Movie
        "flight", "travel" -> Icons.Default.Flight
        "subscriptions" -> Icons.Default.Subscriptions
        "spa", "personal" -> Icons.Default.Spa
        "payments", "salary" -> Icons.Default.Payments
        "laptop_mac", "freelance" -> Icons.Default.LaptopMac
        "storefront", "business" -> Icons.Default.Storefront
        "trending_up", "investments" -> Icons.Default.TrendingUp
        "account_balance", "interest" -> Icons.Default.AccountBalance
        "card_giftcard", "gifts" -> Icons.Default.CardGiftcard
        "savings" -> Icons.Default.Savings
        "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.Category
    }
}
