# EBChat - Android Social Media & Messaging Application

A feature-rich Android social media and messaging application built with **Kotlin**, **Jetpack Compose**, **Firebase**, and **Supabase**. EBChat features a beautiful pink glass theme design with real-time messaging, stories, posts, group chats, and push notifications.

## Features

### Authentication
- Email/password authentication with Firebase Auth
- Google Sign-In integration
- User registration with name, email, phone,irth date, and gender

### Messaging
- Real-time one-to-one messaging
- Group chat support
- Reply to messages
- Forward messages
- Delete messages (with "This message was deleted" indicator)
- Emoji reactions on messages
- Typing indicators ("typing..." shown when user is typing)
- Online/offline status with green indicator dot
- Voice message support
- Image and video sharing
- Message status: Sending, Sent, Delivered, Read

### Stories
- Upload image and video stories
- Auto-delete after 12 hours
- Manual delete option
- View who saw your story
- React to stories with emojis
- Stories shown at top of feed

### Posts & Feed (Home Screen)
- Create posts with title, content, and tags
- Upload images and videos to posts
- Like/unlike posts with heart animation
- Bookmark posts
- Comment on posts
- View count tracking
- Search posts by title, content, or tags
- Beautiful card-based feed layout

### Groups
- Create groups
- Add/remove members
- Admin controls
- Group messaging with media support
- Search groups

### Profile
- View profile with posts, followers, following stats
- Edit profile (name, bio, phone, status)
- Upload profile photo via Supabase Storage
- View saved posts
- View my posts
- View my groups

### Settings
- 8 beautiful themes (Pink Glass, Blue, Purple, Green, Orange, Red, Teal, Dark)
- Notification preferences
- Block/Mute users
- Privacy settings (online status, read receipts)
- Dark mode toggle
- Sound and vibration settings

### Notifications
- Firebase Cloud Messaging (FCM) for push notifications
- Real-time notifications even when app is closed
- Reply directly from notification
- Mark as read from notification
- Custom notification sound
- In-app notification inbox
- Typing indicator shown in real-time
- Online status shown when user is connected to internet (even if app is closed)

### Design
- Beautiful Pink Glass theme (primary design)
- 8 customizable color themes
- Glass morphism design elements
- Smooth animations and transitions
- Material Design 3 components
- Responsive layout for all screen sizes

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Kotlin | Primary programming language |
| Jetpack Compose | Modern UI toolkit |
| Firebase Auth | Authentication |
| Firebase Realtime Database | Real-time data sync |
| Firebase Firestore | Secondary database |
| Firebase Cloud Messaging | Push notifications |
| Firebase Storage | File storage (fallback) |
| Supabase | Profile images, media storage |
| Coil | Image loading |
| ExoPlayer | Video playback |
| Accompanist | Permissions, system UI |

## Project Structure

```
EBChat/
├── app/
│   ├── src/main/java/com/ebchat/
│   │   ├── MainActivity.kt              # Main entry point
│   │   ├── EBChatApplication.kt         # Application class
│   │   ├── data/
│   │   │   ├── model/                   # Data models
│   │   │   │   ├── User.kt
│   │   │   │   ├── Message.kt
│   │   │   │   ├── Post.kt
│   │   │   │   ├── Story.kt
│   │   │   │   ├── Group.kt
│   │   │   │   └── Notification.kt
│   │   │   └── remote/
│   │   │       ├── FirebaseConfig.kt    # Firebase setup
│   │   │       └── SupabaseClient.kt    # Supabase setup
│   │   ├── service/
│   │   │   ├── EBChatMessagingService.kt # FCM service
│   │   │   └── NotificationReceiver.kt   # Notification actions
│   │   ├── ui/
│   │   │   ├── navigation/
│   │   │   │   └── Screen.kt            # Navigation routes
│   │   │   ├── theme/
│   │   │   │   ├── Theme.kt             # App themes
│   │   │   │   └── Type.kt              # Typography
│   │   │   └── screens/
│   │   │       ├── auth/                # Auth screens
│   │   │       │   ├── OnboardingScreen.kt
│   │   │       │   ├── LoginScreen.kt
│   │   │       │   └── SignupScreen.kt
│   │   │       ├── main/                # Main screens
│   │   │       │   ├── MainScreen.kt
│   │   │       │   ├── ChatListScreen.kt
│   │   │       │   ├── ChatDetailScreen.kt
│   │   │       │   ├── GroupsScreen.kt
│   │   │       │   └── StoryViewerScreen.kt
│   │   │       ├── post/                # Post screens
│   │   │       │   ├── PostFeedScreen.kt
│   │   │       │   ├── CreatePostScreen.kt
│   │   │       │   └── PostDetailScreen.kt
│   │   │       ├── profile/             # Profile screens
│   │   │       │   ├── ProfileScreen.kt
│   │   │       │   └── EditProfileScreen.kt
│   │   │       ├── search/              # Search screen
│   │   │       │   └── SearchScreen.kt
│   │   │       └── settings/            # Settings screens
│   │   │           ├── SettingsScreen.kt
│   │   │           ├── ThemeScreen.kt
│   │   │           └── OtherSettingsScreens.kt
│   │   └── ...
│   ├── src/main/res/                    # Android resources
│   ├── build.gradle.kts                 # App build config
│   └── google-services.json             # Firebase config
├── cloudflare-worker/                    # Cloudflare Worker
│   ├── worker.js                         # Push notification worker
│   └── wrangler.toml                     # Worker config
├── .github/workflows/
│   └── android.yml                       # CI/CD pipeline
├── build.gradle.kts                      # Project build config
├── settings.gradle.kts
└── README.md
```

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or higher
- Android SDK 34
- Firebase account
- Supabase account
- Cloudflare account (for push notifications)

