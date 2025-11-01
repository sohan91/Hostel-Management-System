// === DASHBOARD OPTIMIZED LOADING ===
document.addEventListener("DOMContentLoaded", () => {
    console.log("Dashboard loading - starting parallel operations");
    
    // 1. IMMEDIATELY set default values (no waiting)
    setDefaultDashboardValues();
    
    // 2. Initialize UI components immediately
    initializeDashboardUI();
    
    // 3. Load dynamic data from APIs
    loadAllDataInParallel();
});

function setDefaultDashboardValues() {
    console.log("Setting default dashboard values immediately");
    
    const hostelNameElem = document.querySelector(".hostel-name");
    const profileNameElem = document.querySelector(".profile-name");

    if (hostelNameElem) hostelNameElem.textContent = "Loading...";
    if (profileNameElem) profileNameElem.textContent = "Loading...";
}

function initializeDashboardUI() {
    console.log("Initializing UI components immediately");
    
    initializeSidebar();
    initializeProfileNavigation();
    initializeSearch();
    console.log("UI components initialized");
}

// === HELPER: Aggressive Content Clear ===
function clearMainContentContainer() {
    // Target the main container where all rooms/empty states are displayed
    const container = document.querySelector('.room-section.expanded'); 
    
    if (container) {
        container.innerHTML = ''; // Clear all inner HTML
        container.style.display = 'block'; // Ensure it's visible
        console.log("🧹 Main content container aggressively cleared.");
    }
    
    // Also, clear any global empty state container if it exists outside the main section
    const globalContainer = document.querySelector('.empty-state-global-container');
    if (globalContainer) {
        globalContainer.remove();
        console.log("🧹 Removed any existing global empty state.");
    }
}

// === DYNAMIC DATA LOADING (PARALLEL) ===
async function loadAllDataInParallel() {
    console.log("Starting parallel data fetching: Admin Details and Room Details");
    
    // Clear the container before showing any content (including loading states)
    clearMainContentContainer(); 
    
    try {
        // Use Promise.all to fetch both data sets concurrently
        const [adminDetails, roomDetails] = await Promise.all([
            getAdminDetails(),
            getRoomDetails()
        ]);

        console.log("✅ All initial data loaded successfully in parallel.");

    } catch (error) {
        console.error("❌ An overall error occurred during parallel data loading:", error);
    }
}

