document.addEventListener("DOMContentLoaded", () => {
    console.log("Dashboard loading - starting sequential operations");
    setDefaultDashboardValues();
    initializeDashboardUI();
    loadDataSequentially();
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
    initializeAddSharingTypeNavButton();
    console.log("UI components initialized");
}

function initializeAddSharingTypeNavButton() {
    const addTypeBtn = document.querySelector('.nav-button.add-type-btn');

    if (addTypeBtn) {
        console.log("➕ Add Sharing Type nav button found");

        addTypeBtn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            console.log("➕ Add Sharing Type nav button clicked");
            showAddSharingTypeModal();
        });

        addTypeBtn.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                console.log("➕ Add Sharing Type nav button activated via keyboard");
                showAddSharingTypeModal();
            }
        });
    } else {
        console.log("❌ Add Sharing Type nav button not found");
    }
}

function clearMainContentContainer() {
    const container = document.querySelector('.room-section.expanded');
    if (container) {
        container.innerHTML = '';
        container.style.display = 'block';
        console.log("🧹 Main content container cleared.");
    }
    const globalContainer = document.querySelector('.empty-state-global-container');
    if (globalContainer) {
        globalContainer.remove();
        console.log("🧹 Removed any existing global empty state.");
    }
}

async function loadDataSequentially() {
    console.log("🚀 >>>>> loadDataSequentially() STARTED <<<<<");
    clearMainContentContainer();

    try {
        // Step 1: Load admin details first
        console.log("👤 >>>>> STEP 1: Calling getAdminDetails() <<<<<");
        const adminDetails = await getAdminDetails();
        console.log("✅ >>>>> STEP 1 COMPLETE: Admin details loaded <<<<<");

        // Step 2: Load sharing types
        console.log("🏠 >>>>> STEP 2: Calling getSharingTypes() <<<<<");
        const sharingTypes = await getSharingTypes();
        console.log("✅ >>>>> STEP 2 COMPLETE: Sharing types loaded:", sharingTypes.length);
        console.log("📊 >>>>> Sharing types data:", sharingTypes);

        // Step 3: Load room details
        console.log("🚪 >>>>> STEP 3: Calling getRoomDetails() <<<<<");
        const roomDetails = await getRoomDetails(sharingTypes);
        console.log("✅ >>>>> STEP 3 COMPLETE: Room details loaded:", roomDetails.length);

        console.log("🎉 >>>>> ALL DATA LOADED SUCCESSFULLY <<<<<");

    } catch (error) {
        console.error("💥 >>>>> ERROR in loadDataSequentially:", error);
        showNotification("Error loading dashboard data", "error");
    }
}

// Admin details function
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
            throw new Error("Failed to load admin details");
        }
    } catch (error) {
        console.error("Error fetching admin details:", error);
        showNotification("Error loading admin details", "error");
        throw error;
    }
}

// Get sharing types
async function getSharingTypes() {
    console.log("🔄 >>>>> getSharingTypes() FUNCTION STARTED <<<<<");
    
    try {
        console.log("📡 >>>>> ABOUT TO MAKE FETCH CALL <<<<<");
        const response = await fetch("/api/auth/sharing-details", {
            method: "GET",
            credentials: "include",
            headers: {
                'Content-Type': 'application/json',
            }
        });

        console.log("📡 >>>>> FETCH COMPLETED, Status:", response.status);
        
        if (response.ok) {
            const sharingData = await response.json();
            console.log("📡 >>>>> RESPONSE PARSED:", sharingData);
            return sharingData;
        } else {
            console.error("❌ >>>>> FETCH FAILED:", response.status);
            return [];
        }
    } catch (error) {
        console.error("❌ >>>>> FETCH ERROR:", error);
        return [];
    }
}

// Get room details (Accepts pre-fetched sharing types) - FIXED VERSION
async function getRoomDetails(sharingTypes) {
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

            if (Array.isArray(roomsData) && roomsData.length > 0) {
                displayDashboardStructure(sharingTypes, roomsData);
                return roomsData;
            } else {
                // No rooms but sharing types exist
                displayDashboardStructure(sharingTypes, []);
                return [];
            }
        } else {
            console.error("Failed to fetch room details:", response.status);
            showNotification("Failed to load room details", "error");

            // Still try to show structure if sharing types were loaded
            if (sharingTypes && sharingTypes.length > 0) {
                displayDashboardStructure(sharingTypes, []);
                return [];
            } else {
                showGlobalEmptyState();
                return [];
            }
        }
    } catch (error) {
        console.error("Error fetching room details:", error);
        showNotification("Error loading room details", "error");

        // Still try to show structure if sharing types were loaded
        if (sharingTypes && sharingTypes.length > 0) {
            displayDashboardStructure(sharingTypes, []);
            return [];
        } else {
            showGlobalEmptyState();
            return [];
        }
    }
}

