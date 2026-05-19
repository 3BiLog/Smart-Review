package com.example.smartreview.ui.screens.search.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartreview.ui.theme.*

@Composable
internal fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onFilterClick: () -> Unit,
    focusRequester: FocusRequester,
    onSearch: () -> Unit,
) {
    Surface(
        color = GlassBg,
        tonalElevation = 0.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurfaceVariant)
            }

            Surface(
                color = SurfaceContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .border(1.dp, GlassBorder, RoundedCornerShape(14.dp)),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        cursorBrush = SolidColor(Primary),
                        textStyle = TextStyle(
                            color = OnSurface,
                            fontSize = 15.sp,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                        decorationBox = { innerField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (query.isEmpty()) {
                                    Text(
                                        "Tìm kiếm khóa học, kỹ năng...",
                                        color = OnSurfaceVariant,
                                        fontSize = 15.sp,
                                    )
                                }
                                innerField()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                    )

                    AnimatedVisibility(
                        visible = query.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                    ) {
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier.size(20.dp),
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = "Clear",
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(4.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceContainer)
                    .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                    .clickable(onClick = onFilterClick),
            ) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = "Advanced Filters",
                    tint = Primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
