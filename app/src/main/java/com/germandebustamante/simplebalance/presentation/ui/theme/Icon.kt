package com.germandebustamante.simplebalance.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.germandebustamante.model.Category
import com.germandebustamante.simplebalance.R

@Composable
fun Category.iconPainter(): Painter {
    val resourceId = when (this) {
        Category.FOOD -> R.drawable.category_food
        Category.TRANSPORT -> R.drawable.category_transport
        Category.SHOPPING -> R.drawable.category_shopping
        Category.HEALTH -> R.drawable.category_health
        Category.LEISURE -> R.drawable.category_leisure
        Category.HOME -> R.drawable.category_home
        Category.OTHER -> R.drawable.category_other
    }
    return painterResource(id = resourceId)
}
