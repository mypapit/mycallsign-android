package net.mypapit.mobile.callsignview.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.mypapit.mobile.callsignview.data.local.CallsignStats
import net.mypapit.mobile.callsignview.data.repository.CallsignRepository
import net.mypapit.mobile.callsignview.ui.components.CompactTopAppBarHeight
import net.mypapit.mobile.callsignview.ui.theme.AmberWarning
import net.mypapit.mobile.callsignview.ui.theme.AppMotion
import net.mypapit.mobile.callsignview.ui.theme.ChartCoolSlate
import net.mypapit.mobile.callsignview.ui.theme.ChartIndigo
import net.mypapit.mobile.callsignview.ui.theme.ChartLightOrange
import net.mypapit.mobile.callsignview.ui.theme.ChartSkyBlue
import net.mypapit.mobile.callsignview.ui.theme.ChartViolet
import net.mypapit.mobile.callsignview.ui.theme.OrangeAccent
import net.mypapit.mobile.callsignview.ui.theme.RadioCyan
import net.mypapit.mobile.callsignview.ui.theme.SignalRed
import java.text.NumberFormat
import java.util.Locale

private data class PrefixSlice(
    val label: String,
    val count: Int,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    repository: CallsignRepository,
    onOpenExpiredOver5Years: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by repository.observeStats()
        .collectAsStateWithLifecycle(initialValue = CallsignStats())
    val nf = remember { NumberFormat.getIntegerInstance(Locale.getDefault()) }

    var chartStarted by remember { mutableStateOf(false) }
    LaunchedEffect(stats.total) {
        if (stats.total > 0) chartStarted = true
    }
    val sweepProgress by animateFloatAsState(
        targetValue = if (chartStarted) 1f else 0f,
        animationSpec = AppMotion.ChartSweepSpring,
        label = "donutSweep"
    )
    var selectedSliceIndex by remember { mutableIntStateOf(-1) }

    val slices = remember(stats) {
        listOf(
            PrefixSlice("9M (9M2/4/6/8)", stats.count9M, OrangeAccent),
            PrefixSlice("9W2", stats.count9W2, RadioCyan),
            PrefixSlice("9W6", stats.count9W6, ChartIndigo),
            PrefixSlice("9W8", stats.count9W8, ChartLightOrange),
            PrefixSlice("9W3 (Class C)", stats.count9W3, ChartViolet),
            PrefixSlice("9W4 (Gateway)", stats.count9W4, ChartSkyBlue),
            PrefixSlice("Others", stats.countOthers, ChartCoolSlate)
        ).filter { it.count > 0 }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                expandedHeight = CompactTopAppBarHeight,
                title = {
                    Column {
                        Text(
                            text = "Statistics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "100% offline directory metrics",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 880.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatKpiCard(
                    title = "Total Listed",
                    value = nf.format(stats.total),
                    accent = OrangeAccent,
                    modifier = Modifier.weight(1f)
                )
                StatKpiCard(
                    title = "Active",
                    value = nf.format(stats.active),
                    accent = RadioCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatKpiCard(
                    title = "Expired Today",
                    value = nf.format(stats.expired),
                    accent = AmberWarning,
                    modifier = Modifier.weight(1f)
                )
                StatKpiCard(
                    title = "Expiring ≤ 90d",
                    value = nf.format(stats.expiringIn90Days),
                    accent = ChartIndigo,
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Callsign Class Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (stats.total > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(176.dp)) {
                                val strokeWidth = 28.dp.toPx()
                                val diameter = size.minDimension - strokeWidth
                                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                                val arcSize = Size(diameter, diameter)

                                var startAngle = -90f
                                slices.forEachIndexed { idx, slice ->
                                    val fullSweep = (slice.count.toFloat() / stats.total.toFloat()) * 360f
                                    val sweep = (fullSweep * sweepProgress - 2.5f).coerceAtLeast(1f)
                                    val extra = if (selectedSliceIndex == idx) 6.dp.toPx() else 0f
                                    drawArc(
                                        color = slice.color,
                                        startAngle = startAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth + extra, cap = StrokeCap.Butt)
                                    )
                                    startAngle += fullSweep
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val focused = slices.getOrNull(selectedSliceIndex)
                                Text(
                                    text = focused?.let { nf.format(it.count) } ?: nf.format(stats.total),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = focused?.label ?: "Callsigns",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            slices.forEachIndexed { idx, slice ->
                                val pct = (slice.count * 100.0) / stats.total.toDouble()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable {
                                            selectedSliceIndex = if (selectedSliceIndex == idx) -1 else idx
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(slice.color)
                                    )
                                    Text(
                                        text = slice.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "%s (%.1f%%)", nf.format(slice.count), pct),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Waiting for callsign database...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Card(
                onClick = onOpenExpiredOver5Years,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = BorderStroke(1.dp, SignalRed.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = SignalRed,
                        modifier = Modifier.size(26.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Expired Callsigns (>5 Years)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${nf.format(stats.expiredOver5Years)} callsigns expired over 5 years ago",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
        }
    }
}

@Composable
private fun StatKpiCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}
