package com.financeapp.calculator.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.financeapp.calculator.data.model.YearPoint
import com.financeapp.calculator.ui.theme.ChartInvested
import com.financeapp.calculator.ui.theme.ChartReturns
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

/**
 * Year-over-year growth chart.
 *
 * Renders two lines: cumulative amount invested, and the projected balance.
 */
@Composable
fun GrowthChart(
    series: List<YearPoint>,
    modifier: Modifier = Modifier
) {
    val investedColor = ChartInvested.toArgb()
    val valueColor = ChartReturns.toArgb()
    val ctx = LocalContext.current
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        factory = {
            LineChart(ctx).apply {
                description.isEnabled = false
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                setTouchEnabled(true)
                setPinchZoom(true)
            }
        },
        update = { chart ->
            val investedEntries = series.map { Entry(it.year.toFloat(), it.invested.toFloat()) }
            val valueEntries = series.map { Entry(it.year.toFloat(), it.value.toFloat()) }
            val investedSet = LineDataSet(investedEntries, "Invested").apply {
                color = investedColor
                setCircleColor(investedColor)
                lineWidth = 2f
                setDrawValues(false)
            }
            val valueSet = LineDataSet(valueEntries, "Value").apply {
                color = valueColor
                setCircleColor(valueColor)
                lineWidth = 2f
                setDrawValues(false)
            }
            chart.data = LineData(investedSet, valueSet)
            chart.invalidate()
        }
    )
}