// Admin details function (No changes needed here)
async function getAdminDetails() {
    console.log("Fetching admin details...");
    
    try {
        const response = await fetch("/api/auth/admin-details", {
            method: "GET",
            credentials: "include",
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (response.ok) {
            const adminData = await response.json();
            updateDashboardUI(adminData);
            return adminData;
        } else {
            console.error("Failed to fetch admin details:", response.status);
            showNotification("Failed to load admin details", "error");
        }
    } catch (error) {
        console.error("Error fetching admin details:", error);
        showNotification("Error loading admin details", "error");
    }
}

// Room details function - Fetches real data from API
async function getRoomDetails() {
    console.log("Fetching room details from API...");
    
    try {
        const response = await fetch("/api/auth/room-details", {
            method: "GET",
            credentials: "include",
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (response.ok) {
            const roomsData = await response.json();
            console.log("Room details loaded from API:", roomsData);
            
            // Handle both array response and message response
            if (Array.isArray(roomsData) && roomsData.length > 0) {
                displayRoomsWithSharingHeaders(roomsData);
                return roomsData;
            } else {
                // Rooms array is empty, check for sharing types
                await showSharingTypesForEmptyRooms();
                return [];
            }
        } else {
            console.error("Failed to fetch room details:", response.status);
            showNotification("Failed to load room details", "error");
            showGlobalEmptyState(); 
        }
    } catch (error) {
        console.error("Error fetching room details:", error);
        showNotification("Error loading room details", "error");
        showGlobalEmptyState();
    }
}

// Fetch sharing types when no rooms exist
async function showSharingTypesForEmptyRooms() {
    console.log("🔄 Fetching sharing types since no rooms exist");
    
    try {
        const response = await fetch("/api/auth/sharing-details", {
            method: "GET",
            credentials: "include",
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (response.ok) {
            const sharingData = await response.json();
            console.log("Sharing types loaded from API:", sharingData);
            
            if (Array.isArray(sharingData) && sharingData.length > 0) {
                // Sharing types exist, display them with empty room states
                sharingData.sort((a, b) => (a.capacity || 99) - (b.capacity || 99));
                displaySharingTypesWithoutRooms(sharingData);
            } else {
                // No rooms AND no sharing types exist, show the absolute empty state
                showGlobalEmptyState();
            }
        } else {
             // Failed to fetch sharing types (e.g., 404/500), assume they don't exist
            showGlobalEmptyState();
        }
    } catch (error) {
        console.error("Error fetching sharing types:", error);
        showGlobalEmptyState();
    }
}

function updateDashboardUI(admin) {
    console.log("Updating dashboard UI with admin data:", admin);
    
    const hostelNameElem = document.querySelector(".hostel-name");
    const profileNameElem = document.querySelector(".profile-name");

    if (hostelNameElem && admin.hostelName) {
        hostelNameElem.textContent = admin.hostelName;
    }

    if (profileNameElem) {
        const fullName = `${admin.firstName || ''} ${admin.lastName || ''}`.trim();
        profileNameElem.textContent = fullName || "Admin";
    }
}

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

function initializeProfileNavigation() {
    const profileElement = document.querySelector('.user-profile-info');
    const logoutButton = document.querySelector('.logout');

    if (profileElement) {
        profileElement.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            console.log("Profile clicked - navigating to profile page");
            window.location.href = "/hostel/admin-profile";
        });

        profileElement.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                console.log("Enter key pressed - navigating to profile page");
                window.location.href = "/hostel/admin-profile";
            }
        });
    }

    if (logoutButton) {
        logoutButton.addEventListener('click', async function(e) {
            e.preventDefault();
            e.stopPropagation();
            console.log("Logout button clicked");
            
            showNotification("Logging out...", "info");
            
            try {
                const response = await fetch("/api/auth/logout", {
                    method: "POST",
                    credentials: "include"
                });
                
                if (response.ok) {
                    console.log("Logout successful");
                    localStorage.clear();
                    sessionStorage.clear();
                    window.location.href = "/hostel/login";
                } else {
                    console.error("Logout failed with status:", response.status);
                    localStorage.clear();
                    sessionStorage.clear();
                    window.location.href = "/hostel/login";
                }
            } catch (error) {
                console.error("Logout API error:", error);
                localStorage.clear();
                sessionStorage.clear();
                window.location.href = "/hostel/login";
            }
        });
    }
}

function showNotification(message, type = "info") {
    console.log(`Notification: ${message}`);
    // Create a nice notification instead of alert
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.remove();
    }, 3000);
}

// === SEARCH FUNCTIONALITY ===
function initializeSearch() {
    console.log("🔍 initializeSearch() called");
    
    const searchInput = document.getElementById('roomSearch');
    console.log("Search input found:", !!searchInput);
    
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const searchTerm = e.target.value.toLowerCase().trim();
            console.log("Searching for:", searchTerm);
            filterRooms(searchTerm);
        });
    }
}

function filterRooms(searchTerm) {
    const roomCards = document.querySelectorAll('.room-card');
    console.log("Filtering", roomCards.length, "rooms for:", searchTerm);
    
    let visibleCount = 0;

    roomCards.forEach(card => {
        const roomNumber = card.querySelector('.room-number')?.textContent.toLowerCase() || '';
        const floor = card.getAttribute('data-floor') || '';
        const sharingType = card.getAttribute('data-sharing-type')?.toLowerCase() || '';
        
        const matchesSearch = 
            roomNumber.includes(searchTerm) ||
            floor.includes(searchTerm) ||
            sharingType.includes(searchTerm) ||
            searchTerm === '';

        if (matchesSearch) {
            card.style.display = 'block';
            visibleCount++;
            
            // Show parent sections
            let currentElement = card.parentElement;
            while (currentElement) {
                if (currentElement.classList.contains('floor-section') || 
                    currentElement.classList.contains('sharing-type-section')) {
                    currentElement.style.display = 'block';
                }
                currentElement = currentElement.parentElement;
            }
        } else {
            card.style.display = 'none';
        }
    });

    console.log("Visible rooms after filtering:", visibleCount);

    // Show no results message if needed
    const existingNoResults = document.querySelector('.no-results');
    if (searchTerm && visibleCount === 0) {
        if (!existingNoResults) {
            showNoSearchResults(searchTerm);
        }
    } else if (existingNoResults) {
        existingNoResults.remove();
    }
}

