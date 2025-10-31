// Check authentication and setup page
document.addEventListener("DOMContentLoaded", function() {
    console.log('Login page loaded - checking authentication');

    // Check if user is already authenticated
    if (isUserAuthenticated()) {
        console.log('User already authenticated, redirecting to dashboard');
        window.location.href = '/hostel/dashboard';
        return;
    }

    // Only initialize login page if user is not authenticated
    initializeLoginPage();
});

// Check if user has valid JWT token
function isUserAuthenticated() {
    return document.cookie.includes('jwtToken=');
}

// Initialize login page functionality
function initializeLoginPage() {
    console.log('Initializing login page functionality');

    // Initialize password toggle
    initializePasswordToggle();

    // Initialize form submission
    initializeFormSubmission();

    // Initialize button events
    initializeButtonEvents();

    // Check URL parameters for messages
    checkURLParameters();

    // Clear any stored data
    clearStoredData();

    // Auto-focus email field
    autoFocusEmail();
}

// Password visibility toggle
function initializePasswordToggle() {
    const showPasswordCheckbox = document.getElementById("showPassword");
    const passwordField = document.getElementById("password");

    if (showPasswordCheckbox && passwordField) {
        showPasswordCheckbox.addEventListener("change", function() {
            passwordField.type = this.checked ? "text" : "password";
        });
    }
}

// Form submission handling
function initializeFormSubmission() {
    const loginForm = document.getElementById('loginForm');
    if (!loginForm) return;

    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        console.log('Login form submitted');
       
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;
       
        // Clear previous errors
        clearErrors();
       
        // Validate form
        const validation = validateForm(email, password);
        if (!validation.isValid) {
            showErrors(validation.errors);
            return;
        }
       
        // Attempt login
        await attemptLogin(email, password);
    });
}

// Button events
function initializeButtonEvents() {
    const hostlerBtn = document.getElementById('hostlerLoginBtn');
    const adminBtn = document.getElementById('adminLoginBtn');
   
    if (hostlerBtn) {
        hostlerBtn.addEventListener('click', function() {
            showMessage('Hostler login feature coming soon!', 'info');
        });
    }
   
    if (adminBtn) {
        adminBtn.addEventListener('click', function() {
            const emailField = document.getElementById('email');
            if (emailField) emailField.focus();
        });
    }
}

// Form validation
function validateForm(email, password) {
    const errors = {};
   
    // Email validation
    if (!email) {
        errors.email = 'Email is required';
    } else if (!isValidEmail(email)) {
        errors.email = 'Please enter a valid email address';
    }
   
    // Password validation
    if (!password) {
        errors.password = 'Password is required';
    } else if (password.length < 6) {
        errors.password = 'Password must be at least 6 characters';
    }
   
    return {
        isValid: Object.keys(errors).length === 0,
        errors: errors
    };
}

// Email validation
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// ✅ FIXED: Clear error messages properly
function clearErrors() {
    const emailError = document.getElementById('emailError');
    const passwordError = document.getElementById('passwordError');
   
    if (emailError) emailError.textContent = '';
    if (passwordError) passwordError.textContent = '';
}

// ✅ FIXED: Show validation errors properly
// ✅ FIXED: Show validation errors properly
function showErrors(errors) {
    if (errors.email) {
        const emailError = document.getElementById('emailError');
        if (emailError) {
            emailError.textContent = errors.email;
            // Clear error after 2 seconds
            setTimeout(() => {
                emailError.textContent = '';
            }, 2000);
        }
    }
   
    if (errors.password) {
        const passwordError = document.getElementById('passwordError');
        if (passwordError) {
            passwordError.textContent = errors.password;
            // Clear error after 2 seconds
            setTimeout(() => {
                passwordError.textContent = '';
            }, 2000);
        }
    }
   
    // Focus on first field with error
    if (errors.email) {
        const emailField = document.getElementById('email');
        if (emailField) emailField.focus();
    } else if (errors.password) {
        const passwordField = document.getElementById('password');
        if (passwordField) passwordField.focus();
    }
}

// Attempt login
async function attemptLogin(email, password) {
    const loginBtn = document.getElementById('loginBtn');
    if (!loginBtn) return;
   
    // Show loading state
    const originalText = loginBtn.textContent;
    loginBtn.textContent = 'Logging in...';
    loginBtn.disabled = true;
   
    try {
        console.log('Sending login request for:', email);
       
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        });
       
        console.log('Login response status:', response.status);
       
        const result = await response.json();
        console.log('Login result:', result);
       
        if (result.success) {
            showMessage('Login successful!');
           
            // Store admin data if available
            if (result.admin) {
                localStorage.setItem('adminData', JSON.stringify(result.admin));
            }
           
            // Redirect to dashboard after short delay
            setTimeout(() => {
                window.location.href = '/hostel/dashboard';
            }, 1000);
           
        } else {
            showMessage(result.message || 'Login failed. Please check your credentials.', 'error');
            resetLoginButton(loginBtn, originalText);
        }
       
    } catch (error) {
        console.error('Login error:', error);
        showMessage('Network error. Please check your connection and try again.', 'error');
        resetLoginButton(loginBtn, originalText);
    }
}

