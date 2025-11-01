document.addEventListener("DOMContentLoaded", async () => {
    console.log("=== Admin Profile Page Loaded ===");

    const nameDisplay = document.querySelector(".admin-name-display");
    const hostelDisplay = document.querySelector(".hostel-name-display");
    const goToDashboardBtn = document.querySelector(".go-to-dashboard-btn");

    if (nameDisplay) nameDisplay.textContent = "loading...";
    if (hostelDisplay) hostelDisplay.textContent = "loading...";

    const cachedAdminData = localStorage.getItem('adminData');
    if (cachedAdminData) {
        console.log("Using cached admin data for instant display");
        const admin = JSON.parse(cachedAdminData);
        updateProfileUI(admin);
    }

    try {
        console.log("Fetching profile details with cookie session...");
        
        const response = await fetch("/api/auth/admin-profile-details", {
            method: "GET",
            credentials: "include" 
        });

        console.log("Profile API Response Status:", response.status);

        if (response.status === 401) {
            console.warn("Session expired on profile page");

            if (cachedAdminData) {
                const admin = JSON.parse(cachedAdminData);
                updateProfileUI(admin);
                showProfileNotification("Session expired but showing cached data", "warning");
            } else {
                showProfileNotification("Session expired. Please login again.", "error");
                setTimeout(() => {
                    window.location.href = "/hostel/login";
                }, 3000);
            }
            return;
        }

        if (!response.ok) {
            throw new Error("Failed to fetch admin profile. Status: " + response.status);
        }

        const admin = await response.json();
        console.log("Admin Profile Data:", admin);

        updateProfileUI(admin);
        
        localStorage.setItem('adminData', JSON.stringify(admin));

    } catch (error) {
        console.error("Error loading profile:", error);
        
        if (cachedAdminData) {
            const admin = JSON.parse(cachedAdminData);
            updateProfileUI(admin);
            showProfileNotification("Using cached data - Network error", "warning");
        } else {
            showProfileNotification("Error loading profile. Please login again.", "error");
            setTimeout(() => {
                window.location.href = "/hostel/login";
            }, 3000);
        }
    }

    if (goToDashboardBtn) {
        goToDashboardBtn.addEventListener("click", () => {
            window.location.href = "/hostel/dashboard";
        });
    }
});

function updateProfileUI(admin) {
    console.log("Updating profile UI with:", admin);
    
    const nameDisplay = document.querySelector(".admin-name-display");
    const hostelDisplay = document.querySelector(".hostel-name-display");

    if (nameDisplay) {
        nameDisplay.textContent = `${admin.firstName || ''} ${admin.lastName || ''}`.trim() || "Admin";
    }

    if (hostelDisplay) {
        hostelDisplay.textContent = admin.hostelName || "HostelHub - Branch";
    }

    const fieldMap = {
        "admin_id": admin.adminId,
        "first_name": admin.firstName,
        "last_name": admin.lastName,
        "email": admin.email,
        "phone_number": admin.phoneNumber || "N/A",
        "created_at": admin.createdAt ? new Date(admin.createdAt).toLocaleDateString() : "N/A",
        "hostel_name": admin.hostelName,
        "hostel_address": admin.hostelAddress || "N/A"
    };

    Object.entries(fieldMap).forEach(([key, value]) => {
        const fieldElement = document.querySelector(`[data-field='${key}']`);
        if (fieldElement) fieldElement.textContent = value;
    });

    console.log("Profile UI updated successfully");
}


function showProfileNotification(message, type = "info") {
    console.log(`Profile Notification: ${message}`);

}