function showNoSearchResults(searchTerm) {
    const container = document.getElementById('roomManagementContainer');
    if (!container) return;

    const noResults = document.createElement('div');
    noResults.className = 'no-results empty-state';
    noResults.innerHTML = `
        <div class="empty-state-content">
            <i class="fas fa-search"></i>
            <h3>No Rooms Found</h3>
            <p>No rooms found matching "${searchTerm}"</p>
            <button class="btn-secondary" onclick="clearSearch()">
                <i class="fas fa-times"></i> Clear Search
            </button>
        </div>
    `;

    container.appendChild(noResults);
}

function clearSearch() {
    const searchInput = document.getElementById('roomSearch');
    if (searchInput) {
        searchInput.value = '';
    }
    filterRooms('');
    
    const noResults = document.querySelector('.no-results');
    if (noResults) {
        noResults.remove();
    }
}

// === ROOM MANAGEMENT WITH REAL API DATA ===
function displayRoomsWithSharingHeaders(rooms) {
    console.log("🎨 displayRoomsWithSharingHeaders() called with real API data:", rooms);
    
    const roomSection = document.querySelector('.room-section.expanded');
    
    if (!roomSection) {
        console.error("❌ Room section not found!");
        return;
    }

    // Aggressively clear content first
    clearMainContentContainer();

    if (!rooms || !Array.isArray(rooms) || rooms.length === 0) {
        console.log("❌ No rooms to display (secondary check)");
        return; 
    }

    console.log("✅ Displaying", rooms.length, "real rooms from API");

    // Group rooms by sharing type first, then by floor using real API data
    const roomsBySharingType = groupRoomsBySharingTypeAndFloor(rooms);
    console.log("📊 Real rooms grouped by sharing type:", roomsBySharingType);

    // Create sharing type sections with real data
    Object.keys(roomsBySharingType).sort((a, b) => parseInt(a) - parseInt(b)).forEach(sharingType => {
        const sharingSection = createSharingTypeSection(sharingType, roomsBySharingType[sharingType]);
        roomSection.appendChild(sharingSection);
        console.log(`✅ Added sharing type section: ${sharingType}`);
    });

    // Initialize room interactions and dropdown toggles
    initializeRoomCardInteractions();
    initializeDropdownToggles();
    initializeAddRoomButtons();
    
    console.log("✅ Real rooms displayed successfully from API");
}

// === Global Empty State Function (The one that replaces everything) ===
function showGlobalEmptyState() {
    console.log("⚠️ Displaying GLOBAL empty state: No rooms or sharing types exist.");
    const container = document.querySelector('.room-section.expanded');
    
    if (!container) {
         console.error("❌ Main room section container not found!");
         return;
    }

    // Aggressively clear content first
    clearMainContentContainer();

    container.innerHTML = `
        <div class="empty-state-global-container" style="text-align: center; padding: 50px 20px; min-height: 200px;">
            <div class="empty-state-content">
                <i class="fas fa-home" style="font-size: 40px; color: #007bff;"></i>
                <h3 style="margin-top: 15px;">No Rooms or Sharing Types Configured</h3>
                <p>No-Sharing are available to start that click on **Add ShareType Button** to start the application</p>
                <button class="btn-primary" onclick="window.location.href='/hostel/add-sharing-type'" style="margin-top: 20px;">
                    <i class="fas fa-plus"></i> Add ShareType
                </button>
            </div>
        </div>
    `;
    
    container.style.display = 'block'; 
}

function initializeRoomCardInteractions() {
    console.log("Initializing room card interactions (e.g., Book Now/View Details).");
    // Implementation for button event listeners goes here
}


// Display sharing types when no rooms exist
function displaySharingTypesWithoutRooms(sharingTypes) {
    console.log("🏗️ Displaying sharing types without rooms:", sharingTypes);
    
    const roomSection = document.querySelector('.room-section.expanded');
    if (!roomSection) return;

    // Aggressively clear content first
    clearMainContentContainer();

    // Create sharing type sections similar to when there are rooms, but with empty state
    sharingTypes.forEach(sharingType => {
        const sharingSection = createEmptySharingTypeSection(sharingType);
        roomSection.appendChild(sharingSection);
    });
    
    // Initialize dropdown toggles and add room buttons for these empty sections
    initializeDropdownToggles();
    initializeAddRoomButtons(); 

    console.log(`✅ Created ${sharingTypes.length} sharing type sections without rooms`);
}

