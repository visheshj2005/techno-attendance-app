(function () {
    // Enhanced debug popup function
    function showAlert(title, message) {
        alert('🔍 DEBUG: ' + title + '\n\n' + message);
    }

    showAlert('SCRIPT START', 'Debug script injected!\nURL: ' + window.location.href + '\nUser Agent: ' + navigator.userAgent);

    // Prevent multiple executions
    if (window.autoLoginExecuted) {
        showAlert('SKIP', 'Auto-login already executed');
        return;
    }
    window.autoLoginExecuted = true;

    // Check if on login page
    if (!window.location.href.includes('login')) {
        showAlert('SKIP', 'Not on login page\nCurrent URL: ' + window.location.href);
        return;
    }

    // Show environment info
    let envInfo = 'Environment Info:\n';
    envInfo += 'Origin: ' + window.location.origin + '\n';
    envInfo += 'Protocol: ' + window.location.protocol + '\n';
    envInfo += 'Host: ' + window.location.host + '\n';
    envInfo += 'Cookies: ' + (document.cookie ? 'YES' : 'NO') + '\n';
    envInfo += 'Local Storage: ' + (typeof (Storage) !== "undefined" ? 'YES' : 'NO');

    showAlert('ENVIRONMENT', envInfo);

    // Check for existing cookies/tokens
    let cookieInfo = 'Cookie Analysis:\n';
    cookieInfo += 'All Cookies: ' + document.cookie + '\n';
    cookieInfo += 'LocalStorage Keys: ' + Object.keys(localStorage).join(', ');

    showAlert('COOKIES', cookieInfo);

    showAlert('START LOGIN', 'Starting API login process...\nRoll: ROLL_NUMBER\nEmail: EMAIL\nPassword length: PASSWORD_LENGTH');

    // Prepare request data
    const requestData = {
        rollNumber: 'ROLL_NUMBER',
        email: 'EMAIL',
        password: 'PASSWORD'
    };

    // Show request details
    let requestInfo = 'Request Details:\n';
    requestInfo += 'URL: http://103.159.68.35:3536/api/student/auth/login\n';
    requestInfo += 'Method: POST\n';
    requestInfo += 'Content-Type: application/json\n';
    requestInfo += 'Body: ' + JSON.stringify(requestData, null, 2);

    showAlert('REQUEST INFO', requestInfo);

    // Enhanced headers for 403 fix
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Origin': window.location.origin,
        'Referer': window.location.href,
        'X-Requested-With': 'XMLHttpRequest',
        'Cache-Control': 'no-cache'
    };

    // Show headers being sent
    let headerInfo = 'Headers Being Sent:\n';
    for (let key in headers) {
        headerInfo += key + ': ' + headers[key] + '\n';
    }

    showAlert('HEADERS', headerInfo);

    // Make API call with enhanced error handling
    fetch('http://103.159.68.35:3536/api/student/auth/login', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(requestData),
        credentials: 'include',
        mode: 'cors'
    })
        .then(response => {
            // Detailed response analysis
            let responseInfo = 'Response Details:\n';
            responseInfo += 'Status: ' + response.status + '\n';
            responseInfo += 'OK: ' + response.ok + '\n';
            responseInfo += 'Status Text: ' + response.statusText + '\n';
            responseInfo += 'Type: ' + response.type + '\n';
            responseInfo += 'URL: ' + response.url + '\n';
            responseInfo += 'Redirected: ' + response.redirected + '\n\n';

            // Response headers
            responseInfo += 'Response Headers:\n';
            for (let [key, value] of response.headers.entries()) {
                responseInfo += key + ': ' + value + '\n';
            }

            showAlert('RESPONSE DETAILS', responseInfo);

            // Handle 403 specifically
            if (response.status === 403) {
                let forbiddenInfo = '403 FORBIDDEN Analysis:\n\n';
                forbiddenInfo += 'Common causes:\n';
                forbiddenInfo += '1. CORS policy blocking WebView\n';
                forbiddenInfo += '2. Missing authentication headers\n';
                forbiddenInfo += '3. Server blocking mobile user agents\n';
                forbiddenInfo += '4. Rate limiting or IP blocking\n';
                forbiddenInfo += '5. Missing CSRF token\n';
                forbiddenInfo += '6. Wrong Content-Type header\n\n';
                forbiddenInfo += 'Current User Agent:\n' + navigator.userAgent;

                showAlert('403 ANALYSIS', forbiddenInfo);

                // Try to get response text even for 403
                return response.text().then(text => {
                    showAlert('403 RESPONSE BODY', 'Response Text:\n' + text);
                    throw new Error('HTTP 403: ' + response.statusText + '\nResponse: ' + text);
                });
            }

            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error('HTTP ' + response.status + ': ' + response.statusText + '\nResponse: ' + text);
                });
            }

            return response.json();
        })
        .then(data => {
            let msg = 'API SUCCESS!\n\n';
            msg += 'Full Response:\n' + JSON.stringify(data, null, 2) + '\n\n';
            msg += 'Token: ' + (data.token ? 'YES (' + data.token.substring(0, 30) + '...)' : 'NO') + '\n';
            msg += 'Student: ' + (data.student ? 'YES' : 'NO') + '\n';

            if (data.student) {
                msg += 'Name: ' + (data.student.name || 'N/A') + '\n';
                msg += 'Roll: ' + (data.student.rollNumber || 'N/A');
            }

            showAlert('API SUCCESS', msg);

            if (data.token) {
                // Store token for debugging
                localStorage.setItem('debugToken', data.token);
                localStorage.setItem('debugStudent', JSON.stringify(data.student));

                showAlert('TOKEN STORED', 'Token stored in localStorage!\nRedirecting to dashboard in 3 seconds...');

                setTimeout(() => {
                    showAlert('REDIRECTING', 'Now redirecting to:\nhttp://103.159.68.35:3535/student');
                    window.location.href = 'http://103.159.68.35:3535/student';
                }, 3000);
            } else {
                showAlert('ERROR', 'No token in response!\nFull response:\n' + JSON.stringify(data, null, 2));
            }
        })
        .catch(error => {
            let errorMsg = 'API CALL FAILED!\n\n';
            errorMsg += 'Error Message: ' + error.message + '\n';
            errorMsg += 'Error Name: ' + error.name + '\n';
            errorMsg += 'Error Stack: ' + (error.stack || 'No stack trace') + '\n\n';

            if (error.message.includes('403')) {
                errorMsg += '🚨 403 FORBIDDEN SOLUTIONS:\n\n';
                errorMsg += '1. Try different User-Agent\n';
                errorMsg += '2. Add authentication headers\n';
                errorMsg += '3. Check CORS settings\n';
                errorMsg += '4. Verify API endpoint\n';
                errorMsg += '5. Check if IP is blocked\n';
                errorMsg += '6. Try from regular browser first';
            } else if (error.message.includes('network') || error.message.includes('fetch')) {
                errorMsg += 'Network Error Causes:\n';
                errorMsg += '- No internet connection\n';
                errorMsg += '- Server is down\n';
                errorMsg += '- DNS resolution failed\n';
                errorMsg += '- Firewall blocking request';
            }

            showAlert('DETAILED ERROR', errorMsg);

            // Additional debugging for 403
            if (error.message.includes('403')) {
                // Try to analyze the login form
                let formAnalysis = 'Login Form Analysis:\n\n';

                const forms = document.querySelectorAll('form');
                formAnalysis += 'Forms found: ' + forms.length + '\n';

                forms.forEach((form, index) => {
                    formAnalysis += 'Form ' + (index + 1) + ':\n';
                    formAnalysis += '  Action: ' + (form.action || 'No action') + '\n';
                    formAnalysis += '  Method: ' + (form.method || 'GET') + '\n';

                    const inputs = form.querySelectorAll('input');
                    formAnalysis += '  Inputs: ' + inputs.length + '\n';

                    inputs.forEach(input => {
                        if (input.type === 'hidden') {
                            formAnalysis += '    Hidden: ' + input.name + ' = ' + input.value + '\n';
                        }
                    });
                });

                showAlert('FORM ANALYSIS', formAnalysis);
            }
        });
})();