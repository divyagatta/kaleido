package com.example.checkin

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val poppinsFontName = GoogleFont("Poppins")

val PoppinsFontFamily = FontFamily(
    Font(googleFont = poppinsFontName, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = poppinsFontName, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = poppinsFontName, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = poppinsFontName, fontProvider = googleFontProvider, weight = FontWeight.Bold)
)

data class AppInfo(val label: String, val pkg: String)

fun launchableApps(ctx: Context): List<AppInfo> {
    val pm = ctx.packageManager

    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val launcherApps = pm.queryIntentActivities(mainIntent, 0).mapNotNull { resolveInfo ->
        val pkg = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
        val label = resolveInfo.loadLabel(pm).toString()
        AppInfo(if (label.isNotBlank()) label else pkg, pkg)
    }

    val installedApps = pm.getInstalledApplications(0).mapNotNull { appInfo ->
        val pkg = appInfo.packageName
        val launchIntent = pm.getLaunchIntentForPackage(pkg)
        if (launchIntent != null) {
            val label = appInfo.loadLabel(pm).toString()
            AppInfo(if (label.isNotBlank()) label else pkg, pkg)
        } else null
    }

    return (launcherApps + installedApps)
        .filter { it.pkg != ctx.packageName }
        .distinctBy { it.pkg }
        .sortedBy { it.label.lowercase() }
}

@Composable
fun Page(title: String, body: String? = null, cta: String, ctaEnabled: Boolean = true, onCta: () -> Unit,
         content: @Composable ColumnScope.() -> Unit = {}) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        body?.let { Spacer(Modifier.height(8.dp)); Text(it) }
        Spacer(Modifier.height(16.dp))
        Column(Modifier.weight(1f)) { content() }
        Button(onCta, enabled = ctaEnabled, modifier = Modifier.fillMaxWidth()) { Text(cta) }
    }
}

