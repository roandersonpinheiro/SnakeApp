package com.example.snakeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                Game()
            }
        }
    }
}

@Composable
fun Game() {
    var direction by remember { mutableStateOf(Direction.Right) }
    var gameOver by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    val screenWidth = 20
    val screenHeight = 30

    var snake by remember { mutableStateOf(listOf(Position(10, 10))) }
    var food by remember { mutableStateOf(Position(15, 10)) }

    LaunchedEffect(Unit) {
        while (!gameOver) {
            delay(200)
            coroutineScope.launch {
                snake = moveSnake(snake, direction, screenWidth, screenHeight)

                if (snake.head() == food) {
                    snake = snake.grow(direction)
                    score++
                    food = generateFood(snake, screenWidth, screenHeight)
                }

                if (snake.hasCollision(screenWidth, screenHeight)) {
                    gameOver = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .aspectRatio(screenWidth.toFloat() / screenHeight)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
        ) {
            drawSnake(snake)
            drawFood(food)
        }

        if (gameOver) {
            GameOverOverlay(score)
        }
    }
}

@Composable
fun GameOverOverlay(score: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Game Over", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Score: $score", style = MaterialTheme.typography.bodySmall, color = Color.White)
        }
    }
}

enum class Direction {
    Up, Down, Left, Right
}

data class Position(val x: Int, val y: Int)

fun List<Position>.head() = first()

fun List<Position>.grow(direction: Direction): List<Position> {
    val newHead = when (direction) {
        Direction.Up -> Position(head().x, head().y - 1)
        Direction.Down -> Position(head().x, head().y + 1)
        Direction.Left -> Position(head().x - 1, head().y)
        Direction.Right -> Position(head().x + 1, head().y)
    }

    return listOf(newHead) + this
}

fun List<Position>.move(direction: Direction): List<Position> {
    val newHead = when (direction) {
        Direction.Up -> Position(head().x, head().y - 1)
        Direction.Down -> Position(head().x, head().y + 1)
        Direction.Left -> Position(head().x - 1, head().y)
        Direction.Right -> Position(head().x + 1, head().y)
    }

    return listOf(newHead) + dropLast(1)
}

fun List<Position>.hasCollision(screenWidth: Int, screenHeight: Int): Boolean {
    val head = head()
    return head.x !in 0 until screenWidth ||
            head.y !in 0 until screenHeight ||
            drop(1).contains(head)
}

fun moveSnake(snake: List<Position>, direction: Direction, screenWidth: Int, screenHeight: Int): List<Position> {
    return snake.move(direction).let { newSnake ->
        if (newSnake.hasCollision(screenWidth, screenHeight)) {
            snake
        } else {
            newSnake
        }
    }
}

fun generateFood(snake: List<Position>, screenWidth: Int, screenHeight: Int): Position {
    val availablePositions = (0 until screenWidth).flatMap { x ->
        (0 until screenHeight).map { y ->
            Position(x, y)
        }
    }

    return availablePositions.filterNot { snake.contains(it) }.random()
}

fun DrawScope.drawSnake(snake: List<Position>) {
    snake.forEachIndexed { _, position ->
        drawRect(
            color = Color.White,
            topLeft = Offset(position.x * 20f, position.y * 20f),
            size = Size(20f, 20f),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

fun DrawScope.drawFood(position: Position) {
    drawCircle(
        color = Color.Red,
        center = Offset(position.x * 20f + 10f, position.y * 20f + 10f),
        radius = 10f
    )
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Surface {
        Game()
    }
}
