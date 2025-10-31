const emailInput = document.getElementById("email");
const otpInput = document.getElementById("otp");
const sendOtpBtn = document.getElementById("sendOtpBtn");
const verifyOtpBtn = document.getElementById("verifyOtpBtn");
const passwordInput = document.getElementById("password");
const confirmPasswordInput = document.getElementById("confirmPassword");
const restOfForm = document.getElementById("restOfForm");
const formMessage = document.getElementById("formMessage");
const validationMessage = document.getElementById("validationMessage");
const otpTimer = document.getElementById("otpTimer");
const phoneInput = document.getElementById("phone");
const registerBtn = document.getElementById("registerBtn");
const loginBtn = document.getElementById("loginBtn");

const restOfFormElements = document.querySelectorAll('#restOfForm input, #restOfForm textarea, #restOfForm .register-btn, #restOfForm .password-toggle');

const sendOtpLoader = document.getElementById("sendOtpLoader");
const verifyOtpLoader = document.getElementById("verifyOtpLoader");
const registerLoader = document.getElementById("registerLoader");

window.onpageshow = function (event) {
    if (event.persisted) window.location.reload();
};

const inlineErrors = {};
document.querySelectorAll('.error-message').forEach(el => { inlineErrors[el.id] = el; });

let otpVerified = false;
let timerInterval;
let currentEmail = "";

function setLoading(button, loader, isLoading) {
    button.disabled = isLoading;
    loader.style.display = isLoading ? "block" : "none";
}

function displayInlineError(errorElementId, message) {
    const errorEl = inlineErrors[errorElementId];
    if (!errorEl) return;
    clearTimeout(errorEl.clearTimeoutId);
    errorEl.textContent = message;
    errorEl.classList.add('show');
    errorEl.clearTimeoutId = setTimeout(() => {
        errorEl.classList.remove('show');
        setTimeout(() => { errorEl.textContent = ""; }, 300);
    }, 1000);
}

function setFormEnabled(enabled) {
    restOfFormElements.forEach(el => {
        if (el.id !== 'loginBtn') el.disabled = !enabled;
    });
    if (enabled) restOfForm.classList.add('form-visible');
    else restOfForm.classList.remove('form-visible');
}

function displayMessageAndClear(message, isError = true, duration = 1000) {
    clearTimeout(formMessage.clearTimeoutId);
    formMessage.textContent = message;
    formMessage.className = isError ? 'error' : 'success';
    formMessage.style.opacity = 1;
    formMessage.clearTimeoutId = setTimeout(() => {
        formMessage.style.opacity = 0;
        setTimeout(() => {
            formMessage.textContent = "";
            formMessage.className = '';
        }, 300);
    }, duration);
}

function displayValidationMessage(message, isError = true) {
    validationMessage.textContent = message;
    validationMessage.className = isError ? 'validation-message error' : 'validation-message success';
    validationMessage.style.display = 'block';
    setTimeout(() => {
        validationMessage.style.display = 'none';
    }, 3000);
}

function goToLogin() { window.location.href = "/hostel/login"; }
loginBtn.addEventListener('click', goToLogin);

phoneInput.addEventListener('input', e => { e.target.value = e.target.value.replace(/\D/g, '').slice(0, 10); });

function validateEmail(email) {
    return /^[a-zA-Z0-9](?:[a-zA-Z0-9._%+-]*[a-zA-Z0-9])?@gmail\.com$/i.test(email);
}

function togglePasswordVisibility(inputElement, buttonElement) {
    if (inputElement.type === 'password') {
        inputElement.type = 'text';
        buttonElement.setAttribute('aria-label', 'Hide password');
    } else {
        inputElement.type = 'password';
        buttonElement.setAttribute('aria-label', 'Show password');
    }
}

document.querySelectorAll('.password-toggle').forEach(button => {
    button.addEventListener('click', function () {
        const targetInput = document.getElementById(this.getAttribute('data-target'));
        if (targetInput) togglePasswordVisibility(targetInput, this);
    });
});

