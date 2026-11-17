package com.yu_morph.smartbudget.ui.main

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet as FilledAccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart as FilledBarChart
import androidx.compose.material.icons.filled.Home as FilledHome
import androidx.compose.material.icons.filled.Settings as FilledSettings
import androidx.compose.material.icons.outlined.AccountBalanceWallet as OutlinedAccountBalanceWallet
import androidx.compose.material.icons.outlined.Home as OutlinedHome
import androidx.compose.material.icons.outlined.Settings as OutlinedSettings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yu_morph.smartbudget.R
import com.yu_morph.smartbudget.ui.document.MarkdownDocument
import com.yu_morph.smartbudget.ui.showAppToast
import com.yu_morph.smartbudget.ui.theme.SmartBudgetGreen
import java.time.YearMonth
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val MainNavigationInactiveColor = Color(0xFF737373)
private val MainActionGradientStart = Color(0xFF18A874)
private val MainActionGradientEnd = Color(0xFF087A55)
private const val MAIN_TAB_PRESSED_SCALE = 0.88f
private const val TAB_TRANSITION_DURATION_MILLIS = 260
private const val TAB_SLIDE_DISTANCE_DIVISOR = 5
private const val ROBOT_SPRITE_COLUMNS = 4
private const val ROBOT_SPRITE_ROWS = 3
private const val ROBOT_POSE_COUNT = ROBOT_SPRITE_COLUMNS * ROBOT_SPRITE_ROWS
private const val ROBOT_RISE_DURATION_MILLIS = 420
private const val ROBOT_RETURN_DELAY_MILLIS = 220L
private const val ROBOT_RETURN_DURATION_MILLIS = 400
private val RobotFrameBaselineOffsets =
    floatArrayOf(
        0f,
        0f,
        0f,
        0f,
        16f,
        16f,
        16f,
        16f,
        30f,
        30f,
        30f,
        30f,
    )
private val RobotGreetingSequence =
    listOf(
        RobotPoseStep(1, 260, 140),
        RobotPoseStep(2, 190, 40),
        RobotPoseStep(3, 220, 170),
        RobotPoseStep(4, 210, 60),
        RobotPoseStep(5, 190, 40),
        RobotPoseStep(6, 220, 170),
        RobotPoseStep(7, 210, 80),
        RobotPoseStep(8, 190, 130),
        RobotPoseStep(9, 220, 150),
        RobotPoseStep(10, 240, 60),
        RobotPoseStep(11, 130, 30),
        RobotPoseStep(10, 130, 30),
        RobotPoseStep(11, 130, 30),
        RobotPoseStep(10, 130, 30),
        RobotPoseStep(11, 130, 80),
        RobotPoseStep(9, 220, 160),
    )

private data class RobotPoseStep(val frame: Int, val durationMillis: Int, val pauseMillis: Long)

internal enum class MainTab(
    @param:StringRes val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME(R.string.home_tab, Icons.Filled.FilledHome, Icons.Outlined.OutlinedHome),
    BUDGET_BOOK(
        R.string.budget_book_tab,
        Icons.Filled.FilledAccountBalanceWallet,
        Icons.Outlined.OutlinedAccountBalanceWallet,
    ),
    ANALYSIS(R.string.analysis_tab, Icons.Filled.FilledBarChart, Icons.Filled.FilledBarChart),
    SETTINGS(R.string.settings_tab, Icons.Filled.FilledSettings, Icons.Outlined.OutlinedSettings),
}

@Composable
@Suppress("LongMethod")
internal fun MainScreen(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    settingsState: SettingsUiState,
    onSettingsLoad: () -> Unit,
    budgetBookState: BudgetBookOverviewUiState,
    onBudgetBookLoad: () -> Unit,
    onLogout: () -> Unit,
    onOpenDocument: (MarkdownDocument) -> Unit,
    onOpenSettings: (SettingsDetailDestination) -> Unit,
    onOpenBudgetBook: (YearMonth) -> Unit,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    val exitConfirmation = remember { ExitConfirmation() }
    BackHandler {
        if (exitConfirmation.shouldExit(SystemClock.elapsedRealtime())) {
            onExit()
        } else {
            context.showAppToast(R.string.exit_confirmation)
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            MainNavigationBar(selectedTab = selectedTab, onTabSelected = onTabSelected)
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val direction = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                    (
                        slideInHorizontally(tween(TAB_TRANSITION_DURATION_MILLIS)) {
                            it * direction / TAB_SLIDE_DISTANCE_DIVISOR
                        } +
                            fadeIn(tween(TAB_TRANSITION_DURATION_MILLIS))
                        ) togetherWith
                        (
                            slideOutHorizontally(tween(TAB_TRANSITION_DURATION_MILLIS)) {
                                -it * direction / TAB_SLIDE_DISTANCE_DIVISOR
                            } +
                                fadeOut(tween(TAB_TRANSITION_DURATION_MILLIS))
                            )
                },
                label = "메인 탭 화면 전환",
            ) { currentTab ->
                when (currentTab) {
                    MainTab.HOME -> HomeScreen(bottomPadding = innerPadding.calculateBottomPadding())

                    MainTab.BUDGET_BOOK -> BudgetBookRoute(
                        bottomPadding = innerPadding.calculateBottomPadding(),
                        state = budgetBookState,
                        onLoad = onBudgetBookLoad,
                        onOpenMonth = onOpenBudgetBook,
                    )

                    MainTab.ANALYSIS -> AnalysisScreen(
                        bottomPadding = innerPadding.calculateBottomPadding(),
                    )

                    MainTab.SETTINGS -> SettingsScreen(
                        state = settingsState,
                        bottomContentPadding = innerPadding.calculateBottomPadding(),
                        onLoad = onSettingsLoad,
                        onLogout = onLogout,
                        onOpenDocument = onOpenDocument,
                        onOpenSettings = onOpenSettings,
                    )
                }
            }
        }
    }
}