// === Empty Sharing Type Section (for no rooms but sharing types exist) ===
function createEmptySharingTypeSection(sharingType) {
    console.log("📦 Creating empty sharing type section for:", sharingType);
    
    const capacity = sharingType.capacity || 1; 
    const price = sharingType.sharingFee || 5000;
    const sharingTypeName = `${capacity}-Sharing`;
    
    const sharingDiv = document.createElement('div');
    sharingDiv.className = 'sharing-type-section empty-sharing-section';
    sharingDiv.setAttribute('data-sharing-type', sharingTypeName);
    
    
    // Create sharing type header with dropdown toggle and add room button
    const headerDiv = document.createElement('div');
    headerDiv.className = 'sharing-type-header';
    headerDiv.innerHTML = `
        <div class="sharing-type-info">
            <button class="dropdown-toggle" data-sharing-type="${sharingTypeName}">
                <i class="fas fa-chevron-down"></i>
            </button>
            <div class="sharing-type-details">
                <h2>${sharingTypeName}</h2>
                <div class="sharing-type-stats">
                    <span class="floor-count">0 floors</span>
                    <span class="room-count">0 rooms</span>
                    <span class="price-info">₹${price} / bed</span>
                </div>
            </div>
        </div>
        <div class="sharing-type-actions">
            <button class="add-room-btn" data-sharing-type="${sharingTypeName}" data-capacity="${capacity}" data-price="${price}">
                <i class="fas fa-plus"></i> Add Room
            </button>
        </div>
    `;
    sharingDiv.appendChild(headerDiv);
    
    // Create empty floors container with centered message
    const floorsContainer = document.createElement('div');
    floorsContainer.className = 'floors-container empty-floors-container';
    floorsContainer.id = `floors-${sharingTypeName.replace(/\s+/g, '-').toLowerCase()}`;
    
    // The key change is to ensure the message content is centered
    floorsContainer.innerHTML = `
        <div class="empty-room-state-container" style="
            display: flex; 
            justify-content: center; 
            align-items: center; 
            text-align: center;
            min-height: 150px; 
            padding: 20px;
        "> 
            <div class="empty-room-content">
                <i class="fas fa-door-open" style="font-size: 30px; color: #aaa;"></i>
                <h4 style="margin-top: 10px;">No Rooms Added Yet</h4>
                <p>This sharing type doesn't have any rooms. Click "Add Room" above to create the first room.</p>
            </div>
        </div>
    `;
    
    sharingDiv.appendChild(floorsContainer);
    
    return sharingDiv;
}

function groupRoomsBySharingTypeAndFloor(rooms) {
    const grouped = {};
    
    rooms.forEach(room => {
        // Use the actual field names from your RoomDTO
        const sharingType = room.sharingTypeName || `${room.sharingCapacity}-Sharing`;
        const floorNumber = room.floorNumber || 1;
        
        if (!grouped[sharingType]) {
            grouped[sharingType] = {};
        }
        
        if (!grouped[sharingType][floorNumber]) {
            grouped[sharingType][floorNumber] = [];
        }
        
        grouped[sharingType][floorNumber].push(room);
    });
    
    return grouped;
}

