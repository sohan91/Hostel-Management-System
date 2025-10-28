document.addEventListener("DOMContentLoaded", function() {
  let logOutBtn = document.querySelector("button.nav-btn.logout-btn");

  logOutBtn.addEventListener("click", function(e) {
    e.preventDefault();   
    console.log("returned to login page");
    window.location.href = "/hostel/logout";
  });
});