// Reset login button
function resetLoginButton(button, originalText) {
    if (button) {
        button.textContent = originalText;
        button.disabled = false;
    }
}

// Check URL parameters
function checkURLParameters() {
    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get('error');
    const logout = urlParams.get('logout');
   
    if (error === 'not_authenticated') {
        showMessage('Please login to access the requested page', 'error');
    } else if (logout === 'true') {
        showMessage('You have been successfully logged out', 'info');
    }
}

// Clear stored data
function clearStoredData() {
    localStorage.removeItem('adminData');
    sessionStorage.clear();
}

// Auto-focus email field
function autoFocusEmail() {
    setTimeout(() => {
        const emailField = document.getElementById('email');
        if (emailField) emailField.focus();
    }, 100);
}

// Show message function
function showMessage(message, type) {
    const messageArea = document.getElementById('messageArea');
    if (!messageArea) {
        console.error('Message area element not found');
        return;
    }
   
    console.log('Showing message:', message, 'Type:', type);
   
    // Set message content and type
    messageArea.textContent = message;
    messageArea.className = `message ${type}`;
    messageArea.style.display = 'block';
    messageArea.style.backgroundColor='#155724';
    messageArea.style.borderRadius='5px';
    messageArea.style.color='white';
   
    switch(type) {
        case 'success':
            messageArea.style.backgroundColor = '#d4edda';
            messageArea.style.color = '#155724';
            messageArea.style.border = '1px solid #c3e6cb';
            break;
        case 'error':
            messageArea.style.backgroundColor = '#f8d7da';
            messageArea.style.color = '#721c24';
            messageArea.style.border = '1px solid #f5c6cb';
            break;
        case 'info':
            messageArea.style.backgroundColor = '#d1ecf1';
            messageArea.style.color = '#1688c2ff';
            messageArea.style.border = '1px solid #02b8d4ff';
            break;
    }
   
    // Auto-hide after delay
    const hideTime = type === 'error' ? 2000 : 2000;
    setTimeout(() => {
        messageArea.style.display = 'none';
    }, hideTime);
}

// Add real-time validation
document.addEventListener('DOMContentLoaded', function() {
    if (!isUserAuthenticated()) {
        addRealTimeValidation();
    }
});

function addRealTimeValidation() {
    const emailField = document.getElementById('email');
    const passwordField = document.getElementById('password');
   
    if (emailField) {
        emailField.addEventListener('input', function() {
            const emailError = document.getElementById('emailError');
            if (emailError) emailError.textContent = '';
        });
       
        emailField.addEventListener('blur', function() {
            const email = this.value.trim();
            if (email && !isValidEmail(email)) {
                const emailError = document.getElementById('emailError');
                if (emailError) {
                    emailError.textContent = 'Please enter a valid email';
                   
                    setTimeout(() => {
                        emailError.textContent = '';
                    }, 2000);
                }
            }
        });
    }
   
    if (passwordField) {
        passwordField.addEventListener('input', function() {
            const passwordError = document.getElementById('passwordError');
            if (passwordError) passwordError.textContent = '';
        });
       
        passwordField.addEventListener('blur', function() {
            const password = this.value;
            if (password && password.length < 8) {
                const passwordError = document.getElementById('passwordError');
                if (passwordError) {
                    passwordError.textContent = 'Password must be at least 8 characters';
                    // Clear error after 2 seconds
                    setTimeout(() => {
                        passwordError.textContent = '';
                    }, 2000);
                }
            }
        });
    }
}

// Check if user already has session
function checkExistingSession() {
    fetch('/api/auth/check-session')
        .then(response => response.json())
        .then(data => {
            if (data.authenticated) {
                console.log('User already authenticated, redirecting to dashboard');
                window.location.href = '/hostel/dashboard';
            }
        })
        .catch(error => {
            console.log('No active session, showing login form');
        });
}

// Call this when page loads
document.addEventListener('DOMContentLoaded', function() {
    checkExistingSession();
});

// Update login success handler
function handleLoginSuccess(data) {
    if (data.success) {
        console.log('Login successful, redirecting to dashboard');
        window.location.href = '/hostel/dashboard';
    }
}
