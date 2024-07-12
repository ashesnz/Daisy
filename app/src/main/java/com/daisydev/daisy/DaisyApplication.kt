package com.daisydev.daisy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// HiltAndroidApp is an annotation used to generate a Hilt application component
// This class is used to initialize Hilt
@HiltAndroidApp
class DaisyApplication: Application()