const sidebar = document.getElementById('sidebar');

document.getElementById('sidebarToggle').addEventListener('click', function() {

    sidebar.classList.toggle('hidden');
});

function toggleSection(sectionId) {
    const section = document.getElementById(sectionId);
    
    const isExpanded = section.classList.contains('expanded');
    
 
    section.classList.toggle('expanded');
    
    const header = section.querySelector('.room-section-header');
    if (header) {
        header.setAttribute('aria-expanded', !isExpanded);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const floorContainers = document.querySelectorAll('.floor-rooms-container');

    floorContainers.forEach(container => {
        if (container.querySelector('.empty-floor-container')) return;

        if (container.children.length === 0 || Array.from(container.children).every(child => !child.classList.contains('room-card'))) {
            
            const emptyMessage = document.createElement('div');
            emptyMessage.className = 'empty-floor-container';
            emptyMessage.innerHTML = `
                <i class="fas fa-box-open"></i>
                <p>No rooms found on this floor. Click 'Add Room' above to create one.</p>
            `;
            
            container.appendChild(emptyMessage);
            
            const floorHeading = container.closest('.room-section-content').querySelector('.floor-heading');
            if(floorHeading) {
                floorHeading.style.display = 'none';
            }
        }
    });
});

document.addEventListener("DOMContentLoaded",function(){
  let logoutBtn = document.querySelector(".logout");
   logoutBtn.addEventListener("click",function(e){
    e.preventDefault();
      window.location.href = "/hostel/logout";   
   })
});

document.addEventListener("DOMContentLoaded",function(){
  let logoutBtn = document.querySelector(".hover-reveal");
   logoutBtn.addEventListener("click",function(e){
    e.preventDefault();
      window.location.href = "/hostel/admin-profile";   
   })
});
