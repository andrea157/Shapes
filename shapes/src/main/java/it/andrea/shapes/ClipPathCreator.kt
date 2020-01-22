package it.andrea.shapes

import android.graphics.Path

interface ClipPathCreator {

    fun createClipPath(width: Int, height: Int): Path

    fun requireBitmap(): Boolean

}