package com.expensemate.app.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowType {
    COMPACT,
    MEDIUM,
    EXPANDED
}

data class WindowSizeInfo(
    val widthType: WindowType,
    val heightType: WindowType,
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,
    val isLandscape: Boolean
)

@Composable
fun rememberWindowSizeInfo(): WindowSizeInfo {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    val widthType = when {
        screenWidth < 600.dp -> WindowType.COMPACT
        screenWidth < 840.dp -> WindowType.MEDIUM
        else -> WindowType.EXPANDED
    }

    val heightType = when {
        screenHeight < 480.dp -> WindowType.COMPACT
        screenHeight < 900.dp -> WindowType.MEDIUM
        else -> WindowType.EXPANDED
    }

    return WindowSizeInfo(
        widthType = widthType,
        heightType = heightType,
        screenWidthDp = screenWidth,
        screenHeightDp = screenHeight,
        isLandscape = isLandscape
    )
}