### Step 1: Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/EBChat.git
cd EBChat
```

### Step 2: Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing `chat-4e1d0`
3. Add an Android app with package name `com.ebchat`
4. Download `google-services.json` and place it in `app/`
5. Enable the following Firebase services:
   - **Authentication**: Enable Email/Password and Google Sign-In
   - **Realtime Database**: Create database with rules (see below)
   - **Firestore Database**: Create database
   - **Cloud Messaging**: Enable and note the Server Key
   - **Storage**: Create default bucket

#### Firebase Realtime Database Rules
```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "auth != null",
        ".write": "auth != null && auth.uid == $uid"
      }
    },
    "messages": {
      "$chatId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    },
    "chats": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "stories": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "groups": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "posts": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "comments": {
      "$postId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    },
    "typing": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

### Step 3: Supabase Setup

1. Go to [Supabase Dashboard](https://app.supabase.io/)
2. Create a new project or use existing
3. Go to **Storage** and create these buckets:
   - `profile-images` (public)
   - `message-media` (public)
   - `story-media` (public)
   - `post-media` (public)
   - `audio-files` (public)
   - `group-images` (public)
4. Copy your Project URL and Anon Key
5. Update `SupabaseClient.kt` with your credentials (already configured for provided credentials)

### Step 4: Cloudflare Worker Setup (Push Notifications)

1. Install Wrangler CLI:
```bash
npm install -g wrangler
```

2. Authenticate with Cloudflare:
```bash
wrangler login
```

3. Create KV Namespace:
```bash
wrangler kv:namespace create "EBCHAT_TOKENS"
```

4. Update `wrangler.toml` with your KV namespace ID

5. Set the Firebase Private Key as a secret:
```bash
wrangler secret put FIREBASE_PRIVATE_KEY
# Paste the private key from your Firebase service account (without \n characters)
```

6. Deploy the worker:
```bash
cd cloudflare-worker
wrangler deploy
```

### Step 5: Build and Run

1. Open the project in Android Studio
2. Sync Gradle files
3. Connect your Android device (Android 8+ / API 26+)
4. Click **Run** (Shift+F10)

## Configuration Files

### Supabase Configuration (app/src/main/java/com/ebchat/data/remote/SupabaseClient.kt)
```kotlin
const val SUPABASE_URL = "https://srfztgcdejfaesrvkarg.supabase.co"
const val SUPABASE_KEY = "sb_publishable_BcH2xwywnUCVG48LYjPOLQ_8-y2InGA"
```

### Firebase Configuration (app/google-services.json)
Already included with project credentials. Replace with your own if creating a new Firebase project.

### Cloudflare Worker Configuration (cloudflare-worker/wrangler.toml)
Update the KV namespace ID after creating it in your Cloudflare dashboard.

## Firebase Cloud Messaging Setup

### For Push Notifications to Work:

1. **FCM Token**: The app automatically retrieves and stores the FCM token in the user's Realtime Database node (`users/{uid}/fcmToken`)

2. **Sending Notifications**: Use the Cloudflare Worker endpoints:
   - `POST /send` - Send to single device
   - `POST /send-multicast` - Send to multiple devices
   - `POST /send-topic` - Send to topic subscribers

3. **Notification Features**:
   - Reply from notification
   - Mark as read from notification
   - Custom notification sound
   - Vibration patterns
   - LED notification light (pink)

### Required Firebase Admin SDK Setup

To send notifications from your backend or Cloudflare Worker, you need the Firebase Admin SDK service account. The service account credentials are provided in the project configuration.

## GitHub Actions CI/CD

The project includes a GitHub Actions workflow (`.github/workflows/android.yml`) that:
- Builds the debug APK on every push and pull request
- Uploads the APK as an artifact

### Setup GitHub Actions:
1. Push code to GitHub repository
2. The workflow runs automatically
3. Download APK from Actions tab

### Create GitHub Repository:
```bash
git init
git add .
git commit -m "Initial commit - EBChat Android App"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/EBChat.git
git push -u origin main
```

## Cloudflare Worker API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` or `/health` | GET | Health check |
| `/send` | POST | Send to single device |
| `/send-multicast` | POST | Send to multiple devices |
| `/send-topic` | POST | Send to FCM topic |
| `/register-token` | POST | Register device token |
| `/unregister-token` | POST | Unregister device token |

### Example: Send Notification
```bash
curl -X POST https://ebchat-notifications.YOUR_SUBDOMAIN.workers.dev/send \
  -H "Content-Type: application/json" \
  -d '{
    "token": "DEVICE_FCM_TOKEN",
    "title": "New Message",
    "body": "You have a new message!",
    "data": {
      "type": "message",
      "chatId": "chat_123",
      "senderId": "user_123"
    }
  }'
```

## Troubleshooting

### Build Issues
- **Gradle sync fails**: Make sure you're using JDK 17
- **Missing google-services.json**: Download from Firebase Console and place in `app/`
- **Dependency conflicts**: Run `./gradlew app:dependencies` to check

### Notification Issues
- **Notifications not received**: Check FCM token is saved in database
- **Worker deployment fails**: Verify `FIREBASE_PRIVATE_KEY` secret is set
- **CORS errors**: Worker already includes CORS headers

### Runtime Issues
- **App crashes on launch**: Check Firebase configuration and internet permission
- **Images not loading**: Verify Supabase bucket permissions are set to public
- **Messages not sending**: Check Realtime Database rules allow write

## License

This project is proprietary and confidential. All rights reserved.

## Credits

Developed with love for the EBChat community.