// === Sharing Type Section (for when rooms exist) ===
function createSharingTypeSection(sharingType, floorsData) {
    const sharingDiv = document.createElement('div');
    sharingDiv.className = 'sharing-type-section';
    sharingDiv.setAttribute('data-sharing-type', sharingType);
    
    // Calculate stats for this sharing type from real data
    const floorNumbers = Object.keys(floorsData);
    const totalRooms = Object.values(floorsData).reduce((total, floorRooms) => total + floorRooms.length, 0);
    const capacity = parseInt(sharingType.split('-')[0]) || 2;
    // Assuming price is available on one of the rooms for calculation or fetched separately
    const price = floorsData[floorNumbers[0]]?.[0]?.price || 5000;

    
    // Create sharing type header with dropdown toggle and add room button
    const headerDiv = document.createElement('div');
    headerDiv.className = 'sharing-type-header';
    headerDiv.innerHTML = `
        <div class="sharing-type-info">
            <button class="dropdown-toggle" data-sharing-type="${sharingType}">
                <i class="fas fa-chevron-down"></i>
            </button>
            <div class="sharing-type-details">
                <h2>${sharingType}</h2>
                <div class="sharing-type-stats">
                    <span class="floor-count">${floorNumbers.length} ${floorNumbers.length === 1 ? 'floor' : 'floors'}</span>
                    <span class="room-count">${totalRooms} ${totalRooms === 1 ? 'room' : 'rooms'}</span>
                    <span class="price-info">₹${price} / bed</span>
                </div>
            </div>
        </div>
        <div class="sharing-type-actions">
            <button class="add-room-btn" data-sharing-type="${sharingType}">
                <i class="fas fa-plus"></i> Add Room
            </button>
        </div>
    `;
    sharingDiv.appendChild(headerDiv);
    
    // Create floors container (initially visible)
    const floorsContainer = document.createElement('div');
    floorsContainer.className = 'floors-container';
    floorsContainer.id = `floors-${sharingType.replace(/\s+/g, '-').toLowerCase()}`;
    
    // Add each floor section with real room data
    Object.keys(floorsData).sort((a, b) => a - b).forEach(floorNumber => {
        const floorSection = createFloorSection(floorNumber, floorsData[floorNumber]);
        floorsContainer.appendChild(floorSection);
    });
    
    sharingDiv.appendChild(floorsContainer);
    return sharingDiv;
}

function createFloorSection(floorNumber, rooms) {
    const floorDiv = document.createElement('div');
    floorDiv.className = 'floor-section';
    
    const heading = document.createElement('h3');
    heading.className = 'floor-heading';
    heading.textContent = `Floor ${floorNumber}`;
    floorDiv.appendChild(heading);
    
    const roomsContainer = document.createElement('div');
    roomsContainer.className = 'floor-rooms-container';
    
    // Add room cards for this floor using real API data
    rooms.forEach(room => {
        const roomCard = createRoomCard(room);
        roomsContainer.appendChild(roomCard);
    });
    
    floorDiv.appendChild(roomsContainer);
    return floorDiv;
}

function createRoomCard(room) {
    console.log("🏠 Creating card for real room:", room);
    
    // Use actual field names from your RoomDTO
    const roomId = room.roomId;
    const roomNumber = room.roomNumber;
    const floorNumber = room.floorNumber;
    const roomStatus = room.roomStatus;
    const currentOccupancy = room.currentOccupancy || 0;
    const sharingCapacity = room.sharingCapacity || 2;
    const sharingTypeName = room.sharingTypeName || `${sharingCapacity}-Sharing`;
    const price = room.price || 5000;
    
    const availableSpots = sharingCapacity - currentOccupancy;
    const statusClass = getRoomStatusClass(roomStatus);
    const availabilityText = getAvailabilityText(roomStatus, availableSpots);
    const occupancyStatus = room.occupancyStatus || `${currentOccupancy}/${sharingCapacity}`;
    
    const roomCard = document.createElement('div');
    roomCard.className = `room-card ${statusClass}`;
    roomCard.setAttribute('data-room-id', roomId);
    roomCard.setAttribute('data-floor', floorNumber);
    roomCard.setAttribute('data-sharing-type', sharingTypeName);
    
    roomCard.innerHTML = `
        <div class="room-card-header">
            <div class="room-number-group">
                <span class="room-number">${roomNumber}</span>
                <span class="beds-info">Available: <span class="beds-available-count">${occupancyStatus}</span> beds</span>
            </div>
        </div>
        <div class="room-details-content">
            <ul class="room-meta-list">
                <li><strong>Sharing Type:</strong> <span>${sharingTypeName}</span></li>
                <li><strong>Availability:</strong> <span class="available-status">${availabilityText}</span></li>
                <li><strong>Price:</strong> <span class="price">₹${price} <span class="price-unit">/Bed</span></span></li>
            </ul>
            <div class="room-actions">
                <button class="btn-primary book-now-btn" data-room="${roomNumber}" data-room-id="${roomId}" ${availableSpots === 0 || roomStatus !== 'Available' ? 'disabled' : ''}>
                    <i class="fas fa-bookmark"></i> ${availableSpots === 0 ? 'Full' : 'Book Now'}
                </button>
                <button class="btn-secondary details-btn" data-room="${roomNumber}" data-room-id="${roomId}">
                    <i class="fas fa-info-circle"></i> View Details
                </button>
            </div>
        </div>
    `;
    
    return roomCard;
}

