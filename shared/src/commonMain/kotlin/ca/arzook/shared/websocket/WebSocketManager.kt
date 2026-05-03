package ca.arzook.shared.websocket

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WebSocketMessage(
    val type: String,
    val data: String? = null,
    val tradeId: String? = null,
    val action: String? = null,
    val title: String? = null,
    val message: String? = null
)

class WebSocketManager(private val baseUrl: String) {
    private var session: DefaultClientWebSocketSession? = null
    private val client = HttpClient {
        install(WebSockets) {
            pingInterval = 20_000 // 20 seconds
        }
    }
    
    private val _messages = MutableSharedFlow<WebSocketMessage>()
    val messages: SharedFlow<WebSocketMessage> = _messages.asSharedFlow()
    
    private val _connectionState = MutableSharedFlow<ConnectionState>()
    val connectionState: SharedFlow<ConnectionState> = _connectionState.asSharedFlow()
    
    private val json = Json { ignoreUnknownKeys = true }
    
    suspend fun connect(token: String) {
        try {
            _connectionState.emit(ConnectionState.CONNECTING)
            println("[WebSocket] Connecting to $baseUrl/ws")
            
            client.webSocket(
                urlString = "$baseUrl/ws",
                request = {
                    header("Authorization", "Bearer $token")
                }
            ) {
                session = this
                _connectionState.emit(ConnectionState.CONNECTED)
                println("[WebSocket] Connected successfully")
                
                // Listen for incoming messages
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            println("[WebSocket] Received: $text")
                            try {
                                val message = json.decodeFromString<WebSocketMessage>(text)
                                _messages.emit(message)
                            } catch (e: Exception) {
                                println("[WebSocket] Failed to parse message: ${e.message}")
                            }
                        }
                        is Frame.Close -> {
                            println("[WebSocket] Connection closed")
                            _connectionState.emit(ConnectionState.DISCONNECTED)
                        }
                        else -> {}
                    }
                }
            }
        } catch (e: Exception) {
            println("[WebSocket] Connection error: ${e.message}")
            _connectionState.emit(ConnectionState.ERROR)
        }
    }
    
    suspend fun send(message: String) {
        try {
            session?.send(Frame.Text(message))
            println("[WebSocket] Sent: $message")
        } catch (e: Exception) {
            println("[WebSocket] Send error: ${e.message}")
        }
    }
    
    suspend fun disconnect() {
        try {
            session?.close()
            client.close()
            println("[WebSocket] Disconnected")
        } catch (e: Exception) {
            println("[WebSocket] Disconnect error: ${e.message}")
        }
    }
    
    enum class ConnectionState {
        CONNECTING, CONNECTED, DISCONNECTED, ERROR
    }
}
