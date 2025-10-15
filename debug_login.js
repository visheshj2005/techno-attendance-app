// Debug script to help identify login issues
// This can be run in the browser console to debug the login form

(function() {
    console.log('🔍 DEBUG: Analyzing login form...');
    
    // Find all form elements
    const forms = document.querySelectorAll('form');
    console.log('Forms found:', forms.length);
    
    forms.forEach((form, index) => {
        console.log(`Form ${index}:`, form);
        
        const inputs = form.querySelectorAll('input');
        console.log(`  Inputs in form ${index}:`, inputs.length);
        
        inputs.forEach(input => {
            console.log(`    Input:`, {
                name: input.name,
                id: input.id,
                type: input.type,
                placeholder: input.placeholder,
                required: input.required,
                value: input.value,
                disabled: input.disabled
            });
        });
        
        const buttons = form.querySelectorAll('button, input[type="submit"]');
        console.log(`  Buttons in form ${index}:`, buttons.length);
        
        buttons.forEach(button => {
            console.log(`    Button:`, {
                type: button.type,
                textContent: button.textContent,
                disabled: button.disabled,
                onclick: button.onclick
            });
        });
    });
    
    // Check for validation libraries
    console.log('🔍 Checking for validation libraries...');
    
    if (window.jQuery) console.log('✅ jQuery detected');
    if (window.React) console.log('✅ React detected');
    if (window.Vue) console.log('✅ Vue detected');
    if (window.angular) console.log('✅ Angular detected');
    
    // Check for form validation
    const rollNumberField = document.querySelector('#rollNumber');
    const emailField = document.querySelector('#email');
    const passwordField = document.querySelector('#password');
    
    if (rollNumberField) {
        console.log('Roll Number Field:', {
            value: rollNumberField.value,
            validity: rollNumberField.validity,
            validationMessage: rollNumberField.validationMessage
        });
    }
    
    if (emailField) {
        console.log('Email Field:', {
            value: emailField.value,
            validity: emailField.validity,
            validationMessage: emailField.validationMessage
        });
    }
    
    if (passwordField) {
        console.log('Password Field:', {
            value: passwordField.value,
            validity: passwordField.validity,
            validationMessage: passwordField.validationMessage
        });
    }
    
    // Test manual form submission
    console.log('🧪 Testing manual form submission...');
    
    function testLogin() {
        if (rollNumberField) rollNumberField.value = '23ETCCS166';
        if (emailField) emailField.value = 'vishesh2023cse@technonjr.org';
        if (passwordField) passwordField.value = 'Visheshjain18@';
        
        const submitButton = document.querySelector('button[type="submit"]');
        if (submitButton) {
            console.log('Submit button found, clicking...');
            submitButton.click();
        } else {
            console.log('No submit button found');
        }
    }
    
    // Expose test function globally
    window.testLogin = testLogin;
    console.log('✅ Debug complete. Run testLogin() to test manual submission.');
})();