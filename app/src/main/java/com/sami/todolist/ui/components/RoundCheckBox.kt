package com.sami.todolist.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RoundCheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    checkedColor: Color = Color(0xFF10B981),
    uncheckedBorderColor: Color = Color(0xFF64748B)
) {
    val scale by animateFloatAsState(
        targetValue = if (checked) 1f else 0.2f,
        animationSpec = tween(durationMillis = 200),
        label = "checkScale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(
                width = if (checked) 0.dp else 2.dp,
                color = if (checked) Color.Transparent else uncheckedBorderColor,
                shape = CircleShape
            )
            .background(if (checked) checkedColor else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(size * 0.65f)
                    .scale(scale)
            )
        }
    }
}
