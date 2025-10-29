document.addEventListener("DOMContentLoaded", () => {
    let goToAdminBtn = document.querySelector(".go-to-dashboard-btn");

        goToAdminBtn.addEventListener("click", () => {
            window.location.href = "/hostel/dashboard";
        });
    
});