package com.vishesh.techno_attendance

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.FileNotFoundException
import com.vishesh.techno_attendance.ui.theme.Techno_AttendanceTheme
import org.json.JSONObject
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val LOGIN_URL = "http://103.159.68.35:3535/auth/student/login"
private const val DASHBOARD_URL = "http://103.159.68.35:3535/student"

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    // Credential storage keys
    private companion object {
        const val PREFS_NAME = "attendance_credentials"
        const val KEY_ROLL_NUMBER = "rollNumber"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_REMEMBER_CREDENTIALS = "rememberCredentials"
        const val KEY_SETUP_COMPLETE = "isSetupComplete"
        const val CONFIG_FILE_NAME = "login_config.json"
    }

    // JavaScript-to-Kotlin Bridge for Pop-ups
    inner class WebAppInterface {
        @JavascriptInterface
        fun showToast(message: String) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        enableEdgeToEdge()

        setContent {
            Techno_AttendanceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Check if setup is complete
                    val isSetupComplete = sharedPreferences.getBoolean(KEY_SETUP_COMPLETE, false)
                    
                    if (isSetupComplete) {
                        AttendanceApp()
                    } else {
                        CredentialSetupScreen(
                            onSetupComplete = { rollNumber, email, password ->
                                saveCredentialsToFile(rollNumber, email, password)
                                sharedPreferences.edit()
                                    .putBoolean(KEY_SETUP_COMPLETE, true)
                                    .apply()
                            }
                        )
                    }
                }
            }
        }
    }

    // ## Signature Generation Functions ##
    // ===================================

    /**
     * Creates an HMAC-SHA256 hash.
     * @param key The secret key for hashing.
     * @param data The data (timestamp) to hash.
     * @return A hexadecimal string representation of the hash.
     */
    private fun hmacSha256(key: String, data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "HmacSHA256")
        mac.init(secretKeySpec)
        val hashBytes = mac.doFinal(data.toByteArray())
        return hashBytes.fold("") { str, it -> str + "%02x".format(it) }
    }

    /**
     * Generates the complete "x-app-signature" header value.
     * It combines the current timestamp with its HMAC-SHA256 hash.
     * @return The formatted signature string "timestamp.hash".
     */
    private fun generateAppSignature(): String {
        // The secret key discovered from the web app's code
        val secretKey = "6ECD762D4776742AFFB192CE8A148"
        val timestamp = System.currentTimeMillis().toString()
        val hash = hmacSha256(secretKey, timestamp)
        return "$timestamp.$hash"
    }

    // ## UI and WebView Logic ##
    // ========================

    @Composable
    private fun CredentialSetupScreen(onSetupComplete: (String, String, String) -> Unit) {
        var rollNumber by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var showAttendanceApp by remember { mutableStateOf(false) }
        var isVisible by remember { mutableStateOf(false) }

        // Trigger animation on first composition
        LaunchedEffect(Unit) {
            isVisible = true
        }

        if (showAttendanceApp) {
            AttendanceApp()
            return
        }

        // Modern gradient background
        val gradientBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF667eea),
                Color(0xFF764ba2),
                Color(0xFF6B73FF)
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush),
            contentAlignment = Alignment.Center
        ) {
            // Animated floating elements in background
            FloatingElements()
            
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(800, easing = EaseOutCubic)
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = Color.Black.copy(alpha = 0.1f),
                            spotColor = Color.Black.copy(alpha = 0.1f)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Modern header with icon
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF667eea),
                                            Color(0xFF764ba2)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Setup",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Modern title with gradient text effect
                        Text(
                            text = "Welcome to Auto Attendance",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            textAlign = TextAlign.Center,
                            color = Color(0xFF2D3748)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "One-Time Setup",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF667eea),
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Modern description with better styling
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF7FAFC)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Security",
                                    tint = Color(0xFF667eea),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Your credentials are encrypted and stored securely on your device. To reset, simply reinstall the app.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF4A5568),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Modern input fields
                        ModernTextField(
                            value = rollNumber,
                            onValueChange = { rollNumber = it },
                            label = "Roll Number",
                            icon = Icons.Default.Person,
                            enabled = !isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        ModernTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email Address",
                            icon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            enabled = !isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        ModernTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Password",
                            icon = Icons.Default.Lock,
                            isPassword = true,
                            enabled = !isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(40.dp))
                        
                        // Modern gradient button
                        val isFormValid = rollNumber.isNotBlank() && email.isNotBlank() && password.isNotBlank()
                        
                        Button(
                            onClick = {
                                if (isFormValid) {
                                    isLoading = true
                                    onSetupComplete(rollNumber.trim(), email.trim(), password.trim())
                                    showAttendanceApp = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(
                                    elevation = if (isFormValid && !isLoading) 12.dp else 0.dp,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            enabled = !isLoading && isFormValid,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color(0xFFE2E8F0)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isFormValid && !isLoading) {
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF667eea),
                                                    Color(0xFF764ba2)
                                                )
                                            )
                                        } else {
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFFE2E8F0),
                                                    Color(0xFFE2E8F0)
                                                )
                                            )
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Setting up...",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "🚀 Save & Start Journey",
                                        color = if (isFormValid) Color.White else Color(0xFF9CA3AF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ModernTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        icon: ImageVector,
        keyboardType: KeyboardType = KeyboardType.Text,
        isPassword: Boolean = false,
        enabled: Boolean = true
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { 
                Text(
                    text = label,
                    color = Color(0xFF6B7280)
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (value.isNotEmpty()) Color(0xFF667eea) else Color(0xFF9CA3AF)
                )
            },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (value.isNotEmpty()) 2.dp else 1.dp,
                    color = if (value.isNotEmpty()) Color(0xFF667eea) else Color(0xFFE5E7EB),
                    shape = RoundedCornerShape(12.dp)
                ),
            singleLine = true,
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedContainerColor = Color(0xFFF9FAFB),
                unfocusedContainerColor = Color(0xFFF9FAFB),
                disabledContainerColor = Color(0xFFF3F4F6),
                focusedTextColor = Color(0xFF1F2937),
                unfocusedTextColor = Color(0xFF374151)
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }

    @Composable
    private fun FloatingElements() {
        val infiniteTransition = rememberInfiniteTransition(label = "floating")
        
        // Floating circle 1
        val offset1 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 30f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offset1"
        )
        
        // Floating circle 2
        val offset2 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -25f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offset2"
        )
        
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = 100.dp + offset1.dp)
                .size(120.dp)
                .background(
                    Color.White.copy(alpha = 0.1f),
                    CircleShape
                )
        )
        
        Box(
            modifier = Modifier
                .offset(x = 250.dp, y = (-80).dp + offset2.dp)
                .size(80.dp)
                .background(
                    Color.White.copy(alpha = 0.08f),
                    CircleShape
                )
        )
        
        Box(
            modifier = Modifier
                .offset(x = 300.dp, y = 400.dp + offset1.dp)
                .size(60.dp)
                .background(
                    Color.White.copy(alpha = 0.06f),
                    CircleShape
                )
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AttendanceApp() {
        var webView: WebView? by remember { mutableStateOf(null) }
        var isLoading by remember { mutableStateOf(true) }

        BackHandler(enabled = true) {
            val currentUrl = webView?.url
            val isOnMainDashboard = currentUrl == DASHBOARD_URL || currentUrl == "$DASHBOARD_URL/"

            when {
                isOnMainDashboard -> this@MainActivity.moveTaskToBack(true)
                webView?.canGoBack() == true -> webView?.goBack()
                else -> this@MainActivity.moveTaskToBack(true)
            }
        }

        Scaffold(
            topBar = {
                if (!isLoading) {
                    TopAppBar(
                        title = { Text("Attendance") },
                        actions = {
                            TextButton(onClick = { webView?.reload() }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Refresh")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.Refresh, "Refresh")
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        createWebView(
                            context = context,
                            onWebViewCreated = { createdWebView -> webView = createdWebView },
                            onDashboardLoaded = { isLoading = false }
                        )
                    }
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Logging in, please wait...")
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(context: Context, onWebViewCreated: (WebView) -> Unit, onDashboardLoaded: () -> Unit): WebView {
        return WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(AndroidColor.WHITE)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)

            // Enable JavaScript-to-Kotlin bridge
            addJavascriptInterface(WebAppInterface(), "Android")

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                    consoleMessage?.let { Log.d("WebView", it.message()) }
                    return true
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false
                    val currentUrl = view?.url
                    
                    return when {
                        // Rule 1: Block Techno NJR LMS with toast
                        url == "http://njrlms.technonjr.org/" -> {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "This app is only for attendance purposes", Toast.LENGTH_SHORT).show()
                            }
                            true // Block navigation
                        }
                        // Block password change page
                        url == "http://103.159.68.35:3535/student/change-password" -> {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "You cant reset password through this app", Toast.LENGTH_SHORT).show()
                            }
                            true // Block navigation
                        }
                        // Block redirection from student page to home page
                        (currentUrl == "http://103.159.68.35:3535/student" || currentUrl == "http://103.159.68.35:3535/student/") && 
                        (url == "http://103.159.68.35:3535/" || url == "http://103.159.68.35:3535") -> {
                            true // Block navigation silently
                        }
                        // Allow all other navigation
                        else -> false
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (url == DASHBOARD_URL || url == "$DASHBOARD_URL/") {
                        onDashboardLoaded()
                        // Inject interaction blockers only on dashboard
                        injectInteractionBlockers(view)
                    }
                    val credentials = getStoredCredentials()
                    injectSimpleApiLogin(view, credentials.first, credentials.second, credentials.third)
                }
            }

            onWebViewCreated(this)
            loadUrl(LOGIN_URL)
        }
    }

    private fun injectSimpleApiLogin(webView: WebView?, rollNumber: String, email: String, password: String) {
        if (webView == null) return

        // Generate a fresh, dynamic signature for every login attempt
        val appSignature = generateAppSignature()
        Log.d("Auth", "Generated Signature: $appSignature")

        val simpleApiScript = """
            (function() {
                if (window.autoLoginExecuted) { return; }
                window.autoLoginExecuted = true;
                if (!window.location.href.includes('login')) { return; }

                console.log('Auto-login script started.');
                const xhr = new XMLHttpRequest();
                xhr.open('POST', 'http://103.159.68.35:3536/api/student/auth/login', true);
                xhr.setRequestHeader('Content-Type', 'application/json');
                
                // Use the dynamically generated signature from the Kotlin code
                xhr.setRequestHeader('x-app-signature', '$appSignature');

                xhr.onreadystatechange = function() {
                    if (xhr.readyState === 4) {
                        console.log('API Response Status: ' + xhr.status);
                        if (xhr.status === 200) {
                            try {
                                const data = JSON.parse(xhr.responseText);
                                if (data.token && data.student && data.student.id) {
                                    localStorage.setItem('studentToken', data.token);
                                    localStorage.setItem('studentId', data.student.id);
                                    localStorage.setItem('userType', 'student');
                                    console.log('Login successful. Redirecting...');
                                    window.location.href = '$DASHBOARD_URL';
                                } else {
                                    console.error('API Error: Missing token or student data.');
                                }
                            } catch (e) {
                                console.error('Parse Error: ' + e.message);
                            }
                        }
                    }
                };
                xhr.onerror = function() { console.error('Network Error.'); };
                
                const requestData = {
                    rollNumber: '$rollNumber',
                    email: '$email',
                    password: '$password'
                };
                xhr.send(JSON.stringify(requestData));
            })();
        """.trimIndent()

        webView.postDelayed({
            webView.evaluateJavascript(simpleApiScript, null)
        }, 1500)
    }

    private fun injectInteractionBlockers(webView: WebView?) {
        if (webView == null) return

        val blockingScript = """
            (function() {
                if (window.interactionBlockersInjected) { return; }
                window.interactionBlockersInjected = true;
                
                console.log('Injecting interaction blockers...');
                
                // Function to find and block logout button
                function blockLogoutButton() {
                    const buttons = document.querySelectorAll('button');
                    buttons.forEach(button => {
                        if (button.textContent && button.textContent.trim().toLowerCase() === 'logout') {
                            console.log('Found logout button, adding blocker...');
                            button.addEventListener('click', function(event) {
                                event.preventDefault();
                                event.stopPropagation();
                                Android.showToast('You cannot log out from this app');
                            }, true);
                        }
                    });
                }
                
                // Function to block problematic links
                function blockProblematicLinks() {
                    // Block Techno NJR LMS links
                    const njrLinks = document.querySelectorAll('a[href="http://njrlms.technonjr.org/"]');
                    njrLinks.forEach(link => {
                        console.log('Found Techno NJR LMS link, adding blocker...');
                        link.addEventListener('click', function(event) {
                            event.preventDefault();
                            event.stopPropagation();
                            Android.showToast('This app is only for attendance purposes');
                        }, true);
                    });
                    
                    // Block password change links
                    const passwordLinks = document.querySelectorAll('a[href="http://103.159.68.35:3535/student/change-password"]');
                    passwordLinks.forEach(link => {
                        console.log('Found password change link, adding blocker...');
                        link.addEventListener('click', function(event) {
                            event.preventDefault();
                            event.stopPropagation();
                            Android.showToast('You cant reset password through this app');
                        }, true);
                    });
                    
                    // Also check for relative links
                    const relativePasswordLinks = document.querySelectorAll('a[href="/student/change-password"]');
                    relativePasswordLinks.forEach(link => {
                        console.log('Found relative password change link, adding blocker...');
                        link.addEventListener('click', function(event) {
                            event.preventDefault();
                            event.stopPropagation();
                            Android.showToast('You cant reset password through this app');
                        }, true);
                    });
                }
                
                // Function to block logo and home page redirections
                function blockHomePageRedirections() {
                    // Block all links that redirect to home page
                    const homeLinks = document.querySelectorAll('a[href="http://103.159.68.35:3535/"], a[href="http://103.159.68.35:3535"], a[href="/"], a[href="../"]');
                    homeLinks.forEach(link => {
                        console.log('Found home page link, adding blocker...');
                        link.addEventListener('click', function(event) {
                            event.preventDefault();
                            event.stopPropagation();
                        }, true);
                    });
                    
                    // Block logo images that might redirect to home
                    const logoImages = document.querySelectorAll('img');
                    logoImages.forEach(img => {
                        const parent = img.parentElement;
                        if (parent && parent.tagName === 'A') {
                            const href = parent.getAttribute('href');
                            if (href === 'http://103.159.68.35:3535/' || href === 'http://103.159.68.35:3535' || href === '/' || href === '../') {
                                console.log('Found logo with home redirect, adding blocker...');
                                parent.addEventListener('click', function(event) {
                                    event.preventDefault();
                                    event.stopPropagation();
                                }, true);
                            }
                        }
                        
                        // Also block direct click events on images that might have JavaScript redirects
                        img.addEventListener('click', function(event) {
                            // Check if this might be a logo (common logo characteristics)
                            if (img.src && (img.src.includes('logo') || img.alt && img.alt.toLowerCase().includes('logo'))) {
                                console.log('Potentially blocking logo click...');
                                event.preventDefault();
                                event.stopPropagation();
                            }
                        }, true);
                    });
                    
                    // Block any element with onclick that tries to navigate to home
                    const clickableElements = document.querySelectorAll('[onclick]');
                    clickableElements.forEach(element => {
                        const onclick = element.getAttribute('onclick');
                        if (onclick && (onclick.includes('http://103.159.68.35:3535/') || onclick.includes('window.location') || onclick.includes('location.href'))) {
                            console.log('Found element with suspicious onclick, adding blocker...');
                            element.addEventListener('click', function(event) {
                                event.preventDefault();
                                event.stopPropagation();
                            }, true);
                        }
                    });
                }
                
                // Run all functions immediately
                blockLogoutButton();
                blockProblematicLinks();
                blockHomePageRedirections();
                
                // Also run when DOM changes (for dynamically loaded content)
                const observer = new MutationObserver(function(mutations) {
                    blockLogoutButton();
                    blockProblematicLinks();
                    blockHomePageRedirections();
                });
                
                observer.observe(document.body, {
                    childList: true,
                    subtree: true
                });
                
            })();
        """.trimIndent()

        webView.postDelayed({
            webView.evaluateJavascript(blockingScript, null)
        }, 500)
    }

    // ## Credential Management ##
    // =========================

    private fun saveCredentialsToFile(rollNumber: String, email: String, password: String) {
        try {
            val credentialsJson = JSONObject().apply {
                put("credentials", JSONObject().apply {
                    put("rollNumber", rollNumber)
                    put("email", email)
                    put("password", password)
                })
                put("urls", JSONObject().apply {
                    put("loginUrl", LOGIN_URL)
                    put("dashboardUrl", DASHBOARD_URL)
                })
                put("settings", JSONObject().apply {
                    put("autoLoginEnabled", true)
                    put("loginDelay", 2000)
                    put("retryAttempts", 3)
                })
            }

            openFileOutput(CONFIG_FILE_NAME, Context.MODE_PRIVATE).use { output ->
                output.write(credentialsJson.toString().toByteArray())
            }
            Log.d("MainActivity", "Credentials saved to internal storage.")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to save credentials: ${e.message}")
        }
    }

    private fun getStoredCredentials(): Triple<String, String, String> {
        // First try to read from internal storage
        try {
            openFileInput(CONFIG_FILE_NAME).bufferedReader().use {
                val config = JSONObject(it.readText()).getJSONObject("credentials")
                val roll = config.getString("rollNumber")
                val mail = config.getString("email")
                val pass = config.getString("password")
                Log.d("MainActivity", "Loaded credentials from internal storage.")
                return Triple(roll, mail, pass)
            }
        } catch (e: FileNotFoundException) {
            Log.d("MainActivity", "Internal config file not found.")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error reading internal config: ${e.message}")
        }

        // Fallback to assets (for backward compatibility during development)
        try {
            assets.open("login_config.json").bufferedReader().use {
                val config = JSONObject(it.readText()).getJSONObject("credentials")
                val roll = config.getString("rollNumber")
                val mail = config.getString("email")
                val pass = config.getString("password")
                Log.d("MainActivity", "Loaded credentials from assets (fallback).")
                return Triple(roll, mail, pass)
            }
        } catch (e: Exception) {
            Log.d("MainActivity", "Assets config file not found.")
        }

        // Final fallback to SharedPreferences (should not be needed with new flow)
        val rollNumber = sharedPreferences.getString(KEY_ROLL_NUMBER, "") ?: ""
        val email = sharedPreferences.getString(KEY_EMAIL, "") ?: ""
        val password = sharedPreferences.getString(KEY_PASSWORD, "") ?: ""
        Log.d("MainActivity", "Using SharedPreferences fallback.")
        return Triple(rollNumber, email, password)
    }
}