function displayDashboardStructure(sharingTypes, rooms) {
    console.log("🎨 Building dashboard structure:", {
        sharingTypesCount: sharingTypes.length,
        roomsCount: rooms.length
    });

    const roomSection = document.querySelector('.room-section.expanded');

    if (!roomSection) {
        console.error("❌ Room section not found!");
        return;
    }

    clearMainContentContainer();

    if (sharingTypes.length === 0) {
        console.log("No sharing types configured");
        showGlobalEmptyState();
        return;
    }

    console.log("✅ Displaying dashboard with", sharingTypes.length, "sharing types");

    // Group rooms by sharing type ID and floor - FIXED: Use sharingTypeId instead of name
    const roomsBySharingIdAndFloor = groupRoomsBySharingTypeAndFloor(rooms);
    console.log("📊 Rooms grouped by sharing type ID:", roomsBySharingIdAndFloor);

    // Create sections for each sharing type
    sharingTypes.forEach(sharingType => {
        const sharingTypeId = sharingType.sharingTypeId;
        const sharingData = roomsBySharingIdAndFloor[sharingTypeId];
        
        console.log(`🔄 Processing sharing type: ${sharingType.typeName} (ID: ${sharingTypeId})`);

        const sharingRooms = sharingData ? sharingData.floors : {};
        console.log(`📦 Rooms for ${sharingType.typeName}:`, sharingRooms);

        const sharingSection = createSharingTypeSection(sharingType, sharingRooms);
        roomSection.appendChild(sharingSection);
        console.log(`✅ Added sharing type section: ${sharingType.typeName}`);
    });

    // Initialize interactions
    initializeRoomCardInteractions();
    initializeDropdownToggles();
    initializeAddRoomButtons();
    initializeAddSharingTypeButton();

    console.log("✅ Dashboard structure built successfully");
}

function groupRoomsBySharingTypeAndFloor(rooms) {
    const grouped = {};

    if (!rooms || !Array.isArray(rooms)) {
        console.log("⚠️ No rooms provided for grouping");
        return grouped;
    }

    console.log("🔍 Grouping", rooms.length, "rooms by sharing type ID and floor");

    rooms.forEach(room => {
        // FIXED: Group by sharingTypeId instead of sharing type name
        const sharingTypeId = room.sharingTypeId;
        const sharingTypeName = room.sharingTypeName || `${room.sharingCapacity || 1}-Sharing`;
        const floorNumber = String(room.floorNumber || 1);

        console.log(`🏠 Processing room: ${room.roomNumber}, SharingID: ${sharingTypeId}, Sharing: ${sharingTypeName}, Floor: ${floorNumber}`);

        // Use sharingTypeId as the key instead of sharingTypeName
        if (!grouped[sharingTypeId]) {
            grouped[sharingTypeId] = {
                sharingTypeName: sharingTypeName,
                floors: {}
            };
            console.log(`📁 Created new sharing type group: ${sharingTypeName} (ID: ${sharingTypeId})`);
        }

        if (!grouped[sharingTypeId].floors[floorNumber]) {
            grouped[sharingTypeId].floors[floorNumber] = [];
            console.log(`📂 Created new floor group: ${sharingTypeName} - Floor ${floorNumber}`);
        }

        grouped[sharingTypeId].floors[floorNumber].push(room);
        console.log(`✅ Added room ${room.roomNumber} to ${sharingTypeName} (ID: ${sharingTypeId}) - Floor ${floorNumber}`);
    });

    console.log("📊 Final grouped structure:", grouped);
    return grouped;
}

