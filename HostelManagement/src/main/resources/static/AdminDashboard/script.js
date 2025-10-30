// === SIDEBAR TOGGLE ===
document.addEventListener("DOMContentLoaded", () => {
    const sidebar = document.querySelector(".sidebar");
    const sidebarToggle = document.getElementById("sidebarToggle");

    if (sidebar && sidebarToggle) {
        let isSidebarHidden = localStorage.getItem("isSidebarHidden") === "true";

        function toggleSidebar() {
            isSidebarHidden = !isSidebarHidden;
            localStorage.setItem("isSidebarHidden", isSidebarHidden);
            applySidebarState();
        }

        function applySidebarState() {
            if (isSidebarHidden) {
                sidebar.classList.add("hidden");
                sidebarToggle.innerHTML = '<i class="fas fa-bars"></i>';
                sidebarToggle.setAttribute("aria-label", "Expand sidebar");
            } else {
                sidebar.classList.remove("hidden");
                sidebarToggle.innerHTML = '<i class="fas fa-times"></i>';
                sidebarToggle.setAttribute("aria-label", "Collapse sidebar");
            }
        }

        applySidebarState();
        sidebarToggle.addEventListener("click", toggleSidebar);
    } else {
        console.error("Sidebar elements not found. Check your HTML structure.");
    }
    initializePage();
});

 

async function getAdminDetails() {
    try {
        const hostelNameElem = document.querySelector(".hostel-name");
        const profileNameElem = document.querySelector(".profile-name");
        if (hostelNameElem && profileNameElem) {
            hostelNameElem.textContent = "Loading...";
            profileNameElem.textContent = "Loading...";
        }

        const response = await fetch("http://localhost:8080/api/auth/admin-details", {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            if (response.status === 404) {
                console.log("Admin not found in session, redirecting to login");
                window.location.href = "/hostel/login";
                return;
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const admin = await response.json();
        if (hostelNameElem) hostelNameElem.textContent = admin.hostelName || "Hostel Management";
        if (profileNameElem) profileNameElem.textContent = admin.firstName || "Admin";
    } catch (error) {
        console.error("Failed to fetch admin details:", error);
        const hostelNameElem = document.querySelector(".hostel-name");
        const profileNameElem = document.querySelector(".profile-name");
        if (hostelNameElem) hostelNameElem.textContent = "Hostel Management";
        if (profileNameElem) profileNameElem.textContent = "Admin";
    }
}
