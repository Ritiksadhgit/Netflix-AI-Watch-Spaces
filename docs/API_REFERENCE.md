# Netflix AI Watch Spaces — API Reference Manual

This document details every implemented REST endpoint and WebSocket event in the Netflix AI Watch Spaces platform.

---

## 1. Authentication Endpoints (`/api/v1/auth`)

### 1.1 Register New User
- **Method**: `POST`
- **Path**: `/api/v1/auth/register`
- **Purpose**: Creates a new user account with encrypted password and returns JWT access and refresh tokens.
- **Authentication**: None (Public)
- **Role Requirement**: None
- **Request Example**:
```json
{
  "email": "sarah@cinephile.org",
  "password": "SecurePassword123!",
  "displayName": "Sarah Connor"
}
```
- **Response Example** (`201 Created`):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "7c8e9b0a-4f12-4c28-910a-3a2e1b4c5d6e",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": 12,
    "email": "sarah@cinephile.org",
    "displayName": "Sarah Connor",
    "role": "VIEWER",
    "avatarUrl": null
  }
}
```
- **Important Errors**: `400 Bad Request` (Email already registered or validation failure).

---

### 1.2 User Login
- **Method**: `POST`
- **Path**: `/api/v1/auth/login`
- **Purpose**: Authenticates user credentials and generates access/refresh tokens.
- **Authentication**: None (Public)
- **Role Requirement**: None
- **Request Example**:
```json
{
  "email": "sarah@cinephile.org",
  "password": "SecurePassword123!"
}
```
- **Response Example** (`200 OK`):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "7c8e9b0a-4f12-4c28-910a-3a2e1b4c5d6e",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": 12,
    "email": "sarah@cinephile.org",
    "displayName": "Sarah Connor",
    "role": "VIEWER"
  }
}
```
- **Important Errors**: `401 Unauthorized` (Invalid email or password).

---

