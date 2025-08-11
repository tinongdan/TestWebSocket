package com.example.testwebsocket

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.testwebsocket.ui.theme.TestWebsocketTheme
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<String>()) }

    val wsClient = remember {
        WebSocketClient(object : LoggingWebSocketListener() {
            override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                messages = messages + text
            }
        })
    }

    LaunchedEffect(Unit) {
        // wsClient.connect("ws://10.0.2.2:8080/chat")                        // for Android emulator
        wsClient.connect("https://4378264ce69b.ngrok-free.app/chat")    // for remote device
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("WebSocket Application") }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text("Messages", fontWeight = FontWeight.Bold)
            messages.forEach {
                Text(it, modifier = Modifier.padding(4.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter message") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                wsClient.sendMessage(message)
                message = ""
            }) {
                Text("Send")
            }
        }
    }
}

class WebSocketClient(
    private val listener: WebSocketListener
) {
    private var webSocket: WebSocket? = null

    fun connect(url: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, listener)
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun close() {
        webSocket?.close(1_000, "Closing")
    }
}

open class LoggingWebSocketListener : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WS", "Connected")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WS", "Message: $text")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d("WS", "Bytes: ${bytes.hex()}")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WS", "Closing: $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WS", "Error", t)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestWebsocketTheme {
        MainScreen()
    }
}
