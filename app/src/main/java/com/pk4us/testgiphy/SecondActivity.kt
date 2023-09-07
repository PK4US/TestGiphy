package com.pk4us.testgiphy

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class SecondActivity : ComponentActivity() {
    companion object {
        const val GIF_URL = "extra_gif_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gifUrl = intent.getStringExtra(GIF_URL)

        setContent {
            FullScreenGifScreen(gifUrl)
        }
    }
}

@Composable
fun FullScreenGifScreen(gifUrl: String?) {
    gifUrl?.let {
        val requestManager = Glide.with(LocalView.current)
        val requestOptions = remember {
            RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            // Add other Glide parameters if needed
        }

        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { imageView ->
                requestManager
                    .load(gifUrl)
                    .apply(requestOptions)
                    .into(imageView)
            }
        )
    }
}