### 1.3 Refresh Access Token
- **Method**: `POST`
- **Path**: `/api/v1/auth/refresh`
- **Purpose**: Issues a new 15-minute access token using an active refresh token.
- **Authentication**: None (Validated via refresh token payload)
- **Role Requirement**: None
- **Request Example**:
```json
{
  "refreshToken": "7c8e9b0a-4f12-4c28-910a-3a2e1b4c5d6e"
}
```
- **Response Example** (`200 OK`):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "7c8e9b0a-4f12-4c28-910a-3a2e1b4c5d6e",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```
- **Important Errors**: `401 Unauthorized` (Expired or revoked refresh token).

---

### 1.4 Get Current User Profile
- **Method**: `GET`
- **Path**: `/api/v1/auth/me`
- **Purpose**: Returns the authenticated user profile.
- **Authentication**: Bearer Token
- **Role Requirement**: Authenticated User
- **Response Example** (`200 OK`):
```json
{
  "id": 12,
  "email": "sarah@cinephile.org",
  "displayName": "Sarah Connor",
  "role": "VIEWER",
  "avatarUrl": null
}
```

---

## 2. Catalog Titles (`/api/v1/titles`)

### 2.1 List All Titles
- **Method**: `GET`
- **Path**: `/api/v1/titles`
- **Purpose**: Retrieves all available movies in the catalog.
- **Authentication**: None (Public)
- **Response Example** (`200 OK`):
```json
[
  {
    "id": 1,
    "name": "Tears of Steel",
    "synopsis": "Set in a dystopian future Amsterdam...",
    "durationSeconds": 734,
    "videoAssetUrl": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
    "posterUrl": "https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=600&q=80",
    "genres": "Sci-Fi, Action",
    "ratingCode": "PG-13",
    "releaseYear": 2012
  }
]
```

---

## 3. Watch Space Operations (`/api/v1/watch-spaces`)

### 3.1 Create Watch Space
- **Method**: `POST`
- **Path**: `/api/v1/watch-spaces`
- **Purpose**: Creates a new synchronized room with the creator as Host.
- **Authentication**: Bearer Token
- **Role Requirement**: Authenticated User
- **Request Example**:
```json
{
  "name": "Cyberpunk Watch Night",
  "titleId": 1,
  "privacy": "PUBLIC",
  "maxParticipants": 25,
  "aiVerbosity": "NORMAL",
  "votingEnabled": true
}
```
- **Response Example** (`201 Created`):
```json
{
  "id": "ws_7a9f81",
  "name": "Cyberpunk Watch Night",
  "status": "LIVE",
  "privacy": "PUBLIC",
  "inviteCode": "CYBER9",
  "maxParticipants": 25,
  "isLocked": false,
  "playbackState": "PAUSED",
  "playbackPositionSeconds": 0.0,
  "title": { "id": 1, "name": "Tears of Steel" },
  "hostUser": { "id": 12, "displayName": "Sarah Connor" },
  "participantCount": 1,
  "isHost": true
}
```

---

### 3.2 List Active Public Watch Spaces
- **Method**: `GET`
- **Path**: `/api/v1/watch-spaces`
- **Purpose**: Retrieves currently live watch spaces, filtering stale rooms (> 2 hours old with 0 active connections).
- **Authentication**: Bearer Token (Optional)
- **Response Example** (`200 OK`):
```json
[
  {
    "id": "ws_demo_tears",
    "name": "Tears of Steel Watch Space",
    "status": "LIVE",
    "inviteCode": "TEARS1",
    "participantCount": 3,
    "hostUser": { "displayName": "Elena Rostova" }
  }
]
```

---

### 3.3 Pin AI Fact (Host Only)
- **Method**: `POST`
- **Path**: `/api/v1/watch-spaces/{id}/pin-fact`
- **Purpose**: Host pins a verified grounded AI response card visible to all room participants.
- **Authentication**: Bearer Token
- **Role Requirement**: Room Host or ADMIN
- **Request Example**:
```json
{
  "text": "Thom operates the robotic arm on the Amsterdam bridge sequence.",
  "citations": [
    {
      "timelineEventId": 2,
      "timestamp": 45,
      "eventType": "CHARACTER",
      "title": "Thom",
      "snippet": "Thom is Lead Engineer."
    }
  ],
  "pinnedBy": "Host"
}
```
- **Response Example** (`200 OK`): Echoes pinned fact object.
- **Important Errors**: `403 Forbidden` (User is not the room Host).

---

### 3.4 Approve Content Variant (Host Only)
- **Method**: `POST`
- **Path**: `/api/v1/watch-spaces/{id}/variant/approve`
- **Purpose**: Host approves a pre-authored localized asset variant (e.g. Spanish subtitles).
- **Authentication**: Bearer Token
- **Role Requirement**: Room Host or ADMIN
- **Request Example**:
```json
{
  "variantId": "es",
  "label": "Spanish Subtitles",
  "lang": "es"
}
```
- **Response Example** (`200 OK`): Echoes approved variant payload.
- **Important Errors**: `403 Forbidden` (User is not the room Host).

---

## 4. Timeline & AI Co-Pilot (`/api/v1/titles/{titleId}`)

### 4.1 Get Content Timeline
- **Method**: `GET`
- **Path**: `/api/v1/titles/{titleId}/timeline`
- **Purpose**: Retrieves all authored chronological events for the given title.
- **Authentication**: None (Public)
- **Response Example** (`200 OK`):
```json
[
  {
    "id": 1,
    "titleId": 1,
    "tsSeconds": 15,
    "eventType": "SCENE",
    "title": "The Desolate Bridge of Amsterdam",
    "payloadJson": "{\"description\": \"Opening sequence showing Thom and Celia on the bridge.\"}"
  },
  {
    "id": 2,
    "titleId": 1,
    "tsSeconds": 45,
    "eventType": "CHARACTER",
    "title": "Thom",
    "payloadJson": "{\"name\": \"Thom\", \"role\": \"Lead Engineer\", \"description\": \"Operates robotic prosthetic arm.\"}"
  }
]
```

---

### 4.2 Ask Grounded AI Co-Pilot
- **Method**: `POST`
- **Path**: `/api/v1/titles/{titleId}/ai/ask`
- **Purpose**: Submits a user question grounded to authored metadata within `[timestamp - 90s, timestamp + 30s]`.
- **Authentication**: Bearer Token
- **Request Example**:
```json
{
  "question": "Who is Thom?",
  "currentTimestamp": 50.0,
  "verbosity": "NORMAL"
}
```
- **Response Example** (`200 OK`):
```json
{
  "answer": "Thom is Lead Engineer. Operates robotic prosthetic arm.",
  "currentScene": "The Desolate Bridge of Amsterdam",
  "timestamp": 50.0,
  "sources": [
    {
      "timelineEventId": 2,
      "timestamp": 45,
      "eventType": "CHARACTER",
      "title": "Thom",
      "snippet": "Thom: Lead Engineer - Operates robotic prosthetic arm."
    }
  ]
}
```

---

## 5. Platform Analytics (`/api/v1/analytics`)

### 5.1 Global Dashboard Statistics
- **Method**: `GET`
- **Path**: `/api/v1/analytics/dashboard`
- **Purpose**: Computes real platform metrics (live spaces count, total watched seconds/hours, AI question volume, trivia accuracy).
- **Authentication**: Bearer Token
- **Response Example** (`200 OK`):
```json
{
  "liveSpacesCount": 3,
  "totalWatchedSeconds": 66240,
  "totalWatchedHours": 18.4,
  "aiQuestionsCount": 42,
  "triviaAccuracy": null
}
```

---

## 6. Admin Timeline Studio (`/api/v1/admin/titles/{titleId}/timeline`)

*All endpoints in this group require `ROLE_ADMIN`.*

### 6.1 Create Timeline Marker
- **Method**: `POST`
- **Path**: `/api/v1/admin/titles/{titleId}/timeline/events`
- **Request Example**:
```json
{
  "tsSeconds": 120,
  "eventType": "SCENE",
  "title": "Bridge Confrontation",
  "payloadJson": "{\"description\": \"Robotic forces advance.\"}"
}
```
- **Response Example** (`201 Created`): Returns newly created `TimelineResponse`.

### 6.2 Bulk Import Timeline
- **Method**: `POST`
- **Path**: `/api/v1/admin/titles/{titleId}/timeline/import`
- **Request Example**:
```json
{
  "replaceExisting": false,
  "events": [
    {
      "tsSeconds": 15,
      "eventType": "SCENE",
      "title": "Bridge Intro",
      "payloadJson": "{\"description\": \"Opening scene.\"}"
    }
  ]
}
```
- **Response Example** (`200 OK`):
```json
{
  "titleId": 1,
  "importedCount": 1,
  "replacedExisting": false,
  "status": "SUCCESS",
  "message": "Atomically imported 1 timeline markers"
}
```

---

## 7. WebSocket Event Protocol (`/ws/watch-space`)

Every WebSocket frame follows standard envelope format:
```json
{
  "event": "event.name",
  "watchSpaceId": "ws_demo_tears",
  "payload": { ... }
}
```

### Event Specification Matrix

| Event Name | Direction | Payload Structure | Auth / RBAC | Purpose |
| :--- | :---: | :--- | :--- | :--- |
| `room.playback.snapshot` | S $\rightarrow$ C | `{ state, position, serverTs, rate }` | Handshake | Initial playback synchronization upon join/reconnect |
| `room.playback.update` | C $\rightarrow$ S $\rightarrow$ C | `{ state, position, rate }` | **Host / Admin** | Updates authoritative room stream position & state |
| `room.sync.ping` | C $\rightarrow$ S | `{ clientSendTs }` | Any | NTP clock-skew and RTT latency estimation probe |
| `room.sync.pong` | S $\rightarrow$ C | `{ clientSendTs, serverTs }` | Any | Reply to sync ping with authoritative server timestamp |
| `room.presence.update` | S $\rightarrow$ C | `{ participants: [...], totalCount }` | Any | Broadcasts updated roster of active users in the room |
| `room.chat.message` | C $\rightarrow$ S $\rightarrow$ C | `{ body, tsSeconds, senderName }` | Unmuted User | Broadcasts sanitized chat message to room participants |
| `room.chat.typing` | C $\rightarrow$ S $\rightarrow$ C | `{ userId, displayName, isTyping }` | Any | Broadcasts typing presence indicator |
| `room.ai.ask` | C $\rightarrow$ S | `{ question, currentTimestamp }` | Any | Submits temporal grounded AI question |
| `room.ai.answer` | S $\rightarrow$ C | `{ answer, currentScene, sources }` | Unicast Requester | Direct reply containing grounded answer with citations |
| `room.ai.pinFact` | C $\rightarrow$ S $\rightarrow$ C | `{ text, citations, pinnedBy }` | **Host / Admin** | Pins AI fact card for all room participants |
| `room.ai.unpinFact` | C $\rightarrow$ S $\rightarrow$ C | `{}` | **Host / Admin** | Unpins current AI fact card |
| `room.variant.approve` | C $\rightarrow$ S $\rightarrow$ C | `{ variantId, label, lang }` | **Host / Admin** | Synchronously switches subtitle/asset variant for room |
| `room.variant.applied` | S $\rightarrow$ C | `{ variantId, label, lang }` | Any | Broadcasts newly active localized variant |
| `room.trivia.trigger` | S $\rightarrow$ C | `{ question, options, points }` | System Event | Pushes synchronized scene trivia challenge to room |
| `room.variation.trigger` | S $\rightarrow$ C | `{ title, prompt, options }` | System Event | Opens interactive narrative voting overlay |
| `room.variation.voteCast`| C $\rightarrow$ S | `{ variationId, optionId }` | Participant | Casts individual vote for narrative branch |
| `room.variation.result` | S $\rightarrow$ C | `{ winnerOptionId, counts }` | Broadcast | Broadcasts winning narrative decision |
| `room.moderation.action` | C $\rightarrow$ S $\rightarrow$ C | `{ action: MUTE\|KICK\|LOCK, targetUserId }` | **Host / Admin** | Moderation governance action |
| `room.action.rejected` | S $\rightarrow$ C | `{ reason, code }` | Direct Rejected User | Sent when user lacks authority or performs invalid action |
