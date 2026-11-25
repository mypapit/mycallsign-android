package net.mypapit.mobile.callsignview.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

object AppMotion {
    val StandardSpatialSpring = spring<Float>(
        dampingRatio = 0.85f,
        stiffness = 420f
    )

    val ExpressiveSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val ExpressiveDpSpring = spring<Dp>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val ExpressiveIntOffsetSpring = spring<IntOffset>(
        dampingRatio = 0.82f,
        stiffness = 380f
    )

    val ChartSweepSpring = spring<Float>(
        dampingRatio = 0.88f,
        stiffness = 220f
    )
}
