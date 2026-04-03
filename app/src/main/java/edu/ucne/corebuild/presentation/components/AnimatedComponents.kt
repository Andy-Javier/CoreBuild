package edu.ucne.corebuild.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedListItem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 2 },
        modifier = modifier
    ) {
        content()
    }
}

fun Modifier.bounceClick() = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    this.scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    try {
                        awaitRelease()
                    } finally {
                        pressed = false
                    }
                }
            )
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        leadingIcon = leadingIcon,
        shape = CircleShape
    )
}
