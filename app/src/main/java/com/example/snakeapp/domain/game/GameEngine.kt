package com.example.snakeapp.domain.game

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import androidx.compose.runtime.mutableStateOf
import com.example.snakeapp.R
import com.example.snakeapp.data.model.State

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class GameEngine(
    private val scope: CoroutineScope,
    private val onGameEnded: () -> Unit,
    private val onFoodEaten: () -> Unit,
    private val context: Context
) {
    var mMediaPlayer: MediaPlayer? = null
    var mpEaten: MediaPlayer? = null


    private val mutex = Mutex()
    private val mutableState =
        MutableStateFlow(
            State(
                food = Pair(5, 5),
                snake = listOf(Pair(7, 7)),
                currentDirection = SnakeDirection.Right
            )
        )
    val state: Flow<State> = mutableState
    private val currentDirection = mutableStateOf(SnakeDirection.Right)

    var move = Pair(1, 0)
        set(value) {
            scope.launch {
                mutex.withLock {
                    field = value
                }
            }
        }

    fun playSound(context: Context, sound: Int) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(context, sound)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }
    fun pauseSound() {
        if (mMediaPlayer?.isPlaying == true) mMediaPlayer?.pause()
    }
    private fun stopSound() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    fun playSoundEaten(context: Context) {
        if (mpEaten == null) {
            mpEaten = MediaPlayer.create(context, SOUND_EATEN)
            mpEaten!!.isLooping = false
            mpEaten!!.start()
        } else mpEaten!!.start()
    }


    private fun stopSoundEaten() {
        if (mpEaten != null) {
            mpEaten!!.stop()
            mpEaten!!.release()
            mpEaten = null
        }
    }



    fun reset() {
        mutableState.update {
            it.copy(
                food = Pair(5, 5),
                snake = listOf(Pair(7, 7)),
                currentDirection = SnakeDirection.Right
            )
        }
        currentDirection.value = SnakeDirection.Right
        move = Pair(1, 0)
    }

    init {
        scope.launch {
            var snakeLength = 2
            while (true) {
                playSound(context, SOUND_GAMING)
                delay(150)
                mutableState.update {
                    val hasReachedLeftEnd =
                        it.snake.first().first == 0 && it.currentDirection == SnakeDirection.Left
                    val hasReachedTopEnd =
                        it.snake.first().second == 0 && it.currentDirection == SnakeDirection.Up
                    val hasReachedRightEnd =
                        it.snake.first().first == BOARD_SIZE - 1 && it.currentDirection == SnakeDirection.Right
                    val hasReachedBottomEnd =
                        it.snake.first().second == BOARD_SIZE - 1 && it.currentDirection == SnakeDirection.Down
                    if (hasReachedLeftEnd || hasReachedTopEnd || hasReachedRightEnd || hasReachedBottomEnd) {
                        snakeLength = 2
                        onGameEnded.invoke()
                        stopSound()
                    }
                    if (move.first == 0 && move.second == -1) {
                        currentDirection.value = SnakeDirection.Up
                    } else if (move.first == -1 && move.second == 0) {
                        currentDirection.value = SnakeDirection.Left
                    } else if (move.first == 1 && move.second == 0) {
                        currentDirection.value = SnakeDirection.Right
                    } else if (move.first == 0 && move.second == 1) {
                        currentDirection.value = SnakeDirection.Down
                    }
                    val newPosition = it.snake.first().let { poz ->
                        mutex.withLock {
                            Pair(
                                (poz.first + move.first + BOARD_SIZE) % BOARD_SIZE,
                                (poz.second + move.second + BOARD_SIZE) % BOARD_SIZE
                            )
                        }
                    }
                    if (newPosition == it.food) {
                        onFoodEaten.invoke()
                        pauseSound()
                        playSoundEaten(context)
                        snakeLength++
                    }

                    if (it.snake.contains(newPosition)) {
                        snakeLength = 2
                        onGameEnded.invoke()
                        stopSound()
                        stopSoundEaten()
                        playSoundEaten(context)
                    }

                    it.copy(
                        food = if (newPosition == it.food) Pair(
                            Random().nextInt(BOARD_SIZE),
                            Random().nextInt(BOARD_SIZE)
                        ) else it.food,
                        snake = listOf(newPosition) + it.snake.take(snakeLength - 1),
                        currentDirection = currentDirection.value,
                    )
                }
            }
        }
    }

    companion object {
        const val BOARD_SIZE = 32
        val SOUND_GAMING = R.raw.gaming
        val SOUND_EATEN = R.raw.eaten
        val SOUND_GAMEOVER = R.raw.gameover
    }
}
