// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

// Grouped settings style: rounded cards on a grey page, small uppercase section headers,
// inset dividers between rows, colored icon tiles and rounded switches.

val GroupShape = RoundedCornerShape(12.dp)
private val groupHorizontalPadding = 16.dp

/** Card color of settings groups. */
val groupColor @Composable get() = MaterialTheme.colorScheme.surfaceContainerLowest

/** Shape for an item at [index] of [count] items that are drawn as one group, e.g. in a lazy list. */
fun groupItemShape(index: Int, count: Int): Shape {
    val top = if (index == 0) 12.dp else 0.dp
    val bottom = if (index == count - 1) 12.dp else 0.dp
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

/** Modifier for an item at [index] of [count] items drawn as one group. */
@Composable
fun Modifier.groupItem(index: Int, count: Int): Modifier =
    padding(horizontal = groupHorizontalPadding)
        .clip(groupItemShape(index, count))
        .background(groupColor)

@Composable
fun GroupHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        modifier = modifier.padding(start = groupHorizontalPadding + 16.dp, end = groupHorizontalPadding + 16.dp, top = 22.dp, bottom = 7.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun GroupFooter(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(start = groupHorizontalPadding + 16.dp, end = groupHorizontalPadding + 16.dp, top = 7.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Inset divider between rows of a group. [inset] should match the start of the row text. */
@Composable
fun GroupDivider(inset: Int = 16) {
    HorizontalDivider(
        modifier = Modifier.padding(start = inset.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline
    )
}

/** A group of settings rows: optional header, rounded card, optional footer. */
@Composable
fun SettingsGroup(
    title: String? = null,
    footer: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        if (title != null) GroupHeader(title)
        else Spacer(Modifier.height(22.dp))
        Column(
            Modifier
                .padding(horizontal = groupHorizontalPadding)
                .fillMaxWidth()
                .clip(GroupShape)
                .background(groupColor)
                .animateContentSize(),
            content = content
        )
        if (footer != null) GroupFooter(footer)
    }
}

/** Draws [items] in a group, with dividers between them. */
@Composable
fun <T> SettingsGroup(
    items: List<T>,
    title: String? = null,
    footer: String? = null,
    dividerInset: Int = 16,
    itemContent: @Composable (T) -> Unit,
) {
    if (items.isEmpty()) return
    SettingsGroup(title, footer) {
        items.forEachIndexed { i, item ->
            if (i > 0) GroupDivider(dividerInset)
            itemContent(item)
        }
    }
}

/** White icon on a colored rounded square. */
@Composable
fun IconTile(@DrawableRes icon: Int, color: Color, contentDescription: String? = null) {
    Box(
        Modifier.size(30.dp).clip(RoundedCornerShape(7.dp)).background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(icon), contentDescription, Modifier.size(20.dp), tint = Color.White)
    }
}

/** Switch with a round thumb of constant size, and green track when checked. */
@Composable
fun SettingsSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        // non-null thumb content makes the unchecked thumb as large as the checked one
        thumbContent = { Spacer(Modifier.size(SwitchDefaults.IconSize)) },
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = colors.tertiary,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = colors.surfaceContainerHighest,
            uncheckedBorderColor = Color.Transparent,
            disabledCheckedThumbColor = Color.White,
            disabledCheckedTrackColor = colors.tertiary.copy(alpha = 0.4f),
            disabledCheckedBorderColor = Color.Transparent,
            disabledUncheckedThumbColor = Color.White.copy(alpha = 0.7f),
            disabledUncheckedTrackColor = colors.surfaceContainerHighest.copy(alpha = 0.5f),
            disabledUncheckedBorderColor = Color.Transparent,
        )
    )
}
