package com.example.checkin

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
    val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return ctx.packageManager.queryIntentActivities(i, 0)
        .map { AppInfo(it.loadLabel(ctx.packageManager).toString(), it.activityInfo.packageName) }
        .filter { it.pkg != ctx.packageName }.distinctBy { it.pkg }.sortedBy { it.label.lowercase() }
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

@Composable fun SelectAppsScreen(apps: List<AppInfo>, selected: Set<String>, onToggle: (String) -> Unit, onNext: () -> Unit) =
    Page("Choose apps", "Which apps should trigger check-ins?", "Next", selected.isNotEmpty(), onNext) {
        LazyColumn {
            items(apps) { a ->
                Row(Modifier.fillMaxWidth().clickable { onToggle(a.pkg) }.padding(vertical = 4.dp)) {
                    Checkbox(a.pkg in selected, { onToggle(a.pkg) }); Text(a.label, Modifier.padding(top = 12.dp))
                }
            }
        }
    }

@Composable fun FrequencyScreen(minutes: Int, onChange: (Int) -> Unit, onNext: () -> Unit) =
    Page("Check-in frequency", "Check in after this much time in your selected apps.", "Next", onCta = onNext) {
        Text("$minutes minutes", style = MaterialTheme.typography.titleLarge)
        Slider(minutes.toFloat(), { onChange((it / 5).toInt().coerceAtLeast(1) * 5) }, valueRange = 5f..120f)
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
    val valid = if (q.type == QType.OPEN) current.trim().length >= 10 else current.isNotBlank()
    val last = idx == questions.lastIndex
    Page(title, "Question ${idx + 1} of ${questions.size}", if (last) "Finish" else "Next", valid, {
        answers.add(QA(q.text, current.trim()))
        if (last) onDone(answers.toList()) else idx++
    }) {
        Text(q.text, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        if (q.type == QType.OPEN) OutlinedTextField(current, { current = it }, Modifier.fillMaxWidth(), minLines = 4)
        else q.options.forEach { o ->
            Row(Modifier.fillMaxWidth().clickable { current = o }) { RadioButton(current == o, { current = o }); Text(o, Modifier.padding(top = 12.dp)) }
        }
    }
}

@Composable fun HomeScreen(apps: List<String>, minutes: Int, count: Int) =
    Page("CheckIn is active", "Watching: ${apps.joinToString()}\nCheck-in every $minutes min\nReflections so far: $count",
        "OK", onCta = {})
