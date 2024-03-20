package com.example.greenwaste

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.greenwaste.ui.theme.GreenWasteTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GreenWasteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    BinStatusScreen()
                }
            }
        }
    }
}

@Composable
fun BinStatusScreen() {
    val database = FirebaseDatabase.getInstance().getReference("sensorData")
    val context = LocalContext.current

    val bin = remember { mutableStateOf(Bin()) }

    database.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            bin.value = dataSnapshot.getValue(Bin::class.java) ?: Bin()
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Handle error
            Log.d("Firebase", "Error: ${databaseError.message}")
        }
    })

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row {
                BinStatusColumn("Wet Bin", bin.value.distance1, Color.Blue)
                Spacer(modifier = Modifier.width(16.dp))
                BinStatusColumn("Dry Bin", bin.value.distance2, Color.Green)
            }
            Text(text = "Latitude: ${bin.value.latitude}, Longitude: ${bin.value.longitude}")
            Button(onClick = {
                val gmmIntentUri = Uri.parse("geo:${bin.value.latitude},${bin.value.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                context.startActivity(mapIntent)
            }){
                Text(text = "Open in Maps")
            }
        }
    }
}

@Composable
fun BinStatusColumn(title: String, fillLevel: Int, color: Color) {
    val fillPercentage = (fillLevel / MAX_BIN_HEIGHT).coerceIn(0.0, 1.0)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(200.dp)
                .border(2.dp, Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = fillPercentage.toFloat())
                    .background(color)
                    .align(Alignment.BottomStart)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 1 - fillPercentage.toFloat())
                    .background(Color.White)
                    .align(Alignment.TopStart)
            )
        }
        Text(
            text = "$title: $fillLevel cm",
            color = Color.Black
        )
    }
}

data class Bin(
    var distance1: Int = 0,
    var distance2: Int = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)

const val MAX_BIN_HEIGHT = 15.0  // Maximum height of the bin in centimeters