function createSharingTypeSection(sharingType, floorsData) {
    // Use the actual typeName from the API response
    const sharingTypeName = sharingType.typeName || `${sharingType.sharingCapacity || 1}-Sharing`;
    const capacity = sharingType.sharingCapacity || sharingType.capacity || 1;
    const price = sharingType.sharingFee || sharingType.price || 5000;
    const sharingTypeId = sharingType.sharingTypeId;

    console.log(`🏗️ Creating section for: ${sharingTypeName} (ID: ${sharingTypeId}, Capacity: ${capacity}, Price: ${price})`);

    const floorNumbers = Object.keys(floorsData || {});
    const totalRooms = Object.values(floorsData || {}).reduce((total, floorRooms) => total + floorRooms.length, 0);

    console.log(`📊 Section stats - Floors: ${floorNumbers.length}, Total Rooms: ${totalRooms}`);

    const sharingDiv = document.createElement('div');
    sharingDiv.className = 'sharing-type-section';
    sharingDiv.setAttribute('data-sharing-type', sharingTypeName);
    sharingDiv.setAttribute('data-sharing-id', sharingTypeId || '');
    sharingDiv.setAttribute('data-capacity', capacity);

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
                    <span class="floor-count">${floorNumbers.length} ${floorNumbers.length === 1 ? 'floor' : 'floors'}</span>
                    <span class="room-count">${totalRooms} ${totalRooms === 1 ? 'room' : 'rooms'}</span>
                    <span class="price-info">₹${price} / bed</span>
                </div>
            </div>
        </div>
        <div class="sharing-type-actions">
            <button class="add-room-btn" data-sharing-type="${sharingTypeName}" data-capacity="${capacity}" data-price="${price}" data-sharing-id="${sharingTypeId || ''}">
                <i class="fas fa-plus"></i> Add Room
            </button>
        </div>
    `;
    sharingDiv.appendChild(headerDiv);

    const floorsContainer = document.createElement('div');
    floorsContainer.className = 'floors-container';
    floorsContainer.id = `floors-${sharingTypeName.replace(/\s+/g, '-').toLowerCase()}`;

    // Handle empty state or create floor sections
    if (totalRooms === 0) {
        console.log(`📭 No rooms found for sharing type: ${sharingTypeName}, showing empty state`);
        floorsContainer.innerHTML = `
            <div class="empty-room-state-container">
                <div class="empty-room-content">
                    <i class="fas fa-door-open"></i>
                    <h4>No Rooms Added Yet</h4>
                    <p>This sharing type is present, but doesn't contain any rooms/floors. Click the <strong>"Add Room"</strong> button above to create the first room.</p>
                </div>
            </div>
        `;
    } else {
        console.log(`🏢 Creating floor sections for ${sharingTypeName} with ${floorNumbers.length} floors`);
        // Create floor sections with rooms
        Object.keys(floorsData).sort((a, b) => parseInt(a) - parseInt(b)).forEach(floorNumber => {
            const floorSection = createFloorSection(floorNumber, floorsData[floorNumber]);
            floorsContainer.appendChild(floorSection);
            console.log(`✅ Added floor ${floorNumber} for ${sharingTypeName}`);
        });
    }

    sharingDiv.appendChild(floorsContainer);
    console.log(`✅ Completed section for: ${sharingTypeName}`);
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

    // Add room cards for this floor
    rooms.forEach(room => {
        const roomCard = createRoomCard(room);
        roomsContainer.appendChild(roomCard);
    });

    floorDiv.appendChild(roomsContainer);
    return floorDiv;
}

function createRoomCard(room) {
    console.log("🏠 Creating card for room:", room.roomNumber);

    const roomId = room.roomId;
    
    // Extract display room number (remove capacity prefix)
    const fullRoomNumber = room.roomNumber;
    let displayRoomNumber = fullRoomNumber;
    
    // Remove the capacity digit from the beginning for display
    if (/^\d+/.test(fullRoomNumber)) {
        displayRoomNumber = fullRoomNumber.substring(1); // Remove first character (capacity)
    }
    
    const floorNumber = room.floorNumber;
    const roomStatus = room.roomStatus;
    const currentOccupancy = room.currentOccupancy || 0;
    
    // Use actual sharingTypeName from room data instead of generating from capacity
    const sharingTypeName = room.sharingTypeName || `${room.sharingCapacity || 2}-Sharing`;
    const sharingCapacity = room.sharingCapacity || 2;
    const price = room.price || 5000;

    const availableSpots = sharingCapacity - currentOccupancy;
    const statusClass = getRoomStatusClass(roomStatus);
    const availabilityText = getAvailabilityText(roomStatus, availableSpots);
    const occupancyStatus = room.occupancyStatus || `${currentOccupancy}/${sharingCapacity}`;

    // Create the display format: "1-201" (SharingType-RoomNumber)
    const sharingTypePrefix = sharingCapacity; // Get just the number from sharing type
    const roomDisplay = `${sharingTypePrefix}-${displayRoomNumber}`;

    const roomCard = document.createElement('div');
    roomCard.className = `room-card ${statusClass}`;
    roomCard.setAttribute('data-room-id', roomId);
    roomCard.setAttribute('data-floor', floorNumber);
    roomCard.setAttribute('data-sharing-type', sharingTypeName);

    roomCard.innerHTML = `
        <div class="room-card-header">
            <div class="room-number-group">
                <span class="room-number">${roomDisplay}</span>
                <span class="beds-info">Available: <span class="beds-available-count">${occupancyStatus}</span> beds</span>
            </div>
        </div>
        <div class="room-details-content">
            <ul class="room-meta-list">
                <li><strong>Sharing Type:</strong> <span>${sharingTypeName}</span></li>
                <li><strong>Availability:</strong> <span class="available-status ${availableSpots === 0 ? 'filled' : ''}">${availabilityText}</span></li>
                <li><strong>Price:</strong> <span class="price">₹${price} <span class="price-unit">/Bed</span></span></li>
            </ul>
            <div class="room-actions">
                <button class="book-now-btn" data-room="${roomDisplay}" data-room-id="${roomId}" ${availableSpots === 0 || roomStatus !== 'Available' ? 'disabled' : ''}>
                    <i class="fas fa-bookmark"></i> ${availableSpots === 0 ? 'Full' : 'Book Now'}
                </button>
                <button class="details-btn" data-room="${roomDisplay}" data-room-id="${roomId}">
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

function showGlobalEmptyState() {
    console.log("⚠️ Displaying GLOBAL empty state: No sharing types exist.");
    const container = document.querySelector('.room-section.expanded');

    if (!container) {
         console.error("❌ Main room section container not found!");
         return;
    }

    clearMainContentContainer();

    container.innerHTML = `
        <div class="empty-state-global-container" style="text-align: center; padding: 50px 20px; min-height: 200px;">
            <div class="empty-state-content">
                <i class="fas fa-home" style="font-size: 40px; color: #007bff;"></i>
                <h3 style="margin-top: 15px;">No Sharing Types Configured</h3>
                <p>Start by creating sharing types to organize your hostel rooms</p>
            </div>
        </div>
    `;

    container.style.display = 'block';

    // Initialize the add sharing type button
    initializeAddSharingTypeButton();
}

function initializeAddSharingTypeButton() {
    const addSharingTypeBtn = document.getElementById('addSharingTypeBtn');
    if (addSharingTypeBtn) {
        addSharingTypeBtn.addEventListener('click', function() {
            console.log("➕ Add Sharing Type button clicked");
            showAddSharingTypeModal();
        });
    }
}

function showAddSharingTypeModal() {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay active';

    // Add CSS for proper modal styling
    const modalStyles = `
        <style>
            .modal-overlay.active {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.6);
                backdrop-filter: blur(5px);
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 1000;
                opacity: 1;
                transition: opacity 0.3s ease;
            }
            .modal-overlay.active .modal-content {
                background: white;
                border-radius: 12px;
                box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
                width: 90%;
                max-width: 500px;
                max-height: 90vh;
                overflow-y: auto;
                transform: scale(1);
                transition: transform 0.3s ease;
                border: 1px solid #e0e0e0;
            }
            .modal-header {
                padding: 20px 24px;
                border-bottom: 1px solid #e8e8e8;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                border-radius: 12px 12px 0 0;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }
            .modal-header h3 {
                margin: 0;
                font-size: 1.4rem;
                font-weight: 600;
            }
            .close-modal {
                background: rgba(255, 255, 255, 0.2);
                border: none;
                color: white;
                font-size: 24px;
                cursor: pointer;
                width: 32px;
                height: 32px;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: background 0.2s ease;
            }
            .close-modal:hover {
                background: rgba(255, 255, 255, 0.3);
            }
            .modal-body {
                padding: 24px;
            }
            .form-group {
                margin-bottom: 20px;
            }
            .form-group label {
                display: block;
                margin-bottom: 8px;
                font-weight: 600;
                color: #333;
                font-size: 0.95rem;
            }
            .form-group input,
            .form-group select,
            .form-group textarea {
                width: 100%;
                padding: 12px 16px;
                border: 2px solid #e8e8e8;
                border-radius: 8px;
                font-size: 1rem;
                transition: all 0.3s ease;
                box-sizing: border-box;
            }
            .form-group input:focus,
            .form-group select:focus,
            .form-group textarea:focus {
                outline: none;
                border-color: #667eea;
                box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
            }
            .form-group textarea {
                resize: vertical;
                min-height: 80px;
                font-family: inherit;
            }
            .readonly-field {
                padding: 12px 16px;
                background: #f8f9fa;
                border: 2px solid #e8e8e8;
                border-radius: 8px;
                color: #666;
                font-size: 1rem;
            }
            .form-actions {
                display: flex;
                gap: 12px;
                justify-content: flex-end;
                margin-top: 24px;
                padding-top: 20px;
                border-top: 1px solid #e8e8e8;
            }
            .btn-primary, .btn-secondary {
                padding: 12px 24px;
                border: none;
                border-radius: 8px;
                font-size: 1rem;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.3s ease;
                min-width: 100px;
            }
            .btn-primary {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
            }
            .btn-primary:hover:not(:disabled) {
                transform: translateY(-2px);
                box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
            }
            .btn-primary:disabled {
                opacity: 0.6;
                cursor: not-allowed;
                transform: none !important;
            }
            .btn-secondary {
                background: #f8f9fa;
                color: #666;
                border: 2px solid #e8e8e8;
            }
            .btn-secondary:hover {
                background: #e9ecef;
                border-color: #ddd;
            }
            small {
                display: block;
                margin-top: 6px;
                font-size: 0.85rem;
                color: #666;
            }
            #capacityExistsMsg {
                color: #dc3545;
                font-weight: 500;
            }
            .loading-spinner {
                display: inline-block;
                width: 16px;
                height: 16px;
                border: 2px solid #ffffff;
                border-radius: 50%;
                border-top-color: transparent;
                animation: spin 1s ease-in-out infinite;
                margin-right: 8px;
            }
            @keyframes spin {
                to { transform: rotate(360deg); }
            }
            .validation-error {
                border-color: #dc3545 !important;
                background-color: #fff5f5;
            }
            .validation-success {
                border-color: #28a745 !important;
                background-color: #f8fff9;
            }
        </style>
    `;

    modal.innerHTML = modalStyles + `
        <div class="modal-content">
            <div class="modal-header">
                <h3>Add New Sharing Type</h3>
                <button class="close-modal">×</button>
            </div>
            <div class="modal-body">
                <form id="addSharingTypeForm">
                    <div class="form-group">
                        <label for="sharingCapacity">Sharing Capacity:</label>
                        <select id="sharingCapacity" required>
                            <option value="">Select capacity</option>
                            <option value="1">1-Sharing (Single)</option>
                            <option value="2">2-Sharing</option>
                            <option value="3">3-Sharing</option>
                            <option value="4">4-Sharing</option>
                            <option value="5">5-Sharing</option>
                            <option value="6">6-Sharing</option>
                        </select>
                        <small id="capacityExistsMsg" style="color: red; display: none; margin-top: 8px;">
                            <i class="fas fa-exclamation-triangle"></i> This sharing type already exists for your hostel.
                        </small>
                    </div>
                    <div class="form-group">
                        <label for="sharingFee">Price per Bed (₹):</label>
                        <input type="number" id="sharingFee" min="1000" step="500" placeholder="Enter price per bed" required>
                        <small>Minimum price: ₹1000 per bed</small>
                    </div>
                    <div class="form-group">
                        <label for="sharingDescription">Description (Optional):</label>
                        <textarea id="sharingDescription" placeholder="Brief description of this sharing type (e.g., 'Premium single rooms', 'Economy shared rooms')"></textarea>
                    </div>
                    <div class="form-actions">
                        <button type="button" class="btn-secondary cancel-btn">Cancel</button>
                        <button type="submit" class="btn-primary" id="submitSharingBtn">
                            Add Sharing Type
                        </button>
                    </div>
                </form>
            </div>
        </div>
    `;

    document.body.appendChild(modal);

    // Prevent background scrolling when modal is open
    document.body.style.overflow = 'hidden';

    // Close modal function
    const closeModal = () => {
        modal.classList.remove('active');
        document.body.style.overflow = '';
        setTimeout(() => {
            if (modal.parentNode) {
                modal.parentNode.removeChild(modal);
            }
        }, 300);
    };

    // Close modal events
    modal.querySelector('.close-modal').addEventListener('click', closeModal);
    modal.querySelector('.cancel-btn').addEventListener('click', closeModal);
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeModal();
    });

    // Escape key to close modal
    document.addEventListener('keydown', function escapeHandler(e) {
        if (e.key === 'Escape') {
            closeModal();
            document.removeEventListener('keydown', escapeHandler);
        }
    });

    // Real-time validation for capacity - FIXED VERSION
    const capacitySelect = modal.querySelector('#sharingCapacity');
    const existsMsg = modal.querySelector('#capacityExistsMsg');
    const submitBtn = modal.querySelector('#submitSharingBtn');
    const form = modal.querySelector('#addSharingTypeForm');

    // Store current admin's sharing types for validation
    let currentAdminSharingTypes = [];

    // Function to load current admin's sharing types
    const loadCurrentAdminSharingTypes = async () => {
        try {
            console.log("🔍 Loading current admin's sharing types for validation...");
            const response = await fetch("/api/auth/sharing-details", {
                method: "GET",
                credentials: "include"
            });

            if (response.ok) {
                currentAdminSharingTypes = await response.json();
                console.log("✅ Current admin sharing types loaded:", currentAdminSharingTypes);
            } else {
                console.error("❌ Failed to load sharing types for validation");
            }
        } catch (error) {
            console.error("❌ Error loading sharing types for validation:", error);
        }
    };

    // Load sharing types when modal opens
    loadCurrentAdminSharingTypes();

    capacitySelect.addEventListener('change', async function() {
        const capacity = this.value;

        if (capacity) {
            console.log(`🔍 Checking if ${capacity}-Sharing already exists for current admin...`);

            // Check against current admin's sharing types only
            const alreadyExists = currentAdminSharingTypes.some(type =>
                type.capacity == capacity || type.sharingCapacity == capacity
            );

            if (alreadyExists) {
                existsMsg.style.display = 'block';
                submitBtn.disabled = true;
                capacitySelect.classList.add('validation-error');
                capacitySelect.classList.remove('validation-success');
                console.log(`❌ ${capacity}-Sharing already exists for current admin`);
            } else {
                existsMsg.style.display = 'none';
                submitBtn.disabled = false;
                capacitySelect.classList.remove('validation-error');
                capacitySelect.classList.add('validation-success');
                console.log(`✅ ${capacity}-Sharing is available for current admin`);
            }
        } else {
            existsMsg.style.display = 'none';
            submitBtn.disabled = false;
            capacitySelect.classList.remove('validation-error', 'validation-success');
        }
    });

    // Form submission with server-side validation as backup
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const capacity = capacitySelect.value;
        const sharingFee = modal.querySelector('#sharingFee').value;
        const description = modal.querySelector('#sharingDescription').value;

        console.log(`📝 Attempting to add new sharing type: ${capacity}-Sharing, ₹${sharingFee}`);

        // Validate inputs
        if (!capacity || !sharingFee) {
            showNotification("Please fill all required fields", "error");
            return;
        }

        if (parseInt(sharingFee) < 1000) {
            showNotification("Price per bed must be at least ₹1000", "error");
            modal.querySelector('#sharingFee').focus();
            return;
        }

        // Double-check client-side validation
        const alreadyExists = currentAdminSharingTypes.some(type =>
            type.capacity == capacity || type.sharingCapacity == capacity
        );

        if (alreadyExists) {
            showNotification("This sharing type already exists for your hostel", "error");
            capacitySelect.focus();
            return;
        }

        try {
            // Update button to show loading state
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<div class="loading-spinner"></div> Adding...';

            const response = await fetch("/api/auth/add-sharing-type", {
                method: "POST",
                credentials: "include",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    capacity: parseInt(capacity),
                    sharingFee: parseInt(sharingFee),
                    description: description || ""
                })
            });

            const result = await response.json();

            if (result.success) {
                showNotification(result.message, 'success');
                // Don't close modal immediately - show success state briefly
                submitBtn.innerHTML = '<i class="fas fa-check"></i> Success!';
                submitBtn.style.background = 'linear-gradient(135deg, #28a745 0%, #20c997 100%)';

                // Wait a moment to show success, then close and refresh
                setTimeout(async () => {
                    closeModal();
                    // Refresh the dashboard to show new sharing type
                    await refreshDashboard();
                }, 1000);

            } else {
                // Handle server-side validation errors
                if (result.message && result.message.toLowerCase().includes('already exists')) {
                    showNotification("This sharing type already exists for your hostel", "error");
                    existsMsg.style.display = 'block';
                    submitBtn.disabled = true;
                    capacitySelect.classList.add('validation-error');
                } else {
                    showNotification(result.message || "Error adding sharing type", "error");
                }

                // Reset button state on error
                submitBtn.disabled = false;
                submitBtn.innerHTML = 'Add Sharing Type';
            }
        } catch (error) {
            console.error("Error adding sharing type:", error);
            showNotification("Error adding sharing type. Please try again.", 'error');
            // Reset button state on error
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Add Sharing Type';
        }
    });

    // Initialize form
    capacitySelect.dispatchEvent(new Event('change'));

    // Focus on first input
    setTimeout(() => {
        capacitySelect.focus();
    }, 100);
}