@Composable
private fun MainNavigationBar(selectedTab: MainTab, onTabSelected: (MainTab) -> Unit) {
    var greetingRunId by remember { mutableIntStateOf(0) }
    var isGreetingPlaying by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        RobotGreeting(
            greetingRunId = greetingRunId,
            onAnimationFinished = { isGreetingPlaying = false },
            modifier = Modifier.matchParentSize(),
        )
        Surface(
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 16.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(68.dp).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MainTab.entries.take(2).forEach { tab ->
                    MainNavigationTab(tab, selectedTab == tab, onTabSelected)
                }
                Spacer(modifier = Modifier.size(58.dp))
                MainTab.entries.drop(2).forEach { tab ->
                    MainNavigationTab(tab, selectedTab == tab, onTabSelected)
                }
            }
        }
        MainActionButton(
            onClick = {
                if (!isGreetingPlaying) {
                    isGreetingPlaying = true
                    greetingRunId += 1
                }
            },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun RobotGreeting(greetingRunId: Int, onAnimationFinished: () -> Unit, modifier: Modifier = Modifier) {
    val sprite = ImageBitmap.imageResource(R.drawable.robot_greeting_sprite)
    val verticalProgress = remember { Animatable(1f) }
    val poseProgress = remember { Animatable(0f) }
    val hiddenDistance = with(LocalDensity.current) { 144.dp.toPx() }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(greetingRunId) {
        if (greetingRunId == 0) return@LaunchedEffect
        try {
            isVisible = true
            verticalProgress.snapTo(1f)
            poseProgress.snapTo(0f)
            verticalProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(ROBOT_RISE_DURATION_MILLIS, easing = LinearOutSlowInEasing),
            )
            RobotGreetingSequence.forEach { step ->
                poseProgress.animateTo(
                    targetValue = step.frame.toFloat(),
                    animationSpec = tween(step.durationMillis, easing = FastOutSlowInEasing),
                )
                delay(step.pauseMillis)
            }
            delay(ROBOT_RETURN_DELAY_MILLIS)
            verticalProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(ROBOT_RETURN_DURATION_MILLIS, easing = FastOutLinearInEasing),
            )
        } finally {
            isVisible = false
            onAnimationFinished()
        }
    }

    Box(modifier = modifier) {
        if (isVisible) {
            Canvas(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .size(144.dp)
                        .offset(y = (-96).dp)
                        .graphicsLayer {
                            translationY = verticalProgress.value * hiddenDistance
                        },
            ) {
                val frameWidth = sprite.width / ROBOT_SPRITE_COLUMNS
                val frameHeight = sprite.height / ROBOT_SPRITE_ROWS
                val pose = poseProgress.value.coerceIn(0f, (ROBOT_POSE_COUNT - 1).toFloat())
                val firstFrame = floor(pose).toInt()
                val secondFrame = min(firstFrame + 1, ROBOT_POSE_COUNT - 1)
                val blend = pose - firstFrame

                fun drawFrame(frame: Int, alpha: Float) {
                    if (alpha <= 0f) return
                    val baselineOffset =
                        (RobotFrameBaselineOffsets[frame] / frameHeight * size.height).roundToInt()
                    drawImage(
                        image = sprite,
                        srcOffset =
                            IntOffset(
                                x = frame % ROBOT_SPRITE_COLUMNS * frameWidth,
                                y = frame / ROBOT_SPRITE_COLUMNS * frameHeight,
                            ),
                        srcSize = IntSize(frameWidth, frameHeight),
                        dstOffset = IntOffset(0, baselineOffset),
                        dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                        alpha = alpha,
                        filterQuality = FilterQuality.High,
                    )
                }

                drawFrame(firstFrame, 1f - blend)
                drawFrame(secondFrame, blend)
            }
        }
    }
}

@Composable
private fun MainNavigationTab(tab: MainTab, isSelected: Boolean, onTabSelected: (MainTab) -> Unit) {
    val scale = remember { Animatable(1f) }
    val animationScope = rememberCoroutineScope()
    Surface(
        onClick = {
            onTabSelected(tab)
            animationScope.launch {
                scale.animateTo(MAIN_TAB_PRESSED_SCALE, animationSpec = tween(durationMillis = 70))
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                )
            }
        },
        modifier = Modifier.size(56.dp).graphicsLayer(scaleX = scale.value, scaleY = scale.value),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        contentColor = if (isSelected) SmartBudgetGreen else MainNavigationInactiveColor,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = stringResource(tab.label),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 12.sp,
            )
        }
    }
}

@Composable
private fun MainActionButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(58.dp),
        shape = CircleShape,
        containerColor = Color.Transparent,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(listOf(MainActionGradientStart, MainActionGradientEnd)),
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.app_logo_description),
                modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = 1.1f, scaleY = 1.1f),
            )
        }
    }
}
