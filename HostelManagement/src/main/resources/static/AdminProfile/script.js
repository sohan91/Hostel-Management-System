document.addEventListener("DOMContentLoaded", async () => {
    console.log("=== Admin Profile Page Loaded ===");

    // Selectors
    const nameDisplay = document.querySelector(".admin-name-display");
    const hostelDisplay = document.querySelector(".hostel-name-display");
    const goToDashboardBtn = document.querySelector(".go-to-dashboard-btn");

    // Fetch admin profile details from backend with JWT
    try {
        const token = localStorage.getItem('token');
        if (!token) {
            alert("Session expired. Please login again.");
            window.location.href = "/hostel/login";
            return;
        }

        const response = await fetch("http://localhost:8080/api/auth/admin-profile-details", {
            method: "GET",
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error("Failed to fetch admin profile. Status: " + response.status);
        }

        const admin = await response.json();
        console.log("Admin Profile Data:", admin);

        // ✅ Fill profile info in UI
        nameDisplay.textContent = `${admin.firstName} ${admin.lastName}`;
        hostelDisplay.textContent = admin.hostelName || "HostelHub - Branch";

        // Fill all <p data-field="...">
        const fieldMap = {
            "admin_id": admin.adminId,
            "first_name": admin.firstName,
            "last_name": admin.lastName,
            "email": admin.email,
            "phone_number": admin.phoneNumber || "N/A",
            "created_at": new Date(admin.createdAt).toLocaleDateString(),
            "hostel_name": admin.hostelName,
            "hostel_address": admin.hostelAddress
        };

        Object.entries(fieldMap).forEach(([key, value]) => {
            const fieldElement = document.querySelector(`[data-field='${key}']`);
            if (fieldElement) fieldElement.textContent = value ?? "N/A";
        });

    } catch (error) {
        console.error("Error loading profile:", error);
        alert("Error loading profile details. Please login again.");
        window.location.href = "/hostel/login";
    }

    // === Go To Dashboard Button ===
    if (goToDashboardBtn) {
        goToDashboardBtn.addEventListener("click", () => {
            window.location.href = "/hostel/dashboard";
        });
    }
});
