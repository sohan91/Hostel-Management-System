document.addEventListener('DOMContentLoaded', function() {
    loadAdminProfile();
    addEventListeners();
});

async function loadAdminProfile() {
    try {
        showLoadingState();
        
        const response = await fetch('http://localhost:8080/api/auth/admin-profile', {
            method: 'GET',
            credentials: 'include'
        });

        if (!response.ok) {
            if (response.status === 401 || response.status === 404) {
                window.location.href = '/hostel/login';
                return;
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const adminData = await response.json();
        populateProfileData(adminData);
        
    } catch (error) {
        console.error('Error loading admin profile:', error);
        showErrorState();
    }
}

function populateProfileData(adminData) {
    try {
        const fieldMapping = {
            'admin_id': adminData.adminId || 'N/A',
            'first_name': adminData.firstName || 'Not specified',
            'last_name': adminData.lastName || 'Not specified',
            'email': adminData.email || 'Not specified',
            'phone_number': adminData.phoneNumber || 'Not specified',
            'created_at': formatDate(adminData.createdAt) || 'Not available',
            'hostel_name': adminData.hostelName || 'Not specified',
            'hostel_address': adminData.hostelAddress || 'Not specified'
        };

        Object.keys(fieldMapping).forEach(fieldName => {
            const elements = document.querySelectorAll(`[data-field="${fieldName}"]`);
            elements.forEach(element => {
                element.textContent = fieldMapping[fieldName];
                element.setAttribute('data-value', fieldMapping[fieldName]);
            });
        });

        updateProfileHeader(adminData);
        removeLoadingState();
        
    } catch (error) {
        console.error('Error populating profile data:', error);
        showErrorState();
    }
}

function updateProfileHeader(adminData) {
    const adminNameElement = document.querySelector('.admin-name-display');
    const hostelNameElement = document.querySelector('.hostel-name-display');
    
    if (adminData.firstName && adminData.lastName) {
        adminNameElement.textContent = `${adminData.firstName} ${adminData.lastName}`;
    } else if (adminData.firstName) {
        adminNameElement.textContent = adminData.firstName;
    } else {
        adminNameElement.textContent = 'Administrator';
    }
    
    if (adminData.hostelName) {
        hostelNameElement.textContent = adminData.hostelName;
    }
}

function formatDate(dateString) {
    if (!dateString) return 'Not available';
    
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    } catch (error) {
        return 'Invalid date';
    }
}

function showLoadingState() {
    const dataFields = document.querySelectorAll('[data-field]');
    dataFields.forEach(field => {
        field.innerHTML = '<span class="loading-dots">Loading</span>';
        field.classList.add('loading');
    });
    
    document.querySelector('.admin-name-display').textContent = 'Loading...';
    document.querySelector('.hostel-name-display').textContent = 'Loading...';
}

function removeLoadingState() {
    const dataFields = document.querySelectorAll('[data-field]');
    dataFields.forEach(field => {
        field.classList.remove('loading');
    });
}

function showErrorState() {
    const dataFields = document.querySelectorAll('[data-field]');
    dataFields.forEach(field => {
        field.textContent = 'Error loading data';
        field.classList.add('error');
    });
    
    document.querySelector('.admin-name-display').textContent = 'Error Loading Profile';
    document.querySelector('.hostel-name-display').textContent = 'Please try again later';
}

function addEventListeners() {
    const editProfileBtn = document.querySelector('.edit-profile-btn');
    if (editProfileBtn) {
        editProfileBtn.addEventListener('click', function() {
            alert('Edit profile functionality would open here');
        });
    }
    
    const goToDashboardBtn = document.querySelector('.go-to-dashboard-btn');
    if (goToDashboardBtn) {
        goToDashboardBtn.addEventListener('click', function() {
            window.location.href = '/hostel/dashboard';
        });
    }
    
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            window.location.href = '/hostel/dashboard';
        }
    });
}