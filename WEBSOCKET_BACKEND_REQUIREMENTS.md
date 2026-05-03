# WebSocket Backend Requirements for Arzook

## Endpoint
```
wss://api.arzook.ca/ws
```

## Authentication
- Client sends JWT token in `Authorization` header: `Bearer <token>`
- Server validates token before accepting WebSocket connection
- If invalid, close connection with code 401

## Message Format
All messages are JSON with this structure:

```json
{
  "type": "MESSAGE_TYPE",
  "data": "optional data",
  "tradeId": "optional trade ID",
  "action": "optional action",
  "title": "optional title",
  "message": "optional message"
}
```

## Message Types (Server → Client)

### 1. TRADE_UPDATE
Sent when any trade is created, updated, or deleted in the public trades list.

```json
{
  "type": "TRADE_UPDATE",
  "tradeId": "abc-123",
  "action": "CREATED" | "UPDATED" | "DELETED"
}
```

**When to send:**
- New trade posted
- Trade amount/rate changed
- Trade deleted
- Trade status changed (deposited, advertised, etc.)

### 2. DRAFT_CREATED
Sent when user creates a buying or selling draft.

```json
{
  "type": "DRAFT_CREATED",
  "tradeId": "draft-id",
  "action": "BUYING" | "SELLING"
}
```

### 3. DRAFT_UPDATED
Sent when user updates their draft.

```json
{
  "type": "DRAFT_UPDATED",
  "tradeId": "draft-id",
  "action": "BUYING" | "SELLING"
}
```

### 4. DRAFT_DELETED
Sent when user deletes their draft.

```json
{
  "type": "DRAFT_DELETED",
  "tradeId": "draft-id",
  "action": "BUYING" | "SELLING"
}
```

### 5. TRADE_MATCHED
Sent when user's trade is matched with another user.

```json
{
  "type": "TRADE_MATCHED",
  "tradeId": "trade-id",
  "message": "Your trade has been matched!"
}
```

### 6. NOTIFICATION
General notification for user.

```json
{
  "type": "NOTIFICATION",
  "title": "Notification Title",
  "message": "Notification message",
  "data": "optional additional data"
}
```

**Examples:**
- "Your trade has been deposited"
- "Your selling draft is about to expire"
- "New trade matching your criteria"

## Connection Lifecycle

1. **Client connects** with Authorization header
2. **Server validates** token
3. **Server accepts** connection if valid
4. **Server sends** relevant messages to client
5. **Client can send** ping/pong to keep alive (handled by Ktor automatically)
6. **Server closes** connection if:
   - Token expires
   - User logs out
   - Inactivity timeout (optional)

## Ping/Pong
- Client sends ping every 20 seconds
- Server should respond with pong
- If no pong received, client reconnects

## User-Specific Messages
- Only send messages relevant to the connected user
- Don't broadcast all trades to all users
- Filter by:
  - User's own drafts/trades
  - Trades user is watching
  - Public trade updates (everyone gets these)

## Implementation Notes

### Spring Boot Example (Java/Kotlin)
```kotlin
@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(tradeWebSocketHandler(), "/ws")
            .setAllowedOrigins("*")
    }
}

@Component
class TradeWebSocketHandler : TextWebSocketHandler() {
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    
    override fun afterConnectionEstablished(session: WebSocketSession) {
        val token = session.handshakeHeaders["Authorization"]?.firstOrNull()
        // Validate token and store session
    }
    
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        // Handle incoming messages if needed
    }
    
    fun broadcastTradeUpdate(tradeId: String, action: String) {
        val message = """{"type":"TRADE_UPDATE","tradeId":"$tradeId","action":"$action"}"""
        sessions.values.forEach { it.sendMessage(TextMessage(message)) }
    }
}
```

### Node.js Example
```javascript
const WebSocket = require('ws');
const wss = new WebSocket.Server({ port: 8080, path: '/ws' });

wss.on('connection', (ws, req) => {
  const token = req.headers.authorization;
  // Validate token
  
  ws.on('message', (message) => {
    // Handle incoming messages
  });
});

function broadcastTradeUpdate(tradeId, action) {
  const message = JSON.stringify({
    type: 'TRADE_UPDATE',
    tradeId,
    action
  });
  wss.clients.forEach(client => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(message);
    }
  });
}
```

## Testing
Use `wscat` to test:
```bash
npm install -g wscat
wscat -c wss://api.arzook.ca/ws -H "Authorization: Bearer YOUR_TOKEN"
```

## Security
- Always validate JWT token
- Rate limit connections per user
- Close connections on suspicious activity
- Log all WebSocket connections for monitoring