async function refreshDashboard() {
    console.log("🔄 Starting dashboard refresh...");

    try {
        // Clear current content
        clearMainContentContainer();

        // Show loading state
        const container = document.querySelector('.room-section.expanded');
        if (container) {
            container.innerHTML = `
                <div class="loading-state" style="text-align: center; padding: 40px;">
                    <i class="fas fa-spinner fa-spin" style="font-size: 24px; color: #007bff;"></i>
                    <p style="margin-top: 10px;">Refreshing dashboard...</p>
                </div>
            `;
        }

        // Reload all data sequentially
        await loadDataSequentially();

        console.log("✅ Dashboard refresh completed");
    } catch (error) {
        console.error("❌ Error refreshing dashboard:", error);
        showNotification("Error refreshing dashboard", "error");
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

function initializeAddRoomButtons() {
    const addRoomButtons = document.querySelectorAll('.add-room-btn');
    console.log("➕ Initializing add room buttons:", addRoomButtons.length);

    addRoomButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation();
            const sharingType = this.getAttribute('data-sharing-type');
            const capacity = this.getAttribute('data-capacity');
            const price = this.getAttribute('data-price');
            const sharingId = this.getAttribute('data-sharing-id');

            console.log("➕ Add room clicked for sharing type:", sharingType);

            addNewRoom(sharingType, capacity, price, sharingId);
        });
    });
}

