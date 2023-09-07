package com.pk4us.testgiphy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.pk4us.testgiphy.ui.theme.TestGiphyTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = "https://api.giphy.com/v1/"
const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestGiphyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    ScreenMain(context = this)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Composable
    fun ScreenMain(context: Context) {
        val trendingGifsState = remember { mutableStateOf<List<String>>(emptyList()) }
        val searchGifsState = remember { mutableStateOf<List<String>>(emptyList()) }
        var searchText by remember { mutableStateOf("") }

        fun searchGifs(query: String) {
            val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()

            val retroService = retrofit.create(DataService::class.java)
            retroService.searchGifs(query).enqueue(object : Callback<DataResult?> {
                override fun onResponse(
                    call: Call<DataResult?>, response: Response<DataResult?>
                ) {
                    val body = response.body()
                    if (body == null) {
                        Log.d(TAG, "onResponse: No response...")
                        return
                    }

                    if (response.isSuccessful) {
                        val gifUrls =
                            response.body()?.res?.map { it.images.ogImage.url } ?: emptyList()
                        searchGifsState.value = gifUrls
                    } else {
                        Log.d(TAG, "onResponse: No response...")
                    }
                }

                override fun onFailure(call: Call<DataResult?>, t: Throwable) {
                    Log.e(TAG, "onFailure: ${t.message}", t)
                }
            })
        }

        fun getTrendingGifs() {
            val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()

            val retroService = retrofit.create(DataService::class.java)
            retroService.getGifs().enqueue(object : Callback<DataResult?> {
                override fun onResponse(
                    call: Call<DataResult?>, response: Response<DataResult?>
                ) {
                    val body = response.body()
                    if (body == null) {
                        Log.d(TAG, "onResponse: No response...")
                        return
                    }

                    if (response.isSuccessful) {
                        val gifUrls =
                            response.body()?.res?.map { it.images.ogImage.url } ?: emptyList()
                        trendingGifsState.value = gifUrls
                    } else {
                        Log.d(TAG, "onResponse: No response...")
                    }
                }

                override fun onFailure(call: Call<DataResult?>, t: Throwable) {
                    Log.e(TAG, "onFailure: ${t.message}", t)
                }
            })
        }

        // Загрузка трендовых GIF при запуске приложения
        LaunchedEffect(Unit) {
            getTrendingGifs()
        }

        Column {
            // Search input
            val keyboardController = LocalSoftwareKeyboardController.current
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    searchGifs(it)
                },
                singleLine = true,
                label = { Text("Search GIFs") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                keyboardActions = KeyboardActions(
                    onDone = {
                        searchGifs(searchText)
                        keyboardController?.hide()
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        searchGifs(searchText)
                        keyboardController?.hide()
                    }) {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                    }
                }
            )

            GifListScreen(images = if (searchText.isEmpty()) trendingGifsState.value else searchGifsState.value) { selectedGifUrl ->
                val intent = Intent(context, SecondActivity::class.java)
                intent.putExtra(SecondActivity.GIF_URL, selectedGifUrl)
                context.startActivity(intent)
            }
        }
    }

    @Composable
    fun GifListScreen(images: List<String>, onGifClick: (String) -> Unit) {
        val requestManager = Glide.with(LocalView.current)
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(images) { imageUrl ->
                GifItem(
                    imageUrl = imageUrl,
                    onGifClick = onGifClick,
                    requestManager = requestManager,
                    requestOptions = requestOptions
                )
            }
        }
    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun GifItem(
        imageUrl: String,
        onGifClick: (String) -> Unit,
        requestManager: RequestManager,
        requestOptions: RequestOptions
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopEnd)
        ) {
            var expanded by remember { mutableStateOf(false) }
            val haptic = LocalHapticFeedback.current
            Card(
                modifier = Modifier
                    .combinedClickable(
                        onClick = { onGifClick(imageUrl) },
                        onLongClick = {
                            expanded = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                    )
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                val requestManager = remember { requestManager }
                val requestOptions = remember { requestOptions }

                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                600
                            )
                        }
                    },
                    update = { imageView ->
                        requestManager
                            .load(imageUrl)
                            .apply(requestOptions)
                            .into(imageView)
                    }
                )
            }
            if (expanded) {
                CardMenu(
                    imageUrl = imageUrl,
                    onGifClick = onGifClick,
                    onDismiss = { expanded = false })
            }
        }
    }

    @Composable
    fun CardMenu(imageUrl: String, onGifClick: (String) -> Unit, onDismiss: () -> Unit) {
        DropdownMenu(
            expanded = true,
            onDismissRequest = { onDismiss() }
        ) {
            DropdownMenuItem(
                text = { Text("Открыть") },
                onClick = { onGifClick(imageUrl) },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.OpenInNew,
                        contentDescription = null
                    )
                })
            DropdownMenuItem(
                text = { Text("Скачать") },
                onClick = { /* Handle settings! */ },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Save,
                        contentDescription = null
                    )
                })
            Divider()
            DropdownMenuItem(
                text = { Text("Поделиться") },
                onClick = { /* Handle send feedback! */ },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = null
                    )
                })
        }
    }
}