function getRoomStatusClass(status) {
    switch(status) {
        case 'Available': return 'available-room';
        case 'Occupied': return 'occupied-room';
        case 'Maintenance': return 'maintenance-room';
        default: return 'available-room';
    }
}

function getAvailabilityText(status, availableSpots) {
    switch(status) {
        case 'Available': 
            return availableSpots === 1 ? '1 spot available' : `${availableSpots} spots available`;
        case 'Occupied': return 'Fully occupied';
        case 'Maintenance': return 'Under maintenance';
        default: return 'Available';
    }
}

// Initialize dropdown toggles for sharing sections
function initializeDropdownToggles() {
    const dropdownToggles = document.querySelectorAll('.dropdown-toggle');
    console.log("📂 Initializing dropdown toggles:", dropdownToggles.length);
    
    dropdownToggles.forEach(toggle => {
        toggle.addEventListener('click', function(e) {
            e.stopPropagation();
            const sharingType = this.getAttribute('data-sharing-type');
            const sharingSection = this.closest('.sharing-type-section');
            const floorsContainer = sharingSection.querySelector('.floors-container');
            const icon = this.querySelector('i');
            
            if (floorsContainer.style.display === 'none') {
                // Expand
                floorsContainer.style.display = 'block';
                icon.className = 'fas fa-chevron-down';
                this.setAttribute('aria-expanded', 'true');
                console.log(`📂 Expanded ${sharingType} section`);
            } else {
                // Collapse
                floorsContainer.style.display = 'none';
                icon.className = 'fas fa-chevron-right';
                this.setAttribute('aria-expanded', 'false');
                console.log(`📂 Collapsed ${sharingType} section`);
            }
        });
    });
}

// Initialize Add Room buttons
function initializeAddRoomButtons() {
    const addRoomButtons = document.querySelectorAll('.add-room-btn');
    console.log("➕ Initializing add room buttons:", addRoomButtons.length);
    
    addRoomButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation();
            const sharingType = this.getAttribute('data-sharing-type');
            const capacity = this.getAttribute('data-capacity');
            const price = this.getAttribute('data-price');
            console.log("➕ Add room clicked for sharing type:", sharingType);
            
            if (capacity && price) {
                // From empty sharing type section
                addNewRoomWithSharingType(sharingType, capacity, price);
            } else {
                // From regular sharing type section with rooms (capacity/price will be null/undefined)
                // We need to infer the capacity from the sharingType string
                const inferredCapacity = parseInt(sharingType.split('-')[0]) || 2;
                addNewRoom(sharingType, inferredCapacity);
            }
        });
    });
}