function initializeRoomCardInteractions() {
    console.log("🔄 Initializing room card interactions");

    // Book Now button functionality
    document.querySelectorAll('.book-now-btn').forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation();
            const roomNumber = this.getAttribute('data-room');
            const roomId = this.getAttribute('data-room-id');

            if (!this.disabled) {
                console.log(`Booking room: ${roomNumber} (ID: ${roomId})`);
                // Implement booking logic here
                showNotification(`Booking initiated for room ${roomNumber}`, 'info');
            }
        });
    });

    // View Details button functionality
    document.querySelectorAll('.details-btn').forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation();
            const roomNumber = this.getAttribute('data-room');
            const roomId = this.getAttribute('data-room-id');

            console.log(`Viewing details for room: ${roomNumber} (ID: ${roomId})`);
            // Implement view details logic here
            showNotification(`Showing details for room ${roomNumber}`, 'info');
        });
    });

    // Room card click functionality (optional)
    document.querySelectorAll('.room-card').forEach(card => {
        card.addEventListener('click', function(e) {
            // Don't trigger if user clicked on a button inside the card
            if (!e.target.closest('button')) {
                const roomNumber = this.querySelector('.room-number').textContent;
                const roomId = this.getAttribute('data-room-id');
                console.log(`Room card clicked: ${roomNumber} (ID: ${roomId})`);
                // You can add room card click logic here
            }
        });
    });
}

