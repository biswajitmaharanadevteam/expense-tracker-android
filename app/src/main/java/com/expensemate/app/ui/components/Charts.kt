package com.expensemate.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensemate.app.domain.model.CategorySpending
import com.expensemate.app.domain.model.CurrencyConfig
import com.expensemate.app.domain.model.DailySpending
import com.expensemate.app.domain.model.MonthlyComparison
import com.expensemate.app.ui.theme.ExpenseRed
import com.expensemate.app.ui.theme.IncomeGreen
import com.expensemate.app.ui.theme.IndigoPrimary
import com.expensemate.app.util.CurrencyFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
    categories: List<CategorySpending>,
    totalExpense: Double,
    currency: CurrencyConfig,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spending by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = CurrencyFormatter.format(totalExpense, currency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (categories.isEmpty() || totalExpense <= 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses recorded for this period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val animationProgress = remember { Animatable(0f) }
                LaunchedEffect(categories) {
                    animationProgress.snapTo(0f)
                    animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
                }

                // Donut Chart Canvas with Center Text
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(190.dp)
                ) {
                    Canvas(modifier = Modifier.size(170.dp)) {
                        val strokeWidth = 28.dp.toPx()
                        val arcSize = size.width - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        var currentAngle = -90f

                        for (item in categories) {
                            val sweep = (item.percentage / 100f) * 360f * animationProgress.value
                            val itemColor = try {
                                Color(android.graphics.Color.parseColor(item.colorHex))
                            } catch (e: Exception) {
                                IndigoPrimary
                            }

                            drawArc(
                                color = itemColor,
                                startAngle = currentAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            currentAngle += sweep
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Spent",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatCompact(totalExpense, currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Legend items
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (item in categories.take(6)) {
                        val itemColor = try {
                            Color(android.graphics.Color.parseColor(item.colorHex))
                        } catch (e: Exception) {
                            IndigoPrimary
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(itemColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${item.categoryName} (${String.format(java.util.Locale.US, "%.0f", item.percentage)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IncomeExpenseBarChart(
    data: List<MonthlyComparison>,
    currency: CurrencyConfig,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Income vs Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Small legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IncomeGreen))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Income", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Expense", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (data.isEmpty() || data.all { it.incomeAmount == 0.0 && it.expenseAmount == 0.0 }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No financial data available for this timeline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxVal = (data.maxOfOrNull { maxOf(it.incomeAmount, it.expenseAmount) } ?: 1.0).coerceAtLeast(1.0)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    for (item in data) {
                        val incomeHeightRatio = (item.incomeAmount / maxVal).toFloat().coerceIn(0f, 1f)
                        val expenseHeightRatio = (item.expenseAmount / maxVal).toFloat().coerceIn(0f, 1f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Bars row
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.height(140.dp)
                            ) {
                                // Income bar
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .height((130 * incomeHeightRatio).coerceAtLeast(4f).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(IncomeGreen)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                // Expense bar
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .height((130 * expenseHeightRatio).coerceAtLeast(4f).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(ExpenseRed)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.monthLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpendingTrendLineChart(
    trendData: List<DailySpending>,
    currency: CurrencyConfig,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Daily Spending Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (trendData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No daily spending recorded for this period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxExpense = (trendData.maxOfOrNull { it.expenseAmount } ?: 1.0).coerceAtLeast(1.0)
                val primaryColor = MaterialTheme.colorScheme.primary

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val spacing = if (trendData.size > 1) width / (trendData.size - 1) else width

                    val points = trendData.mapIndexed { index, item ->
                        val x = index * spacing
                        val y = height - (item.expenseAmount / maxExpense * (height - 30f)).toFloat()
                        Offset(x, y)
                    }

                    if (points.size > 1) {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    for (p in points) {
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = p
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = trendData.firstOrNull()?.label ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trendData.lastOrNull()?.label ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
