
package com.example.axolotlandfriends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// pink theme colors
val lightPink = Color(0xFFFFF0F6)
val babyPink = Color(0xFFFFD6E7)
val darkPink = Color(0xFFD85A92)
val textPink = Color(0xFF873F63)

// amphibian data model
data class Amphibian(
    val name: String,
    val type: String,
    val description: String,
    @SerializedName("img_src")
    val imageUrl: String
)

// api for getting amphibian data
interface AmphibianApi {
    @GET("amphibians")
    suspend fun getAmphibians(): List<Amphibian>
}

// retrofit setup
object ApiClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl(
            "https://android-kotlin-fun-mars-server.appspot.com/"
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: AmphibianApi =
        retrofit.create(AmphibianApi::class.java)
}

// repository for handling data
class AmphibianRepository {

    suspend fun getAmphibians(): List<Amphibian> {
        return ApiClient.api.getAmphibians()
    }
}

// ui states
sealed interface AmphibianUiState {

    data object Loading : AmphibianUiState

    data class Success(
        val amphibians: List<Amphibian>
    ) : AmphibianUiState

    data class Error(
        val message: String
    ) : AmphibianUiState
}

// viewmodel
class AmphibianViewModel : ViewModel() {

    private val repository = AmphibianRepository()

    private val _uiState =
        MutableStateFlow<AmphibianUiState>(
            AmphibianUiState.Loading
        )

    val uiState: StateFlow<AmphibianUiState> =
        _uiState

    init {
        loadAmphibians()
    }

    fun loadAmphibians() {

        viewModelScope.launch {

            _uiState.value = AmphibianUiState.Loading

            try {

                val result = repository.getAmphibians()

                _uiState.value =
                    AmphibianUiState.Success(result)

            } catch (e: Exception) {

                _uiState.value = AmphibianUiState.Error(
                    "could not load amphibians. check your internet connection."
                )
            }
        }
    }
}

// main activity
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                AmphibianApp()
            }
        }
    }
}

// main app screen
@Composable
fun AmphibianApp(
    amphibianViewModel: AmphibianViewModel = viewModel()
) {

    val uiState by amphibianViewModel.uiState
        .collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightPink)
            .padding(16.dp)
    ) {

        Text(
            text = "♡ Amphibians App ♡",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = darkPink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "explore the world of amphibians 🌸",
            fontSize = 14.sp,
            color = textPink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        when (val state = uiState) {

            is AmphibianUiState.Loading -> {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator(
                            color = darkPink
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text = "loading amphibians...",
                            color = textPink
                        )
                    }
                }
            }

            is AmphibianUiState.Success -> {

                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {

                    items(state.amphibians) { amphibian ->

                        AmphibianCard(amphibian)
                    }
                }
            }

            is AmphibianUiState.Error -> {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.Center
                ) {

                    Text(
                        text = state.message,
                        color = textPink,
                        textAlign = TextAlign.Center
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Button(
                        onClick = {
                            amphibianViewModel.loadAmphibians()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = darkPink
                        )
                    ) {
                        Text("try again ♡")
                    }
                }
            }
        }
    }
}

// amphibian card
@Composable
fun AmphibianCard(amphibian: Amphibian) {

    var expanded by remember(amphibian.name) {
        mutableStateOf(true)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                expanded = !expanded
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column {

            Text(
                text = "♡ ${amphibian.name}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = darkPink,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp
                )
            )

            Text(
                text = amphibian.type,
                fontSize = 14.sp,
                color = textPink,
                modifier = Modifier.padding(
                    start = 16.dp,
                    bottom = 12.dp
                )
            )

            AsyncImage(
                model = amphibian.imageUrl,
                contentDescription = amphibian.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(babyPink)
            )

            if (expanded) {

                Text(
                    text = amphibian.description,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = textPink,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Text(
                text = if (expanded)
                    "tap to hide ♡"
                else
                    "tap to read more ♡",
                fontSize = 12.sp,
                color = darkPink,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }
    }
}
