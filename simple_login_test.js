// Simple login test script - paste this in browser console to test
(function() {
    console.log('🚀 Testing simple auto-login...');
    
    function fillFieldSlowly(element, text) {
        return new Promise((resolve) => {
            console.log('📝 Filling field:', element.id || element.name, 'with:', text);
            
            // Focus the element
            element.focus();
            element.click();
            
            // Clear first
            element.value = '';
            element.dispatchEvent(new Event('input', { bubbles: true }));
            
            let index = 0;
            function addNextChar() {
                if (index < text.length) {
                    element.value += text.charAt(index);
                    element.dispatchEvent(new Event('input', { bubbles: true }));
                    index++;
                    setTimeout(addNextChar, 100); // 100ms between characters
                } else {
                    element.dispatchEvent(new Event('change', { bubbles: true }));
                    element.dispatchEvent(new Event('blur', { bubbles: true }));
                    console.log('✅ Finished filling:', element.id || element.name, 'Final value:', element.value);
                    setTimeout(resolve, 300);
                }
            }
            
            setTimeout(addNextChar, 200);
        });
    }
    
    async function testLogin() {
        try {
            console.log('🎯 Starting test login...');
            
            // Find elements
            const rollField = document.querySelector('#rollNumber');
            const emailField = document.querySelector('#email');
            const passwordField = document.querySelector('#password');
            const submitBtn = document.querySelector('button[type="submit"]');
            
            console.log('🔍 Elements found:');
            console.log('Roll Number Field:', !!rollField, rollField?.id);
            console.log('Email Field:', !!emailField, emailField?.id);
            console.log('Password Field:', !!passwordField, passwordField?.id);
            console.log('Submit Button:', !!submitBtn, submitBtn?.textContent);
            
            if (!rollField || !emailField || !passwordField || !submitBtn) {
                console.log('❌ Missing form elements');
                return;
            }
            
            // Fill fields
            await fillFieldSlowly(rollField, '23ETCCS166');
            await fillFieldSlowly(emailField, 'vishesh2023cse@technonjr.org');
            await fillFieldSlowly(passwordField, 'Visheshjain18@');
            
            // Wait and submit
            await new Promise(resolve => setTimeout(resolve, 1000));
            console.log('🚀 Clicking submit...');
            submitBtn.focus();
            submitBtn.click();
            
            console.log('✅ Test completed');
            
        } catch (error) {
            console.error('💥 Test error:', error);
        }
    }
    
    testLogin();
})();