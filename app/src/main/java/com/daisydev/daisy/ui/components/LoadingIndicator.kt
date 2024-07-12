package com.daisydev.daisy.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * A circular progress indicator that fills the available space.
 *
 * @param backgroundColor (optional) The background color of the progress indicator.
 */
@Composable
fun LoadingIndicator(backgroundColor: Color? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        if (backgroundColor != null) {
            CircularProgressIndicator(
                color = backgroundColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}