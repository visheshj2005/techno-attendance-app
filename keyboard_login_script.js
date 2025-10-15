// Keyboard-based login script that simulates real user typing
(function() {
    console.log('⌨️ Starting keyboard-based auto-login...');
    
    if (window.autoLoginExecuted) {
        console.log('Auto-login already executed, skipping...');
        return;
    }
    window.autoLoginExecuted = true;
    
    // Simulate real keyboard typing with proper events
    function simulateKeyboardTyping(element, text) {
        return new Promise((resolve) => {
            console.log('⌨️ Simulating keyboard typing for:', element.id || element.name);
            
            // Focus the element to trigger keyboard
            element.focus();
            element.click(); // This should trigger the soft keyboard on mobile
            
            // Clear the field first
            element.value = '';
            
            // Dispatch focus events
            element.dispatchEvent(new FocusEvent('focusin', { bubbles: true }));
            element.dispatchEvent(new FocusEvent('focus', { bubbles: true }));
            
            let currentText = '';
            let charIndex = 0;
            
            function typeNextCharacter() {
                if (charIndex < text.length) {
                    const char = text.charAt(charIndex);
                    const keyCode = char.charCodeAt(0);
                    
                    // Simulate key press sequence
                    const keydownEvent = new KeyboardEvent('keydown', {
                        key: char,
                        code: `Key${char.toUpperCase()}`,
                        keyCode: keyCode,
                        which: keyCode,
                        charCode: keyCode,
                        bubbles: true,
                        cancelable: true,
                        composed: true
                    });
                    
                    const keypressEvent = new KeyboardEvent('keypress', {
                        key: char,
                        code: `Key${char.toUpperCase()}`,
                        keyCode: keyCode,
                        which: keyCode,
                        charCode: keyCode,
                        bubbles: true,
                        cancelable: true,
                        composed: true
                    });
                    
                    const inputEvent = new InputEvent('input', {
                        data: char,
                        inputType: 'insertText',
                        bubbles: true,
                        cancelable: true,
                        composed: true
                    });
                    
                    const keyupEvent = new KeyboardEvent('keyup', {
                        key: char,
                        code: `Key${char.toUpperCase()}`,
                        keyCode: keyCode,
                        which: keyCode,
                        charCode: keyCode,
                        bubbles: true,
                        cancelable: true,
                        composed: true
                    });
                    
                    // Dispatch events in proper sequence
                    element.dispatchEvent(keydownEvent);
                    element.dispatchEvent(keypressEvent);
                    
                    // Update value
                    currentText += char;
                    element.value = currentText;
                    
                    // Dispatch input event
                    element.dispatchEvent(inputEvent);
                    element.dispatchEvent(keyupEvent);
                    
                    // Additional events for frameworks
                    element.dispatchEvent(new Event('change', { bubbles: true }));
                    element.dispatchEvent(new CustomEvent('input-change', { bubbles: true, detail: { value: currentText } }));
                    
                    charIndex++;
                    
                    // Realistic typing speed (100-200ms per character)
                    setTimeout(typeNextCharacter, 150 + Math.random() * 100);
                } else {
                    // Finished typing
                    console.log('✅ Finished typing:', currentText);
                    
                    // Final events
                    element.dispatchEvent(new Event('change', { bubbles: true }));
                    element.dispatchEvent(new FocusEvent('blur', { bubbles: true }));
                    
                    // Verify value was set
                    if (element.value === text) {
                        console.log('✅ Value verified:', element.value);
                    } else {
                        console.log('⚠️ Value mismatch. Expected:', text, 'Got:', element.value);
                        element.value = text;
                        element.dispatchEvent(new Event('input', { bubbles: true }));
                        element.dispatchEvent(new Event('change', { bubbles: true }));
                    }
                    
                    setTimeout(resolve, 500);
                }
            }
            
            // Start typing after a short delay
            setTimeout(typeNextCharacter, 300);
        });
    }
    
    // Find form elements
    function findFormElements() {
        const rollNumberField = document.querySelector('#rollNumber') || 
                               document.querySelector('input[name="rollNumber"]') ||
                               document.querySelector('input[placeholder*="Roll Number" i]');
        
        const emailField = document.querySelector('#email') || 
                          document.querySelector('input[name="email"]') ||
                          document.querySelector('input[type="email"]');
        
        const passwordField = document.querySelector('#password') || 
                             document.querySelector('input[name="password"]') ||
                             document.querySelector('input[type="password"]');
        
        const submitButton = document.querySelector('button[type="submit"]') ||
                            document.querySelector('input[type="submit"]') ||
                            document.querySelector('button:contains("Sign In")');
        
        return { rollNumberField, emailField, passwordField, submitButton };
    }
    
    // Main login execution
    async function executeKeyboardLogin() {
        try {
            console.log('🎯 Starting keyboard-based login execution...');
            
            // Wait for page to be fully loaded
            await new Promise(resolve => setTimeout(resolve, 2000));
            
            const { rollNumberField, emailField, passwordField, submitButton } = findFormElements();
            
            if (!rollNumberField || !emailField || !passwordField || !submitButton) {
                console.log('❌ Form elements not found');
                console.log('Roll Number Field:', !!rollNumberField);
                console.log('Email Field:', !!emailField);
                console.log('Password Field:', !!passwordField);
                console.log('Submit Button:', !!submitButton);
                return false;
            }
            
            console.log('✅ All form elements found');
            
            // Step 1: Fill roll number with keyboard simulation
            console.log('📝 Step 1: Typing roll number...');
            await simulateKeyboardTyping(rollNumberField, '${rollNumber}'.toUpperCase());
            
            // Wait between fields
            await new Promise(resolve => setTimeout(resolve, 1000));
            
            // Step 2: Fill email with keyboard simulation
            console.log('📧 Step 2: Typing email...');
            await simulateKeyboardTyping(emailField, '${email}');
            
            // Wait between fields
            await new Promise(resolve => setTimeout(resolve, 1000));
            
            // Step 3: Fill password with keyboard simulation
            console.log('🔐 Step 3: Typing password...');
            await simulateKeyboardTyping(passwordField, '${password}');
            
            // Wait before submission
            await new Promise(resolve => setTimeout(resolve, 2000));
            
            // Step 4: Submit form
            console.log('🚀 Step 4: Submitting form...');
            
            // Focus on submit button
            submitButton.focus();
            
            // Wait a moment
            await new Promise(resolve => setTimeout(resolve, 500));
            
            // Simulate button click with full event sequence
            submitButton.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }));
            submitButton.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
            submitButton.dispatchEvent(new MouseEvent('click', { bubbles: true }));
            
            // Also try form submission
            const form = submitButton.closest('form');
            if (form) {
                setTimeout(() => {
                    form.submit();
                }, 1000);
            }
            
            console.log('✅ Login submission completed');
            
            // Monitor for success/failure
            setTimeout(() => {
                const currentUrl = window.location.href;
                if (currentUrl.includes('student') && !currentUrl.includes('login')) {
                    console.log('🎉 LOGIN SUCCESS! Redirected to:', currentUrl);
                } else {
                    console.log('⚠️ Still on login page, checking for errors...');
                    
                    // Check for error messages
                    const errorElements = document.querySelectorAll('*');
                    for (let el of errorElements) {
                        if (el.textContent && el.textContent.toLowerCase().includes('failed')) {
                            console.log('❌ Error found:', el.textContent);
                        }
                    }
                }
            }, 3000);
            
            return true;
        } catch (error) {
            console.error('💥 Keyboard login error:', error);
            return false;
        }
    }
    
    // Wait for page ready and execute
    if (document.readyState === 'complete') {
        setTimeout(executeKeyboardLogin, 1000);
    } else {
        document.addEventListener('DOMContentLoaded', () => {
            setTimeout(executeKeyboardLogin, 1000);
        });
    }
})();