function startOtpTimer() {
    let timer = 30;
    otpTimer.textContent = `Time left: ${timer}s`;
    otpTimer.classList.remove('success-message');
    otpTimer.style.cursor = 'default';
    otpTimer.style.color = '';
    
    // Disable email input and send OTP button for 30 seconds
    emailInput.disabled = true;
    sendOtpBtn.disabled = true;

    clearInterval(timerInterval);
    timerInterval = setInterval(() => {
        timer--;
        otpTimer.textContent = `Time left: ${timer}s`;
        if (timer <= 0) {
            clearInterval(timerInterval);
            otpTimer.innerHTML = "OTP expired. <span style='color: #007bff; cursor: pointer; text-decoration: underline;'>Send again</span>.";
            otpInput.disabled = true;
            verifyOtpBtn.disabled = true;
            
            // Re-enable email input and send OTP button when timer expires
            emailInput.disabled = false;
            sendOtpBtn.disabled = false;

            const sendAgainSpan = otpTimer.querySelector('span');
            sendAgainSpan.onclick = function () {
                if (!otpVerified) {
                    const email = emailInput.value.trim();
                    if (!validateEmail(email)) {
                        displayMessageAndClear("Please enter a valid Gmail address.", true, 1000);
                        displayInlineError("emailError", "Only @gmail.com addresses are allowed.");
                        return;
                    }
                    sendAgainSpan.textContent = "Sending...";
                    sendOtp(email);
                }
            };
        }
    }, 1000);
}

async function sendOtp(email) {
    setLoading(sendOtpBtn, sendOtpLoader, true);
    
    // Disable email input and verify OTP until email is sent
    emailInput.disabled = true;
    otpInput.disabled = true;
    verifyOtpBtn.disabled = true;
    
    currentEmail = email;

    try {
        const response = await fetch("http://localhost:8080/email/send-otp", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ to: email, subject: "Email Verification", body: "Use this OTP to verify your email." })
        });
        if (response.ok) {
            displayMessageAndClear("OTP sent to your email.", false, 2000);
            // Start timer and enable OTP input only after successful send
            startOtpTimer();
            otpInput.disabled = false;
            verifyOtpBtn.disabled = false;
        } else {
            const errorText = await response.text();
            let errorMessage = "Failed to send OTP.";
            let isExistingUser = false;
            try {
                const errorData = JSON.parse(errorText);
                errorMessage = errorData.message || errorMessage;
                if (errorMessage.toLowerCase().includes("already registered")) isExistingUser = true;
            } catch { }
            displayMessageAndClear(errorMessage, true, 2000);
            
            // Re-enable email input if OTP send fails
            emailInput.disabled = false;
            sendOtpBtn.disabled = false;
            
            if (isExistingUser) {
                clearInterval(timerInterval);
                otpTimer.textContent = "";
                otpInput.disabled = true;
                verifyOtpBtn.disabled = true;
                setTimeout(goToLogin, 2000);
            }
        }
    } catch (e) {
        console.error(e);
        displayMessageAndClear("Error connecting to server.", true, 2500);
        // Re-enable email input on network error
        emailInput.disabled = false;
        sendOtpBtn.disabled = false;
    } finally {
        setLoading(sendOtpBtn, sendOtpLoader, false);
    }
}

sendOtpBtn.addEventListener("click", function () {
    if (otpVerified) return;
    const email = emailInput.value.trim();
    document.querySelectorAll('.error-message').forEach(el => { el.textContent = ""; el.classList.remove('show'); });
    if (!validateEmail(email)) {
        displayMessageAndClear("Please enter a valid Gmail address.", true, 1000);
        displayInlineError("emailError", "Only @gmail.com addresses are allowed.");
        return;
    }
    sendOtp(email);
});