@Composable
fun HowItWorksScreen(onNext: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FBF7)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How kaleido works",
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = Color(0xFF688E61),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0x21688E61),
                        spotColor = Color(0x21688E61)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFEFFFE7))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Kaleido encourages you to be consciously aware of the short-form content you are consuming on a daily basis by making reflection non-negotiable. At a frequency of your choosing, you will be asked to fill out a survey that asks you about the time you’ve spent on the apps you’re trying to limit. Every time, the questions on the survey will change, building on your previous responses to keep you engaged and aware of your larger patterns. The goal here is not to get you to stop consuming short-form content entirely, but to help you realize what it is you’re actually consuming. From there, it’s your decision.",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = Color(0xFF688E61).copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = onNext,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF688E61),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 4.dp
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = "Got it!",
                                style = TextStyle(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 22.sp,
                                    lineHeight = 28.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun HowItWorksScreenPreview() {
    MaterialTheme {
        HowItWorksScreen(onNext = {})
    }
}

@Composable
fun PermissionsScreen(
    usageGranted: Boolean,
    overlayGranted: Boolean,
    onUsage: () -> Unit,
    onOverlay: () -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FBF7)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section: Progress & Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 35.dp)
                    .padding(top = 26.dp),
                horizontalAlignment = Alignment.Start
            ) {
                LinearProgressIndicator(
                    progress = { 0.33f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF688E61),
                    trackColor = Color(0xFFEFFFE7),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Setup 1 of 3",
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF688E61)
                    )
                )
            }

            Spacer(modifier = Modifier.height(38.dp))

            // Headline Text
            Text(
                text = "Allow kaleido to access the following permissions:",
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = Color(0xFF688E61),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .width(250.dp)
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(35.dp))

            // White Card Sheet
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        ambientColor = Color(0x21688E61),
                        spotColor = Color(0x21688E61)
                    ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFEFFFE7))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Permission Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(35.dp)
                    ) {
                        Surface(
                            onClick = onUsage,
                            enabled = !usageGranted,
                            shape = CircleShape,
                            color = Color(0xFFEFFFE7),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .width(232.dp)
                                .height(40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = if (usageGranted) "screen time tracking ✓" else "screen time tracking >",
                                    style = TextStyle(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp,
                                        color = Color(0xFF688E61),
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }

                        Surface(
                            onClick = onOverlay,
                            enabled = !overlayGranted,
                            shape = CircleShape,
                            color = Color(0xFFEFFFE7),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .width(232.dp)
                                .height(40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = if (overlayGranted) "display over other apps ✓" else "display over other apps >",
                                    style = TextStyle(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp,
                                        color = Color(0xFF688E61),
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = onNext,
                        enabled = usageGranted && overlayGranted,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF688E61),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF688E61).copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 4.dp
                        ),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                        modifier = Modifier.widthIn(min = 134.dp)
                    ) {
                        Text(
                            text = "Next",
                            style = TextStyle(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 22.sp,
                                lineHeight = 28.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PermissionsScreenPreview() {
    MaterialTheme {
        PermissionsScreen(
            usageGranted = false,
            overlayGranted = true,
            onUsage = {},
            onOverlay = {},
            onNext = {}
        )
    }
}

@Composable
private fun AppIconImage(pkg: String, label: String) {
    val context = LocalContext.current
    val bitmap = remember(pkg) {
        try {
            val drawable = context.packageManager.getApplicationIcon(pkg)
            val b = Bitmap.createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(b)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            b.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = label,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFEFFFE7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.take(1).uppercase(),
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF688E61)
                )
            )
        }
    }
}

fun Modifier.drawVerticalScrollbar(
    state: LazyListState,
    coroutineScope: CoroutineScope,
    color: Color = Color(0xFF688E61),
    trackColor: Color = Color(0xFFEFFFE7),
    width: Dp = 10.dp
): Modifier = this
    .pointerInput(state) {
        detectVerticalDragGestures { change, dragAmount ->
            change.consume()
            val totalItems = state.layoutInfo.totalItemsCount
            if (totalItems > 0) {
                val trackHeight = size.height.toFloat()
                val scrollRatio = dragAmount / trackHeight
                val itemScrollDelta = scrollRatio * totalItems

                coroutineScope.launch {
                    val targetIndex = (state.firstVisibleItemIndex + itemScrollDelta.toInt()).coerceIn(0, totalItems - 1)
                    state.scrollToItem(targetIndex)
                }
            }
        }
    }
    .drawWithContent {
        drawContent()

        val totalItemsCount = state.layoutInfo.totalItemsCount
        val visibleItemsInfo = state.layoutInfo.visibleItemsInfo

        if (visibleItemsInfo.isNotEmpty() && totalItemsCount > 0) {
            val firstIndex = state.firstVisibleItemIndex
            val firstOffset = state.firstVisibleItemScrollOffset
            val visibleItemsCount = visibleItemsInfo.size
            val trackHeight = size.height

            val avgItemHeight = visibleItemsInfo.sumOf { it.size }.toFloat() / visibleItemsCount.coerceAtLeast(1)
            val totalEstimatedHeight = avgItemHeight * totalItemsCount
            val currentScrollOffset = (firstIndex * avgItemHeight) + firstOffset
            val maxScrollOffset = (totalEstimatedHeight - trackHeight).coerceAtLeast(1f)

            val thumbHeight = ((visibleItemsCount.toFloat() / totalItemsCount.toFloat()) * trackHeight)
                .coerceIn(36.dp.toPx(), trackHeight)

            val scrollProgress = (currentScrollOffset / maxScrollOffset).coerceIn(0f, 1f)
            val thumbOffsetY = scrollProgress * (trackHeight - thumbHeight)

            drawRoundRect(
                color = trackColor,
                topLeft = Offset(size.width - width.toPx(), 0f),
                size = Size(width.toPx(), trackHeight),
                cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2)
            )

            drawRoundRect(
                color = color,
                topLeft = Offset(size.width - width.toPx(), thumbOffsetY),
                size = Size(width.toPx(), thumbHeight),
                cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2)
            )
        }
    }

