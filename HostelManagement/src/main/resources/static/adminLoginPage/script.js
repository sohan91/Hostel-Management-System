 const passwordField = document.getElementById("password");
  const showPasswordCheckbox = document.getElementById("showPassword");
  const createRegisterLink = document.getElementById("createRegister");

  createRegisterLink.addEventListener("click", function(e) {
    e.preventDefault();
    window.location.href = "/hostel/registration";
  });

  showPasswordCheckbox.addEventListener("change", function() {
    if (this.checked) {
      passwordField.type = "text";
      this.parentNode.firstChild.textContent = "Hide";
    } else {
      passwordField.type = "password";
      this.parentNode.firstChild.textContent = "Show";
    }
  });

  document.getElementById("loginForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();
    const usernameError = document.getElementById("usernameError");
    const passwordError = document.getElementById("passwordError");
    const messageArea = document.getElementById("messageArea");
    const loginBtn = document.getElementById("loginBtn");

    usernameError.textContent = "";
    passwordError.textContent = "";
    messageArea.innerHTML = "";

    let isValid = true;

    if (username === "") {
      usernameError.textContent = "Username is required.";
      isValid = false;
    }

    if (password === "") {
      passwordError.textContent = "Password is required.";
      isValid = false;
    } else if (password.length < 8) {
      passwordError.textContent = "Password must be at least 8 characters long.";
      isValid = false;
    }

    if (!isValid) return;

    loginBtn.disabled = true;
    loginBtn.textContent = "Logging in...";
    document.getElementById("loginForm").classList.add("loading");

    try {
      const formData = new FormData();
      formData.append('username', username);
      formData.append('password', password);

      const response = await fetch('/hostel/login', {
        method: 'POST',
        body: formData
      });

      if (response.redirected) {
        window.location.href = response.url;
      } else {
        const responseText = await response.text();
        const parser = new DOMParser();
        const doc = parser.parseFromString(responseText, 'text/html');
        const errorElement = doc.querySelector('[name="error"]');
        
        if (errorElement) {
          messageArea.innerHTML = `<div class="error-message">${errorElement.value || 'Invalid username/password'}</div>`;
        } else {
          messageArea.innerHTML = `<div class="error-message">Login failed. Please try again.</div>`;
        }
      }
    } catch (error) {
      console.error('Error:', error);
      messageArea.innerHTML = `<div class="error-message">Network error. Please try again.</div>`;
    } finally {
      loginBtn.disabled = false;
      loginBtn.textContent = "Login";
      document.getElementById("loginForm").classList.remove("loading");
    }
  });