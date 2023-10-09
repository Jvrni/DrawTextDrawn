package com.dragtextdrawn

import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.view.WindowCompat
import com.dragtextdrawn.ui.theme.DragTextDrawnTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DragTextDrawnTheme {
                val activity = LocalContext.current as ComponentActivity

                WindowCompat.setDecorFitsSystemWindows(activity.window, false)

                activity.window.setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                )

                Content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content() {
    val value = action.collectAsState().value

    val visibleInput = remember { mutableStateOf(false) }
    val isRotate = remember { mutableStateOf(false) }

    val text = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (add, rotate, input) = createRefs()

        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.background),
            contentDescription = "",
            contentScale = ContentScale.FillBounds
        )

        repeat(value.list.size) {
            DrawText(value.list[it], isRotate.value)
        }

        if (visibleInput.value)
            TextField(
                modifier = Modifier
                    .constrainAs(input) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    textColor = Color.Black,
                    cursorColor = Color.Black,
                ),
                value = text.value,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        _action.value =
                            NewText(_action.value.list.toMutableList().apply { add(text.value) })

                        visibleInput.value = false
                        text.value = ""
                    }
                ),
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                ),
                onValueChange = {
                    text.value = it
                }
            )

        Card(
            modifier = Modifier
                .constrainAs(add) {
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
                .padding(vertical = 30.dp)
                .padding(horizontal = 10.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            onClick = {
                visibleInput.value = true

                coroutineScope.launch {
                    delay(300)
                    focusRequester.requestFocus()
                }
            }
        ) {
            Icon(
                modifier = Modifier
                    .size(60.dp)
                    .padding(18.dp),
                painter = painterResource(id = R.drawable.ic_text),
                tint = Color.Black,
                contentDescription = ""
            )
        }

        Card(
            modifier = Modifier
                .constrainAs(rotate) {
                    bottom.linkTo(add.top)
                    end.linkTo(parent.end)
                }
                .padding(horizontal = 10.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            onClick = { isRotate.value = !isRotate.value }
        ) {
            Icon(
                modifier = Modifier
                    .size(60.dp)
                    .padding(18.dp),
                painter = painterResource(id = R.drawable.ic_rotate),
                tint = if (isRotate.value) Color.Green else Color.Black,
                contentDescription = ""
            )
        }
    }

}

@OptIn(ExperimentalTextApi::class, ExperimentalComposeUiApi::class)
@Composable
fun DrawText(text: String, isRotate: Boolean) {
    val textMeasure = rememberTextMeasurer()
    val textLayoutResult: TextLayoutResult =
        textMeasure.measure(
            text = AnnotatedString(text),
            style = TextStyle(fontSize = 35.sp)
        )

    var viewRotation = remember { 0.0 }
    var fingerRotation = remember { 0.0 }
    var rotation by remember { mutableStateOf(0.0) }

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val screenWidth = with(LocalDensity.current) {
        (configuration.screenWidthDp.dp.toPx() / 2) - (textLayoutResult.size.width / 2)
    }

    val screenHeight = with(LocalDensity.current) {
        (configuration.screenHeightDp.dp.toPx() / 2) - textLayoutResult.size.height
    }

    var offsetX by remember { mutableStateOf(screenWidth) }
    var offsetY by remember { mutableStateOf(screenHeight) }

    Canvas(modifier = Modifier
        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        .pointerInput(isRotate) {
            if (!isRotate)
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
        }
        .pointerInteropFilter { event ->
            if (isRotate) {
                val x: Float = event.x
                val y: Float = event.y
                val xc: Float = with(density) { configuration.screenWidthDp.dp.toPx() } / 2f
                val yc: Float = with(density) { configuration.screenHeightDp.dp.toPx() } / 2f

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        viewRotation = rotation
                        fingerRotation =
                            Math.toDegrees(atan2((x - xc).toDouble(), (yc - y).toDouble()))
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val newFingerRotation =
                            Math.toDegrees(atan2((x - xc).toDouble(), (yc - y).toDouble()))
                        rotation = (viewRotation + newFingerRotation - fingerRotation)
                    }

                    MotionEvent.ACTION_UP -> {
                        fingerRotation = 0.0
                    }
                }
            }
            true
        }
        .graphicsLayer(
            rotationZ = rotation.toFloat()
        )
        .fillMaxWidth()
        .height(100.dp)
        .border(1.dp, Color.Black), //TODO BORDER TO TEST
        onDraw = {
            drawIntoCanvas {
                val path = Path().apply {
                    cubicTo(100f, 800f, 900f, 100f, 700f, 900f)
                }
                it.nativeCanvas.drawTextOnPath(
                    text,
                    path,
                    0f,
                    0f,
                    Paint().apply {
                        textSize = 35.sp.toPx()
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        })
}


data class NewText(
    val list: List<String> = emptyList()
)

val _action = MutableStateFlow(NewText())
val action: StateFlow<NewText> = _action