@Composable
fun SelectAppsScreen(
    apps: List<AppInfo>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FBF7)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section: Progress & Subtitle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 35.dp)
                    .padding(top = 28.dp),
                horizontalAlignment = Alignment.Start
            ) {
                LinearProgressIndicator(
                    progress = { 2f / 3f }, // 66.6% progress for Setup 2 of 3
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF688E61),
                    trackColor = Color(0xFFEFFFE7),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Setup 2 of 3",
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF688E61)
                    )
                )
            }

            Spacer(modifier = Modifier.height(38.dp))

            // Headline Text
            Text(
                text = "Choose the apps you want to limit:",
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = Color(0xFF688E61),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .width(250.dp)
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(35.dp))

            // White Card Sheet
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        ambientColor = Color(0x21688E61),
                        spotColor = Color(0x21688E61)
                    ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFEFFFE7))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val listState = rememberLazyListState()
                    val coroutineScope = rememberCoroutineScope()

                    // List of Apps with Interactive Vertical Scrollbar
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .drawVerticalScrollbar(listState, coroutineScope)
                            .padding(end = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(apps, key = { it.pkg }) { app ->
                            val isChecked = app.pkg in selected
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggle(app.pkg) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppIconImage(pkg = app.pkg, label = app.label)

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = app.label,
                                    style = TextStyle(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1D1B20)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onToggle(app.pkg) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF688E61),
                                        uncheckedColor = Color(0xFF688E61).copy(alpha = 0.6f),
                                        checkmarkColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Next Button
                    Button(
                        onClick = onNext,
                        enabled = selected.isNotEmpty(),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF688E61),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF688E61).copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 4.dp
                        ),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                        modifier = Modifier.widthIn(min = 134.dp)
                    ) {
                        Text(
                            text = "Next",
                            style = TextStyle(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 22.sp,
                                lineHeight = 28.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun SelectAppsScreenPreview() {
    MaterialTheme {
        SelectAppsScreen(
            apps = listOf(
                AppInfo("Instagram", "com.instagram.android"),
                AppInfo("TikTok", "com.zhiliaoapp.musically"),
                AppInfo("YouTube", "com.google.android.youtube"),
                AppInfo("X / Twitter", "com.twitter.android")
            ),
            selected = emptySet(),
            onToggle = {},
            onNext = {}
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimePickerWheel(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = remember { (220 downTo 5 step 5).toList() }
    val initialIndex = remember(value, options) {
        val idx = options.indexOf(if (value in 5..220) value else 30)
        if (idx != -1) idx else options.indexOf(30).coerceAtLeast(0)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val coroutineScope = rememberCoroutineScope()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Calculate current centered/selected item
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                listState.firstVisibleItemIndex
            } else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItems.minByOrNull { item ->
                    val itemCenter = item.offset + item.size / 2
                    kotlin.math.abs(itemCenter - viewportCenter)
                }?.index ?: listState.firstVisibleItemIndex
            }
        }
    }

    // Trigger onValueChange whenever centered item changes
    LaunchedEffect(centerIndex) {
        if (centerIndex in options.indices) {
            val selectedVal = options[centerIndex]
            if (selectedVal != value) {
                onValueChange(selectedVal)
            }
        }
    }

    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionUp -> {
                            if (centerIndex > 0) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(centerIndex - 1)
                                }
                                true
                            } else false
                        }
                        Key.DirectionDown -> {
                            if (centerIndex < options.lastIndex) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(centerIndex + 1)
                                }
                                true
                            } else false
                        }
                        else -> false
                    }
                } else false
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Up Arrow Button (+5 minutes)
        IconButton(
            onClick = {
                if (centerIndex > 0) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(centerIndex - 1)
                    }
                }
            },
            enabled = centerIndex > 0,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase time",
                tint = if (centerIndex > 0) Color(0xFF688E61) else Color(0xFF688E61).copy(alpha = 0.3f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Fixed height box showing scrollable list items
        val itemHeight = 72.dp
        val gap = 16.dp
        // 3 visible items height: 72 * 3 + 16 * 2 = 248.dp
        val visibleHeight = itemHeight * 3 + gap * 2

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(visibleHeight),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(vertical = itemHeight + gap),
                verticalArrangement = Arrangement.spacedBy(gap),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(options) { index, option ->
                    val isSelected = index == centerIndex
                    val distance = kotlin.math.abs(index - centerIndex)
                    val alpha = when (distance) {
                        0 -> 1.0f
                        1 -> 0.6f
                        else -> 0.4f
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .shadow(
                                            elevation = 8.dp,
                                            shape = RoundedCornerShape(16.dp),
                                            ambientColor = Color(0x21688E61),
                                            spotColor = Color(0x21688E61)
                                        )
                                        .background(
                                            color = Color(0xFFEFFFE7),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFEFFFE7),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                } else {
                                    Modifier
                                        .background(
                                            color = Color(0xFFF8FBF7),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFEFFFE7),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                }
                            )
                            .clickable {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$option",
                                style = TextStyle(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 36.sp,
                                    color = Color(0xFF688E61).copy(alpha = if (isSelected) 1.0f else alpha),
                                    textAlign = TextAlign.Center
                                )
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "min",
                                    style = TextStyle(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 24.sp,
                                        color = Color(0xFF688E61).copy(alpha = 0.8f)
                                    ),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Down Arrow Button (-5 minutes)
        IconButton(
            onClick = {
                if (centerIndex < options.lastIndex) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(centerIndex + 1)
                    }
                }
            },
            enabled = centerIndex < options.lastIndex,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease time",
                tint = if (centerIndex < options.lastIndex) Color(0xFF688E61) else Color(0xFF688E61).copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun FrequencyScreen(
    minutes: Int,
    onChange: (Int) -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FBF7)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section: Progress & Subtitle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 35.dp)
                    .padding(top = 28.dp),
                horizontalAlignment = Alignment.Start
            ) {
                LinearProgressIndicator(
                    progress = { 1.0f }, // 100% progress for Setup 3 of 3
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF688E61),
                    trackColor = Color(0xFFEFFFE7),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Setup 3 of 3",
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF688E61)
                    )
                )
            }

            Spacer(modifier = Modifier.height(38.dp))

            // Headline Text
            Text(
                text = "Select the frequency of check-ins:",
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    color = Color(0xFF688E61),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .width(280.dp)
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(35.dp))

            // White Card Sheet
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0x21688E61),
                        spotColor = Color(0x21688E61)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFEFFFE7))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Send me check-ins every:",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 24.sp,
                            color = Color(0xFF688E61).copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    TimePickerWheel(
                        value = minutes,
                        onValueChange = onChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Next Button
            Button(
                onClick = onNext,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF688E61),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .widthIn(min = 134.dp)
            ) {
                Text(
                    text = "Next",
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun FrequencyScreenPreview() {
    var minutes by remember { mutableIntStateOf(30) }
    MaterialTheme {
        FrequencyScreen(
            minutes = minutes,
            onChange = { minutes = it },
            onNext = {}
        )
    }
}

fun introQuestions(apps: List<String>): List<Question> {
    val a = apps.take(3).joinToString(" and ")
    return listOf(
        Question("What kind of content do you notice you typically consume on $a?", QType.OPEN),
        Question("How has this content helped you? (new hobbies, inspiration, positive changes…)", QType.OPEN),
        Question("How has it hurt you? (lost time, less creating, mood…)", QType.OPEN),
        Question("How do you usually feel after a long session?", QType.CHOICE,
            listOf("Energized", "Neutral", "Drained", "Anxious or irritated")),
    )
}

/** Shared by intro survey and check-ins. */
@Composable
fun SurveyScreen(title: String, questions: List<Question>, onDone: (List<QA>) -> Unit) {
    var idx by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateListOf<QA>() }
    var current by remember(idx) { mutableStateOf("") }
    val q = questions[idx]
    val valid = if (q.type == QType.OPEN) current.trim().isNotBlank() else current.isNotBlank()
    val last = idx == questions.lastIndex
    val progress = (idx + 1).toFloat() / questions.size.coerceAtLeast(1).toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FBF7)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 360.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = Color(0xFF688E61),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF688E61),
                trackColor = Color(0xFFEFFFE7),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Question ${idx + 1} of ${questions.size}",
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFF688E61),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0x21688E61),
                        spotColor = Color(0x21688E61)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFEFFFE7))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = q.text,
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = Color(0xFF688E61).copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (q.type == QType.OPEN) {
                        OutlinedTextField(
                            value = current,
                            onValueChange = { current = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 180.dp),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = TextStyle(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = Color(0xFF688E61)
                            ),
                            placeholder = {
                                Text(
                                    text = "Type your response here…",
                                    style = TextStyle(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 15.sp,
                                        color = Color(0xFF688E61).copy(alpha = 0.5f)
                                    )
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF688E61),
                                unfocusedBorderColor = Color(0xFFEFFFE7),
                                cursorColor = Color(0xFF688E61)
                            )
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            q.options.forEach { option ->
                                val isSelected = current == option
                                Surface(
                                    onClick = { current = option },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) Color(0xFFEFFFE7) else Color.White,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF688E61) else Color(0xFFEFFFE7)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { current = option },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color(0xFF688E61),
                                                unselectedColor = Color(0xFF688E61).copy(alpha = 0.6f)
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = option,
                                            style = TextStyle(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 15.sp,
                                                color = Color(0xFF688E61)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                answers.add(QA(q.text, current.trim()))
                                if (last) onDone(answers.toList()) else idx++
                            },
                            enabled = valid,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF688E61),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF688E61).copy(alpha = 0.5f),
                                disabledContentColor = Color.White.copy(alpha = 0.7f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 4.dp
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = if (last) "Finish" else "Next",
                                style = TextStyle(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 22.sp,
                                    lineHeight = 28.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun SurveyScreenPreview() {
    MaterialTheme {
        SurveyScreen(
            title = "Intro Survey",
            questions = listOf(
                Question("What kind of content do you usually consume, and how does it make you feel?", QType.OPEN),
                Question("How do you usually feel after a long session?", QType.CHOICE, listOf("Energized", "Neutral", "Drained", "Anxious or irritated"))
            ),
            onDone = {}
        )
    }
}

@Composable fun HomeScreen(apps: List<String>, minutes: Int, count: Int) =
    Page("CheckIn is active", "Watching: ${apps.joinToString()}\nCheck-in every $minutes min\nReflections so far: $count",
        "OK", onCta = {})