// Add new room function for sharing types with existing rooms
function addNewRoom(sharingType, capacity) {
    console.log(`➕ Opening add room modal for: ${sharingType}`);
    
    // Create modal for adding new room
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>Add New Room - ${sharingType}</h3>
                <button class="close-modal">×</button>
            </div>
            <div class="modal-body">
                <form id="addRoomForm">
                    <div class="form-group">
                        <label>Room Number:</label>
                        <input type="text" id="roomNumber" placeholder="Enter room number" required>
                    </div>
                    <div class="form-group">
                        <label>Floor Number:</label>
                        <input type="number" id="floorNumber" min="1" max="10" placeholder="Enter floor number" required>
                    </div>
                    <div class="form-group">
                        <label>Sharing Type:</label>
                        <div class="readonly-field">${sharingType}</div>
                    </div>
                    <div class="form-group">
                        <label>Price per Bed:</label>
                        <input type="number" id="roomPrice" min="1000" step="500" placeholder="Enter price per bed" required>
                    </div>
                    <div class="form-actions">
                        <button type="button" class="btn-secondary cancel-btn">Cancel</button>
                        <button type="submit" class="btn-primary">Add Room</button>
                    </div>
                </form>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Close modal events
    const closeModal = () => modal.remove();
    
    modal.querySelector('.close-modal').addEventListener('click', closeModal);
    modal.querySelector('.cancel-btn').addEventListener('click', closeModal);
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeModal();
    });
    
    // Form submission
    modal.querySelector('#addRoomForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const roomNumber = modal.querySelector('#roomNumber').value;
        const floorNumber = modal.querySelector('#floorNumber').value;
        const price = modal.querySelector('#roomPrice').value;
        
        console.log(`📝 Adding new room: ${roomNumber}, Floor ${floorNumber}, ${sharingType}, ₹${price}`);
        
        try {
            // API call to add room
            const response = await fetch("/api/rooms/add-room", {
                method: "POST",
                credentials: "include",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    roomNumber: roomNumber,
                    floorNumber: parseInt(floorNumber),
                    sharingType: sharingType,
                    price: parseInt(price),
                    capacity: capacity
                })
            });

            if (response.ok) {
                showNotification(`Room ${roomNumber} added successfully!`, 'success');
                closeModal();
                
                // Refresh room data from API
                await getRoomDetails();
            } else {
                showNotification("Failed to add room. Please try again.", 'error');
            }
        } catch (error) {
            console.error("Error adding room:", error);
            showNotification("Error adding room. Please try again.", 'error');
        }
    });
}

// Add new room function for empty sharing types
function addNewRoomWithSharingType(sharingType, capacity, price) {
    console.log(`➕ Opening add room modal for empty sharing type: ${sharingType}, Capacity: ${capacity}, Price: ${price}`);
    
    // Create modal for adding new room with pre-filled sharing type and price
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>Add New Room - ${sharingType}</h3>
                <button class="close-modal">×</button>
            </div>
            <div class="modal-body">
                <form id="addRoomForm">
                    <div class="form-group">
                        <label>Room Number:</label>
                        <input type="text" id="roomNumber" placeholder="e.g., 101, G01, etc." required>
                    </div>
                    <div class="form-group">
                        <label>Floor Number:</label>
                        <input type="number" id="floorNumber" min="0" max="10" value="1" required>
                    </div>
                    <div class="form-group">
                        <label>Sharing Type:</label>
                        <div class="readonly-field">${sharingType} (Capacity: ${capacity})</div>
                        <input type="hidden" id="roomCapacity" value="${capacity}">
                    </div>
                    <div class="form-group">
                        <label>Price per Bed:</label>
                        <input type="number" id="roomPrice" min="1000" step="500" value="${price}" required>
                    </div>
                    <div class="form-actions">
                        <button type="button" class="btn-secondary cancel-btn">Cancel</button>
                        <button type="submit" class="btn-primary">Add Room</button>
                    </div>
                </form>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Close modal events
    const closeModal = () => modal.remove();
    
    modal.querySelector('.close-modal').addEventListener('click', closeModal);
    modal.querySelector('.cancel-btn').addEventListener('click', closeModal);
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeModal();
    });
    
    // Form submission
    modal.querySelector('#addRoomForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const roomNumber = modal.querySelector('#roomNumber').value;
        const floorNumber = modal.querySelector('#floorNumber').value;
        const roomPrice = modal.querySelector('#roomPrice').value;
        const roomCapacity = modal.querySelector('#roomCapacity').value; // Get capacity from hidden field
        
        console.log(`📝 Adding new room: ${roomNumber}, Floor ${floorNumber}, ${sharingType}, ₹${roomPrice}`);
        
        try {
            // API call to add room
            const response = await fetch("/api/rooms/add-room", {
                method: "POST",
                credentials: "include",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    roomNumber: roomNumber,
                    floorNumber: parseInt(floorNumber),
                    sharingType: sharingType,
                    price: parseInt(roomPrice),
                    capacity: parseInt(roomCapacity) // Use the capacity
                })
            });

            if (response.ok) {
                showNotification(`Room ${roomNumber} added successfully!`, 'success');
                closeModal();
                
                // Refresh room data from API
                await getRoomDetails();
            } else {
                showNotification("Failed to add room. Please try again.", 'error');
            }
        } catch (error) {
            console.error("Error adding room:", error);
            showNotification("Error adding room. Please try again.", 'error');
        }
    });
}