package com.example.greenwaste

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

            val oldBin = bin.value
            bin.value = dataSnapshot.getValue(Bin::class.java) ?: Bin()

            if ((oldBin.distance1 < MAX_BIN_HEIGHT && bin.value.distance1 >= MAX_BIN_HEIGHT) ||
                (oldBin.distance2 < MAX_BIN_HEIGHT && bin.value.distance2 >= MAX_BIN_HEIGHT)) {
                sendNotification(context)
            }
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
fun sendNotification(context: Context) {
    val notificationId = 101
    val channelId = "bin_status_channel"
    val channelName = "Bin Status Channel"
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent,
        PendingIntent.FLAG_IMMUTABLE)

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Bin Status")
        .setContentText("One or more bins are full.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    createNotificationChannel(context, channelId, channelName)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notify(notificationId, builder.build())
    }
}

fun createNotificationChannel(context: Context, channelId: String, channelName: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance)
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}



data class Bin(
    var distance1: Int = 0,
    var distance2: Int = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)

const val MAX_BIN_HEIGHT = 15.0  // Maximum height of the bin in centimeters