verifyOtpBtn.addEventListener("click", async function () {
    if (otpVerified) return;
    const otp = otpInput.value.trim();
    inlineErrors["otpError"].textContent = "";
    if (!otp) {
        displayMessageAndClear("Please enter OTP.", true, 1000);
        displayInlineError("otpError", "OTP is required.");
        return;
    }
    setLoading(verifyOtpBtn, verifyOtpLoader, true);
    try {
        const response = await fetch(`http://localhost:8080/email/verify-otp?email=${encodeURIComponent(currentEmail)}&otp=${encodeURIComponent(otp)}`, 
        { method: "POST" });
        if (response.ok) {
            clearInterval(timerInterval);
            otpVerified = true;
            otpTimer.textContent = "OTP verified!";
            otpTimer.classList.add('success-message');
            verifyOtpBtn.textContent = 'Verified 🎉';
            displayMessageAndClear("OTP verified successfully.", false, 2500);
            sendOtpBtn.disabled = true;
            verifyOtpBtn.disabled = true;
            emailInput.disabled = true;
            otpInput.disabled = true;
            setFormEnabled(true);
            restOfForm.style.display = "block";
            registerBtn.style.display = 'block';
            setTimeout(() => restOfForm.classList.add('form-visible'), 10);
        } else {
            const errorText = await response.text();
             let errorMessage = "Invalid or expired OTP.";
            try { errorMessage = JSON.parse(errorText).message || errorMessage; } catch { }
            otpVerified = false;
            setFormEnabled(false);
            displayMessageAndClear(errorMessage, true, 2000);
        }
    } catch (e) {
        console.error(e);
        setFormEnabled(false);
        displayMessageAndClear("Error connecting to server.", true, 2500);
    } finally {
        setLoading(verifyOtpBtn, verifyOtpLoader, false);
        if (!otpVerified) verifyOtpBtn.disabled = false;
    }
});

document.getElementById("registrationForm").addEventListener("submit", async function (e) {
    e.preventDefault();
    if (!otpVerified) {
        displayValidationMessage("Please verify your email first.", true);
        return;
    }
    let valid = true;
    document.querySelectorAll('.error-message').forEach(el => { el.textContent = ""; el.classList.remove('show'); });
    if (!validateEmail(emailInput.value.trim())) {
        displayInlineError("emailError", "Only @gmail.com addresses are allowed.");
        valid = false;
    }
    if (passwordInput.value.length < 8) {
        displayInlineError("passwordError", "Password must be at least 8 characters.");
        valid = false;
    }
    if (passwordInput.value !== confirmPasswordInput.value) {
        displayInlineError("confirmPasswordError", "Passwords do not match.");
        valid = false;
    }
    if (!/^\d{10}$/.test(phoneInput.value.trim())) {
        displayInlineError("phoneError", "Phone number must be exactly 10 digits.");
        valid = false;
    }
    ["firstName", "lastName", "hostelName", "hostelAddress"].forEach(f => {
        if (!document.getElementById(f).value.trim()) {
            displayInlineError(f + "Error", "This field is required.");
            valid = false;
        }
    });
    if (!valid) {
        displayValidationMessage("Please fix the highlighted errors.", true);
        return;
    }

    setLoading(registerBtn, registerLoader, true);
    const adminData = {
        firstName: document.getElementById("firstName").value.trim(),
        lastName: document.getElementById("lastName").value.trim(),
        email: currentEmail,
        phoneNumber: phoneInput.value.trim(),
        password: passwordInput.value,
        hostelName: document.getElementById("hostelName").value.trim(),
        hostelAddress: document.getElementById("hostelAddress").value.trim()
    };

    try {
        const response = await fetch("http://localhost:8080/admin/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(adminData)
        });
        const result = await response.json();
        if (response.ok && result.status === "success") {
            displayMessageAndClear(result.message + " 🎉", false, 5000);
            this.reset();
            setFormEnabled(false);
            restOfForm.classList.remove('form-visible');
            registerBtn.style.display = 'none';
            setTimeout(() => { restOfForm.style.display = "none"; }, 500);
            otpVerified = false;
            sendOtpBtn.disabled = false;
            emailInput.disabled = false;
            verifyOtpBtn.innerHTML = 'Verify OTP<div id="verifyOtpLoader" class="loader"></div>';
            setTimeout(goToLogin, 3000);
        } else displayMessageAndClear(result.message + " ⛔", true, 2000);
    } catch (e) {
        console.error(e);
        displayMessageAndClear("Error submitting form. Check network.", true, 2000);
    } finally {
        setLoading(registerBtn, registerLoader, false);
    }
});

setFormEnabled(false);
restOfForm.style.display = "none";