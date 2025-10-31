// === DASHBOARD OPTIMIZED LOADING ===
document.addEventListener("DOMContentLoaded", () => {
    console.log("Dashboard loading - starting parallel operations");
    
    // 1. IMMEDIATELY set default values (no waiting)
    setDefaultDashboardValues();
    
    // 2. Initialize UI components immediately
    initializeDashboardUI();
    
    // 3. Start ALL API calls in parallel (no sequential waiting)
    //loadAllDataInParallel();
});

// Set default values INSTANTLY
function setDefaultDashboardValues() {
    console.log("Setting default dashboard values immediately");
    
    const hostelNameElem = document.querySelector(".hostel-name");
    const profileNameElem = document.querySelector(".profile-name");

    if (hostelNameElem) hostelNameElem.textContent = "loading...";
    if (profileNameElem) profileNameElem.textContent = "loading...";
    
    // Set any other default values here
}

// Initialize UI components immediately
function initializeDashboardUI() {
    console.log("Initializing UI components immediately");
    
    // Initialize sidebar toggle
    initializeSidebar();
    
    // Initialize profile navigation
    initializeProfileNavigation();
    
    // Initialize any other UI components
    console.log("UI components initialized");
}

// Admin details - optimized with localStorage cache
async function getAdminDetails() {
    // FIRST: Check if we have cached data from login
    const cachedAdminData = localStorage.getItem('adminData');
    if (cachedAdminData) {
        console.log("Using cached admin data for instant display");
        const admin = JSON.parse(cachedAdminData);
        updateDashboardUI(admin);
        
        // Clear cache after use
        localStorage.removeItem('adminData');
    }
    
    // THEN: Fetch fresh data from API
    try {
        const response = await fetch("/api/auth/admin-details", {
            method: "GET",
            credentials: "include"
        });

        if (response.ok) {
            const admin = await response.json();
            console.log("Fresh admin details loaded from API");
            updateDashboardUI(admin);
            return admin;
        }
    } catch (error) {
        console.error("Admin details error:", error);
        // UI already updated from cache, so no problem
    }
}

// Update UI with admin data
function updateDashboardUI(admin) {
    console.log("Updating dashboard UI with admin data");
    
    const hostelNameElem = document.querySelector(".hostel-name");
    const profileNameElem = document.querySelector(".profile-name");

    if (hostelNameElem && admin.hostelName) {
        hostelNameElem.textContent = admin.hostelName;
    }

    if (profileNameElem) {
        const fullName = `${admin.firstName || ''} ${admin.lastName || ''}`.trim();
        profileNameElem.textContent = fullName || "Admin";
    }
    
    console.log("Dashboard UI updated successfully");
}

// Sidebar toggle
function initializeSidebar() {
    const sidebar = document.querySelector(".sidebar");
    const sidebarToggle = document.getElementById("sidebarToggle");

    if (sidebar && sidebarToggle) {
        sidebar.classList.add("hidden");
        sidebarToggle.innerHTML = '<i class="fas fa-bars"></i>';
        sidebarToggle.setAttribute("aria-label", "Expand sidebar");
        let isSidebarHidden = true;

        sidebarToggle.addEventListener("click", () => {
            isSidebarHidden = !isSidebarHidden;
            if (isSidebarHidden) {
                sidebar.classList.add("hidden");
                sidebarToggle.innerHTML = '<i class="fas fa-bars"></i>';
                sidebarToggle.setAttribute("aria-label", "Expand sidebar");
            } else {
                sidebar.classList.remove("hidden");
                sidebarToggle.innerHTML = '<i class="fas fa-times"></i>';
                sidebarToggle.setAttribute("aria-label", "Collapse sidebar");
            }
        });
    }
}

// Profile navigation
function initializeProfileNavigation() {
    const profileToggle = document.querySelector('.profile-toggle');
    const profileMenu = document.querySelector('.profile-menu');
    const logoutLink = document.querySelector('.logout-link');

    if (profileToggle && profileMenu) {
        profileToggle.addEventListener('click', function(e) {
            e.stopPropagation();
            profileMenu.classList.toggle('active');
        });

        document.addEventListener('click', function() {
            profileMenu.classList.remove('active');
        });

        profileMenu.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    }

    if (logoutLink) {
        logoutLink.addEventListener('click', async function(e) {
            e.preventDefault();
            if (profileMenu) profileMenu.classList.remove('active');
            
            showNotification("Logging out...", "info");
            
            try {
                await fetch("/api/auth/logout", {
                    method: "POST",
                    credentials: "include"
                });
            } catch (error) {
                console.error("Logout API error:", error);
            }
            
            // Clear all storage
            localStorage.clear();
            sessionStorage.clear();
            
            // Immediate redirect
            window.location.href = "/hostel/login?logout=true";
        });
    }
}

// Notification function
function showNotification(message, type = "info") {
    console.log(`Notification: ${message}`);
    // Your existing notification code
}

let profile = document.querySelector(".hover-reveal");
profile.addEventListener("click",()=>{
  window.location.href="/hostel/admin-profile";
});

let logout = document.querySelector(".logout");
logout.addEventListener("click", async () => {
    try {
        console.log('Logging out...');
        
        // Call backend logout API first
        const response = await fetch('/api/auth/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include' // Important for cookies
        });
        
        const result = await response.json();
        console.log('Logout result:', result);
        
        if (result.success) {
            // Clear frontend storage
            localStorage.removeItem('adminData');
            sessionStorage.clear();
            
            // Redirect to login page
            window.location.href = "/hostel/login";
        } else {
            console.error('Logout failed:', result.message);
            window.location.href = "/hostel/login";
        }
        
    } catch (error) {
        console.error('Logout error:', error);
        // Fallback redirect
        window.location.href = "/hostel/login";
    }
});