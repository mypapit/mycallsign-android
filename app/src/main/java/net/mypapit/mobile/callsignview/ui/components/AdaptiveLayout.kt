package net.mypapit.mobile.callsignview.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.mypapit.mobile.callsignview.ui.theme.AppMotion
import net.mypapit.mobile.callsignview.ui.theme.OrangeAccent
import net.mypapit.mobile.callsignview.ui.theme.OrangeContainer

enum class WindowLayoutType {
    COMPACT,
    MEDIUM,
    EXPANDED;

    val useNavigationRail: Boolean
        get() = this != COMPACT

    val isTwoPane: Boolean
        get() = this == EXPANDED
}

fun classifyWindowWidth(widthDp: Int): WindowLayoutType {
    return when {
        widthDp < 600 -> WindowLayoutType.COMPACT
        widthDp < 720 -> WindowLayoutType.MEDIUM
        else -> WindowLayoutType.EXPANDED
    }
}

@Composable
fun MasterDetailPane(
    isTwoPane: Boolean,
    selectedCallsign: String?,
    masterContent: @Composable (Modifier) -> Unit,
    detailContent: @Composable (String, Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isTwoPane) {
        masterContent(modifier.fillMaxSize())
        return
    }

    Row(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(0.44f)
                .fillMaxHeight()
        ) {
            masterContent(Modifier.fillMaxSize())
        }

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Box(
            modifier = Modifier
                .weight(0.56f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            AnimatedContent(
                targetState = selectedCallsign,
                transitionSpec = {
                    (fadeIn() + slideInHorizontally(
                        animationSpec = AppMotion.ExpressiveIntOffsetSpring,
                        initialOffsetX = { it / 16 }
                    )) togetherWith fadeOut()
                },
                label = "tabletDetailPane"
            ) { targetCallsign ->
                if (targetCallsign != null) {
                    detailContent(targetCallsign, Modifier.fillMaxSize())
                } else {
                    EmptyDetailPlaceholder(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun EmptyDetailPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 420.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = OrangeContainer,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Radio,
                            contentDescription = null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Text(
                    text = "Select a Callsign",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Choose any callsign from the directory on the left to inspect license class, holder details, expiry status, and quick actions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