function addNewRoom(sharingType, capacity, price, sharingId) {
    console.log(`➕ Opening add room modal for: ${sharingType}, Capacity: ${capacity}, Price: ${price}`);

    const modal = document.createElement('div');
    modal.className = 'modal-overlay active';

    // Add CSS for proper modal styling
  

    modal.innerHTML =  `
        <div class="modal-content">
            <div class="modal-header">
                <h3>Add New Room - ${sharingType}</h3>
                <button class="close-modal">×</button>
            </div>
            <div class="modal-body">
                <form id="addRoomForm">
                    <div class="form-group">
                        <label>Sharing Capacity (C):</label>
                        <div class="readonly-field" id="capacityReadOnly" style="font-weight: bold;">${capacity} Sharing</div>
                        <input type="hidden" id="roomCapacity" value="${capacity}">
                        <input type="hidden" id="sharingTypeId" value="${sharingId}">
                    </div>
                    <div class="form-group">
                        <label>Floor Number (F):</label>
                        <input type="number" id="floorNumber" min="0" max="10" value="1" required>
                    </div>
                    <div class="form-group">
                        <label>Room Base Number (R):</label>
                        <input type="text" id="baseRoomNumber" placeholder="e.g., 01, 02, etc." required value="01">
                        <small>Must be 2 digits (e.g., '01', '02', '10', etc.)</small>
                    </div>
                    <div class="form-group">
                        <label>Final Room Number (C + F + R):</label>
                        <div class="readonly-field" id="finalRoomNumber" style="font-weight: bold; color: var(--primary-blue);">
                            ${capacity}101
                        </div>
                        <small style="color: var(--text-light-gray); font-weight: 500;">
                            Format: Capacity ${capacity} + Floor 1 + Room 01 = <strong>${capacity}101</strong>
                        </small>
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

    // Auto-generate room number based on capacity + floor + room
    const floorInput = modal.querySelector('#floorNumber');
    const baseRoomInput = modal.querySelector('#baseRoomNumber');
    const finalRoomDisplay = modal.querySelector('#finalRoomNumber');

    const updateRoomNumber = () => {
        const capacityVal = capacity; // From function parameter
        const floor = floorInput.value;
        const baseRoom = baseRoomInput.value.trim();
        
        if (floor && baseRoom) {
            // Format: Capacity + Floor + Room (e.g., 4101 for 4-Sharing, Floor 1, Room 01)
            const formattedRoomNumber = `${capacityVal}${floor}${baseRoom.padStart(2, '0')}`;
            finalRoomDisplay.textContent = formattedRoomNumber;
        }
    };

    floorInput.addEventListener('input', updateRoomNumber);
    baseRoomInput.addEventListener('input', updateRoomNumber);

    // Initialize
    updateRoomNumber();

    // Close modal events
    const closeModal = () => {
        modal.classList.remove('active');
        document.body.style.overflow = '';
        setTimeout(() => {
            if (modal.parentNode) {
                modal.parentNode.removeChild(modal);
            }
        }, 300);
    };

    modal.querySelector('.close-modal').addEventListener('click', closeModal);
    modal.querySelector('.cancel-btn').addEventListener('click', closeModal);
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeModal();
    });

    modal.querySelector('#addRoomForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const floorNumber = floorInput.value;
        const baseRoomNumber = baseRoomInput.value.trim();

        // Generate the final room number in Capacity + Floor + Room format
        const roomNumber = `${capacity}${floorNumber}${baseRoomNumber.padStart(2, '0')}`;
        const roomPrice = modal.querySelector('#roomPrice').value;
        const sharingTypeId = modal.querySelector('#sharingTypeId').value;

        console.log(`📝 Adding new room: ${roomNumber}, Floor: ${floorNumber}, Sharing: ${sharingType}, Price: ₹${roomPrice}`);
        console.log(`📝 Room Number Format: Capacity(${capacity}) + Floor(${floorNumber}) + Room(${baseRoomNumber.padStart(2, '0')}) = ${roomNumber}`);

        // Validate inputs
        if (!floorNumber || !baseRoomNumber || !roomPrice) {
            showNotification("Please fill all required fields", "error");
            return;
        }

        // Validate base room number is numeric and 2 digits
        if (!/^\d{1,2}$/.test(baseRoomNumber)) {
            showNotification("Room number must be 1 or 2 digits (e.g., 1, 01, 10, 25)", "error");
            baseRoomInput.focus();
            return;
        }

        if (parseInt(roomPrice) < 1000) {
            showNotification("Price per bed must be at least ₹1000", "error");
            return;
        }

        try {
            const submitBtn = modal.querySelector('.btn-primary');
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<div class="loading-spinner"></div> Adding...';

            const response = await fetch("/api/auth/add-room", {
                method: "POST",
                credentials: "include",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    roomNumber: roomNumber, // This will be in C+F+R format like "4101"
                    floorNumber: parseInt(floorNumber),
                    sharingTypeId: sharingTypeId,
                    price: parseInt(roomPrice),
                    capacity: parseInt(capacity)
                })
            });

            const result = await response.json();

            if (result.success) {
                showNotification(result.message, 'success');
                submitBtn.innerHTML = '<i class="fas fa-check"></i> Success!';
                submitBtn.style.background = 'linear-gradient(135deg, #28a745 0%, #20c997 100%)';

                setTimeout(async () => {
                    closeModal();
                    await refreshDashboard();
                }, 1000);
            } else {
                showNotification(result.message, 'error');
                submitBtn.disabled = false;
                submitBtn.innerHTML = 'Add Room';
            }
        } catch (error) {
            console.error("Error adding room:", error);
            showNotification("Error adding room. Please try again.", 'error');
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Add Room';
        }
    });

    // Prevent background scrolling
    document.body.style.overflow = 'hidden';

    // Focus on first input
    setTimeout(() => {
        baseRoomInput.focus();
    }, 100);
}