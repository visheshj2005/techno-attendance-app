# Techno Attendance App

An Android WebView application for Techno NJR College students to automatically access their attendance dashboard with seamless login functionality.

## 🎯 What This App Does

This app provides a streamlined way for Techno NJR College students to check their attendance without manually logging in every time. It features:

- **One-time setup** with secure credential storage
- **Automatic API-based login** using the college's authentication system
- **Direct dashboard access** to view attendance records
- **Modern Material Design 3 UI** with smooth animations
- **Secure credential management** with encrypted storage

## ✨ Key Features

### 🔐 Smart Authentication
- **API-based login**: Uses the college's official API endpoint for authentication
- **HMAC-SHA256 signatures**: Implements proper request signing for security
- **Token management**: Handles authentication tokens automatically
- **Fallback mechanisms**: Multiple login strategies for reliability

### 🎨 Modern UI/UX
- **Material Design 3**: Clean, modern interface
- **Smooth animations**: Engaging setup flow with floating elements and transitions
- **One-time setup**: Beautiful onboarding screen for credential configuration
- **Loading states**: Clear feedback during login and navigation

### 🛡️ Security & Privacy
- **Local storage only**: Credentials never leave your device
- **Encrypted preferences**: Uses Android's secure SharedPreferences
- **Domain restrictions**: Only navigates to official college URLs
- **No external tracking**: No data sent to third-party servers

### 🚫 Smart Restrictions
- **LMS blocking**: Prevents navigation to LMS (study-only app)
- **Password change blocking**: Redirects password changes to official channels
- **Attendance focus**: Keeps the app focused on its primary purpose

## 🏗️ Technical Architecture

### Authentication Flow
1. **Initial Setup**: User enters credentials once through the setup screen
2. **API Login**: App uses the college's official API with proper signatures
3. **Token Storage**: Authentication tokens are securely stored
4. **Dashboard Access**: Direct navigation to attendance dashboard

### API Integration
- **Endpoint**: `http://103.159.68.35:3536/api/student/auth/login`
- **Method**: POST with JSON payload
- **Security**: HMAC-SHA256 request signing
- **Headers**: Proper CORS and authentication headers

### WebView Configuration
- **JavaScript enabled**: For dynamic content interaction
- **Cookie management**: Maintains session state
- **DOM storage**: Supports modern web app features
- **Console logging**: Debug support for troubleshooting

## 📱 Setup & Configuration

### First-Time Setup
1. Install the APK on your Android device
2. Launch the app - you'll see the setup screen
3. Enter your college credentials:
   - **Roll Number**: Your student roll number
   - **Email**: Your college email address
   - **Password**: Your college portal password
4. Tap "Save & Start Journey"

### Configuration File (Advanced)
For developers or advanced users, you can modify `app/src/main/assets/login_config.json`:

```json
{
    "credentials": {
        "rollNumber": "your_roll_number",
        "email": "your_email@technonjr.org",
        "password": "your_password"
    },
    "urls": {
        "loginUrl": "http://103.159.68.35:3535/auth/student/login",
        "dashboardUrl": "http://103.159.68.35:3535/student"
    },
    "settings": {
        "autoLoginEnabled": true,
        "loginDelay": 2000,
        "retryAttempts": 3
    }
}
```

## 🔧 Development

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+ (Android 7.0)
- Kotlin 1.9+
- Gradle 8.11+

### Building
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

### Project Structure
```
app/
├── src/main/
│   ├── java/com/vishesh/techno_attendance/
│   │   └── MainActivity.kt          # Main app logic
│   ├── assets/
│   │   ├── login_config.json        # Configuration file
│   │   └── debug_script.js          # Debug utilities
│   └── res/                         # Android resources
├── build.gradle.kts                 # App-level build config
└── proguard-rules.pro              # Code obfuscation rules
```

### Key Components
- **MainActivity.kt**: Main application logic with Jetpack Compose UI
- **WebView Integration**: Custom WebView client with login automation
- **Credential Management**: Secure storage using SharedPreferences
- **API Authentication**: HMAC-SHA256 signature generation

## 🐛 Troubleshooting

### Common Issues

**App won't log in?**
- Verify your credentials are correct
- Check if the college servers are accessible
- Look for error messages in the loading screen

**Setup screen keeps appearing?**
- Ensure all fields are filled correctly
- Check if credentials are being saved properly
- Try reinstalling the app if issues persist

**Dashboard not loading?**
- Check your internet connection
- Verify the college portal is accessible in a browser
- Try the refresh button in the top bar

### Debug Mode
The app includes debug scripts for troubleshooting. Check the WebView console logs for detailed information about the login process.

## 🔒 Security Considerations

- **Local Storage**: All credentials are stored locally on your device
- **Encryption**: Modern Android versions encrypt SharedPreferences automatically
- **No Cloud Sync**: Credentials never leave your device
- **Domain Restrictions**: App only connects to official college domains
- **Regular Updates**: Keep the app updated for security patches

## 📄 License & Disclaimer

This is an unofficial app created for student convenience. It is not affiliated with Techno NJR College. Use at your own discretion and ensure you comply with your institution's IT policies.

## 🤝 Contributing

This project is open for contributions. Feel free to:
- Report bugs or issues
- Suggest new features
- Submit pull requests
- Improve documentation

---

**Made with ❤️ for Techno NJR College students**