package com.example.snakeapp.presentation.activity

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import com.example.snakeapp.R
import com.example.snakeapp.data.cache.GameCache
import com.example.snakeapp.data.model.HighScore
import com.example.snakeapp.domain.base.BaseActivity
import com.example.snakeapp.domain.base.TOP_10
import com.example.snakeapp.domain.game.GameEngine
import com.example.snakeapp.presentation.screen.EndScreen
import com.example.snakeapp.presentation.screen.GameScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GameActivity : BaseActivity() {
    private lateinit var dataStore: GameCache
    private val isPlaying = mutableStateOf(true)
    private var score = mutableStateOf(0)
    private lateinit var scope: CoroutineScope
    private lateinit var playerName: String
    private lateinit var highScores: List<HighScore>
    private lateinit var gameEngine: GameEngine
    var mpGameOver: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameEngine = GameEngine(
            context = this,
            scope = lifecycleScope,
            onGameEnded = {
                if (isPlaying.value) {
                    isPlaying.value = false
                    scope.launch { dataStore.saveHighScore(highScores) }
                }
            },
            onFoodEaten = { score.value++ }
        )
    }
    fun playSoundGameOver(context: Context) {
        if (mpGameOver == null) {
            mpGameOver = MediaPlayer.create(context, GameEngine.SOUND_GAMEOVER)
            mpGameOver!!.isLooping = false
            mpGameOver!!.start()
        } else mpGameOver!!.start()
    }

    @Composable
    override fun Content() {

        scope = rememberCoroutineScope()
        dataStore = GameCache(applicationContext)
        playerName =
            dataStore.getPlayerName.collectAsState(initial = stringResource(id = R.string.default_player_name)).value
        highScores = dataStore.getHighScores.collectAsState(initial = listOf()).value.plus(
            HighScore(playerName, score.value)
        ).sortedByDescending { it.score }.take(TOP_10)
        Column {
            if (isPlaying.value) {
                GameScreen(gameEngine, score.value)
            } else {
                playSoundGameOver(LocalContext.current)
                EndScreen(score.value) {
                    score.value = 0
                    gameEngine.reset()
                    isPlaying.value = true
                }
            }
        }
    }
}