const verifyEmailBtn = document.getElementById("verifyEmailBtn");
const resetPasswordBtn = document.getElementById("resetPasswordBtn");
const messageArea = document.getElementById("messageArea");
const emailStep = document.getElementById("emailStep");
const resetStep = document.getElementById("resetStep");

function showMessage(msg, type) {
    messageArea.textContent = msg;
    messageArea.className = `message ${type} show`;
    setTimeout(() => {
        messageArea.classList.remove('show');
    }, 5000); 
}

verifyEmailBtn.addEventListener("click", async () => {
    const email = document.getElementById("email").value.trim();
    if (!email) { showMessage("Please enter your email.", "error"); return; }

    const response = await fetch("/hostel/api/verify-email", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email })
    });

    const data = await response.json();
    if (data.status === "success") {
        showMessage(data.message || "Email verified. Please reset your password.", "success");
        emailStep.style.display = "none";
        resetStep.style.display = "block";
    } else {
        showMessage(data.message || "Email verification failed.", "error");
    }
});

resetPasswordBtn.addEventListener("click", async () => {
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();
    const confirmPassword = document.getElementById("confirmPassword").value.trim();

    if (!password || !confirmPassword) { 
        showMessage("Please fill out both password fields.", "error"); 
        return; 
    }
    if (password.length < 8) {
        showMessage("Password must be 8 characters or more.", "error");
        return;
    }

    if (password !== confirmPassword) {
        showMessage("Passwords do not match.", "error");
        return;
    }

    const response = await fetch("/hostel/api/reset-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password, confirmPassword })
    });

    const data = await response.json();
    if (data.status === "success") {
        showMessage(data.message || "Password reset successful!", "success");
  
        resetStep.innerHTML = `<p class="success" style="padding: 15px; margin-bottom: 10px;">Password reset successful! You can now log in.</p>
                               <a href="/hostel/login" class="btn" style="text-decoration: none; display: block;">Go to Login</a>`;
    } else {
        showMessage(data.message || "Password reset failed. Please try again.", "error");
    }
});