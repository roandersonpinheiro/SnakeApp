package com.example.snakeapp.presentation.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.example.snakeapp.R
import com.example.snakeapp.domain.extension.launchActivity
import com.example.snakeapp.domain.navigation.Screen
import com.example.snakeapp.presentation.activity.GameActivity
import com.example.snakeapp.presentation.component.AppButton
import com.example.snakeapp.presentation.component.DisplayLarge
import com.example.snakeapp.ui.theme.border2dp
import com.example.snakeapp.ui.theme.padding16dp
import com.example.snakeapp.ui.theme.padding64dp
import com.example.snakeapp.ui.theme.width248dp

@Composable
fun MenuScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding16dp)
            .border(width = border2dp, color = MaterialTheme.colorScheme.onBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val context = LocalContext.current
        DisplayLarge(text = stringResource(id = R.string.app_name))
        AppButton(
            modifier = Modifier
                .width(width248dp)
                .padding(top = padding64dp),
            text = stringResource(R.string.new_game)
        ) { context.launchActivity<GameActivity>() }
        AppButton(
            modifier = Modifier.width(width248dp),
            text = stringResource(id = R.string.high_score)
        ) {
            navController.navigate(Screen.HighScores.route)
        }
        AppButton(modifier = Modifier.width(width248dp), text = stringResource(R.string.settings)) {
            navController.navigate(Screen.Settings.route)
        }
        AppButton(modifier = Modifier.width(width248dp), text = stringResource(R.string.about)) {
            navController.navigate(Screen.About.route)
        }
    }
}