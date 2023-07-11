package com.example.snakeapp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

data class State(val food: Pair<Int, Int>, val snake: List<Pair<Int, Int>>)
class Game(private val scope: CoroutineScope) {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(
        State(
            food = Pair(5, 5),
            snake = listOf(Pair(7, 7))
        )
    )
    val state: Flow<State> = mutableState

    var move = Pair(0, 0)
        set(value) {
            scope.launch {
                mutex.withLock {
                    field = value
                }
            }
        }

    init {
        scope.launch {
            var snakeLength = 4
            while (true) {
                delay(150)
                mutableState.update {
                    val newPosition = it.snake.first().let { pos ->
                        mutex.withLock {
                            Pair(
                                (pos.first + move.first + BOARD_SIZE) % BOARD_SIZE,
                                (pos.second + move.second + BOARD_SIZE) % BOARD_SIZE
                            )
                        }

                    }

                    if (newPosition == it.food) {
                        snakeLength++
                    }
                    if (it.snake.contains(newPosition)) {
                        snakeLength = 4
                        println("chegou aqui")
                    }
                    it.copy(
                        food = if (newPosition == it.food) Pair(
                            Random.nextInt(BOARD_SIZE),
                            Random.nextInt(BOARD_SIZE)
                        ) else it.food,
                        snake = listOf(newPosition) + it.snake.take(snakeLength - 4)
                    )
                }
            }
        }
    }

    companion object {
        const val BOARD_SIZE = 16
    }
}