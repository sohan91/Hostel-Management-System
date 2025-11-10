  document.addEventListener('DOMContentLoaded', function() {
            loadRoomDetails();
            setDefaultDates();
            updateSummary();
        });

        function loadRoomDetails() {
            const urlParams = new URLSearchParams(window.location.search);
            const roomId = urlParams.get('roomId');
            const storedRoomDetails = sessionStorage.getItem('bookingRoomDetails');
            
            if (storedRoomDetails) {
                try {
                    const roomDetails = JSON.parse(storedRoomDetails);
                    displayRoomDetails(roomDetails);
                } catch (error) {
                    console.error('Error parsing room details:', error);
                    redirectToDashboard();
                }
            } else if (roomId) {
                fetchRoomDetails(roomId);
            } else {
                redirectToDashboard();
            }
        }

        function fetchRoomDetails(roomId) {
            fetch(`/api/auth/room-details/${roomId}`, {
                method: 'GET',
                credentials: 'include'
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch room details');
                }
                return response.json();
            })
            .then(roomDetails => {
                displayRoomDetails(roomDetails);
            })
            .catch(error => {
                console.error('Error fetching room details:', error);
                showNotification('Error loading room details', 'error');
                redirectToDashboard();
            });
        }

        function redirectToDashboard() {
            showNotification('No room selected for booking', 'error');
            setTimeout(() => {
                window.location.href = '/hostel/dashboard';
            }, 2000);
        }

        function displayRoomDetails(roomDetails) {
            const roomDetailsContainer = document.getElementById('roomDetails');
            const roomPrice = roomDetails.price || 5000;

            const totalAmount = roomPrice;

            if (roomDetailsContainer) {
                roomDetailsContainer.innerHTML = `
                    <div class="room-detail">
                        <label>Room Number</label>
                        <span>${roomDetails.roomDisplay || roomDetails.roomNumber || 'N/A'}</span>
                    </div>
                    <div class="room-detail">
                        <label>Sharing Type</label>
                        <span>${roomDetails.sharingTypeName || 'N/A'}</span>
                    </div>
                    <div class="room-detail">
                        <label>Floor</label>
                        <span>${roomDetails.floorNumber || 'N/A'}</span>
                    </div>
                    <div class="room-detail">
                        <label>Available Spots</label>
                        <span>${roomDetails.availableSpots || 'N/A'}</span>
                    </div>
                    <div class="room-detail">
                        <label>Price per Bed</label>
                        <span>₹${roomPrice}/month</span>
                    </div>
                    <div class="room-detail">
                        <label>Capacity</label>
                        <span>${roomDetails.sharingCapacity || 'N/A'} Sharing</span>
                    </div>
                `;
            }

            // Update summary only if elements exist
            const summaryRoomPrice = document.getElementById('summaryRoomPrice');
            const summaryTotal = document.getElementById('summaryTotal');
            
            if (summaryRoomPrice) {
                summaryRoomPrice.textContent = `₹${roomPrice}`;
            }
            if (summaryTotal) {
                summaryTotal.textContent = `₹${totalAmount}`;
            }

            // Store room details for submission
            window.currentRoomDetails = roomDetails;
        }

        function setDefaultDates() {
            const today = new Date().toISOString().split('T')[0];
            document.getElementById('joinDate').value = today;

            const maxBirthDate = new Date();
            maxBirthDate.setFullYear(maxBirthDate.getFullYear() - 16);
            document.getElementById('dateOfBirth').max = maxBirthDate.toISOString().split('T')[0];
        }

        function selectPayment(method) {
            document.querySelectorAll('.payment-option').forEach(option => {
                option.classList.remove('selected');
            });
            document.querySelector(`.payment-option input[value="${method}"]`).parentElement.classList.add('selected');
        }

        function updateSummary() {
        }

        function validateForm() {
            let isValid = true;
            const form = document.getElementById('hostelerBookingForm');
            const inputs = form.querySelectorAll('input[required], select[required]');

            document.querySelectorAll('.form-group').forEach(group => {
                group.classList.remove('error');
            });

            // Validate each required field
            inputs.forEach(input => {
                if (!input.value.trim()) {
                    input.closest('.form-group').classList.add('error');
                    isValid = false;
                }
            });

            const email = document.getElementById('studentEmail');
            if (email.value && !isValidEmail(email.value)) {
                email.closest('.form-group').classList.add('error');
                isValid = false;
            }

            const phone = document.getElementById('studentPhone');
            if (phone.value && !isValidPhone(phone.value)) {
                phone.closest('.form-group').classList.add('error');
                isValid = false;
            }

            const password = document.getElementById('studentPassword');
            if (password.value && password.value.length < 6) {
                password.closest('.form-group').classList.add('error');
                isValid = false;
            }

            const birthDate = new Date(document.getElementById('dateOfBirth').value);
            const today = new Date();
            const age = today.getFullYear() - birthDate.getFullYear();
            if (age < 16) {
                document.getElementById('dateOfBirth').closest('.form-group').classList.add('error');
                isValid = false;
            }

            return isValid;
        }

        function isValidEmail(email) {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            return emailRegex.test(email);
        }

        function isValidPhone(phone) {
            const phoneRegex = /^[0-9]{10}$/;
            return phoneRegex.test(phone.replace(/\D/g, ''));
        }

  async function submitBooking() {
    if (!window.currentRoomDetails || !window.currentRoomDetails.roomId) {
        showNotification('Room information is missing. Please select a room again.', 'error');
        redirectToDashboard();
        return;
    }

    if (!validateForm()) {
        showNotification('Please fill all required fields correctly', 'error');
        return;
    }

    const submitBtn = document.querySelector('.btn-success');
    if (!submitBtn) {
        showNotification('Submit button not found', 'error');
        return;
    }

    submitBtn.innerHTML = '<div class="loading-spinner"></div> Processing...';
    submitBtn.disabled = true;

    const formData = {
        student_id: generateStudentId(),
        admin_id: getCurrentAdminId(),
        room_id: window.currentRoomDetails.roomId,
        student_name: document.getElementById('studentName').value,
        student_email: document.getElementById('studentEmail').value,
        student_phone: document.getElementById('studentPhone').value,
        student_password: document.getElementById('studentPassword').value,
        date_of_birth: document.getElementById('dateOfBirth').value,
        parent_name: document.getElementById('parentName').value,
        parent_phone: document.getElementById('parentPhone').value,
        join_date: document.getElementById('joinDate').value,
        payment_status: document.getElementById('paymentStatus').value,
        payment_method: document.querySelector('input[name="paymentMethod"]:checked').value,
        is_active: true,
        last_login: null,
        blood_group: document.getElementById('bloodGroup').value,
        total_amount: calculateTotalAmount()
    };

    try {
        const response = await fetch('/api/auth/book-hosteler', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });
        
        const result = await response.json();
        console.log("Book-Details:", result);

        if (response.ok && result.success) {
            showNotification('Student booked successfully!', 'success');
            submitBtn.innerHTML = '<i class="fas fa-check"></i> Booking Confirmed!';
            submitBtn.style.background = 'linear-gradient(135deg, #27ae60 0%, #2ecc71 100%)';

            sessionStorage.removeItem('bookingRoomDetails');

            setTimeout(() => {
                window.location.href = '/hostel/dashboard';
            }, 2000);
        } else {
            let errorMessage = result.message || 'Error processing booking';
            
            if (errorMessage.includes('Student already exists')) {
                showNotification('Student already exists', 'error');
            } else if (errorMessage.includes('Room is already full')) {
                showNotification('This room is already full. Please select another room.', 'error');
                setTimeout(() => {
                    window.location.href = '/hostel/dashboard';
                }, 3000);
            } else if (errorMessage.includes('Admin ID mismatch')) {
                showNotification('Session error. Please log in again.', 'error');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 3000);
            } else {
                showNotification(errorMessage, 'error');
            }
            
            submitBtn.innerHTML = '<i class="fas fa-check-circle"></i> Confirm Booking';
            submitBtn.disabled = false;
        }
    } catch (error) {
        console.error('Error submitting booking:', error);
        showNotification('Network error. Please check your connection and try again.', 'error');
        submitBtn.innerHTML = '<i class="fas fa-check-circle"></i> Confirm Booking';
        submitBtn.disabled = false;
    }
}
function validateForm() {
    let isValid = true;
    const form = document.getElementById('hostelerBookingForm');
    const inputs = form.querySelectorAll('input[required], select[required]');

    document.querySelectorAll('.form-group').forEach(group => {
        group.classList.remove('error');
        group.classList.remove('blinking');
    });

    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.closest('.form-group').classList.add('error', 'blinking');
            isValid = false;
        }
    });

    const email = document.getElementById('studentEmail');
    if (email.value && !isValidEmail(email.value)) {
        email.closest('.form-group').classList.add('error', 'blinking');
        isValid = false;
    }

    const phone = document.getElementById('studentPhone');
    if (phone.value && !isValidPhone(phone.value)) {
        phone.closest('.form-group').classList.add('error', 'blinking');
        isValid = false;
    }

    const password = document.getElementById('studentPassword');
    if (password.value && password.value.length < 8) {
        password.closest('.form-group').classList.add('error', 'blinking');
        isValid = false;
    }

    const birthDate = new Date(document.getElementById('dateOfBirth').value);
    const today = new Date();
    const age = today.getFullYear() - birthDate.getFullYear();
    if (age < 16) {
        document.getElementById('dateOfBirth').closest('.form-group').classList.add('error', 'blinking');
        isValid = false;
    }

    if (!isValid) {
        setTimeout(() => {
            document.querySelectorAll('.form-group.blinking').forEach(group => {
                group.classList.remove('blinking');
            });
        }, 2000);
    }

    return isValid;
}

function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function isValidPhone(phone) {
    const phoneRegex = /^[0-9]{10}$/;
    return phoneRegex.test(phone.replace(/\D/g, ''));
}function setupFieldListeners() {
    const emailField = document.getElementById('studentEmail');
    const phoneField = document.getElementById('studentPhone');
    const passwordField = document.getElementById('studentPassword');
    
    if (emailField) {
        emailField.addEventListener('input', function() {
            this.style.borderColor = '';
            this.style.backgroundColor = '';
            this.closest('.form-group').classList.remove('error', 'blinking');
        });
    }
    
    if (phoneField) {
        phoneField.addEventListener('input', function() {
            this.style.borderColor = '';
            this.style.backgroundColor = '';
            this.closest('.form-group').classList.remove('error', 'blinking');
        });
    }
    
    if (passwordField) {
        passwordField.addEventListener('input', function() {
            this.style.borderColor = '';
            this.style.backgroundColor = '';
            this.closest('.form-group').classList.remove('error', 'blinking');
            
            const password = this.value;
            const strengthIndicator = document.getElementById('passwordStrength') || createPasswordStrengthIndicator();
            
            if (password.length === 0) {
                strengthIndicator.textContent = '';
                strengthIndicator.className = 'password-strength';
            } else if (password.length < 8) {
                strengthIndicator.textContent = 'Weak - Minimum 8 characters required';
                strengthIndicator.className = 'password-strength weak';
            } else if (password.length >= 8) {
                strengthIndicator.textContent = 'Strong';
                strengthIndicator.className = 'password-strength strong';
            }
        });
    }
    
    const allInputs = document.querySelectorAll('input, select');
    allInputs.forEach(input => {
        input.addEventListener('input', function() {
            this.closest('.form-group').classList.remove('error', 'blinking');
        });
        input.addEventListener('change', function() {
            this.closest('.form-group').classList.remove('error', 'blinking');
        });
    });
}
function createPasswordStrengthIndicator() {
    const passwordGroup = document.getElementById('studentPassword').closest('.form-group');
    const strengthIndicator = document.createElement('div');
    strengthIndicator.id = 'passwordStrength';
    strengthIndicator.className = 'password-strength';
    passwordGroup.appendChild(strengthIndicator);
    return strengthIndicator;
}

function highlightDuplicateFields() {
    const emailField = document.getElementById('studentEmail');
    const phoneField = document.getElementById('studentPhone');
    
    if (emailField) {
        emailField.style.borderColor = '#e74c3c';
        emailField.style.backgroundColor = '#fff5f5';
        emailField.closest('.form-group').classList.add('blinking');
    }
    
    if (phoneField) {
        phoneField.style.borderColor = '#e74c3c';
        phoneField.style.backgroundColor = '#fff5f5';
        phoneField.closest('.form-group').classList.add('blinking');
    }
    
    setTimeout(() => {
        document.querySelectorAll('.form-group.blinking').forEach(group => {
            group.classList.remove('blinking');
        });
    }, 2000);
    
    setTimeout(() => {
        showNotification('Please use different email or phone number', 'info');
    }, 1000);
}

document.addEventListener('DOMContentLoaded', function() {
    loadRoomDetails();
    setDefaultDates();
    updateSummary();
    setupFieldListeners();
});
        function calculateTotalAmount() {
            const roomPrice = window.currentRoomDetails?.price || 5000;
            return roomPrice;
        }

        function generateStudentId() {
            const timestamp = new Date().getTime().toString().slice(-6);
            const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0');
            return `STU${timestamp}${random}`;
        }

        function getCurrentAdminId() {
            return sessionStorage.getItem('adminId') || '1';
        }

        function clearForm() {
            document.getElementById('hostelerBookingForm').reset();
            setDefaultDates();
            document.querySelectorAll('.form-group').forEach(group => {
                group.classList.remove('error');
            });
            showNotification('Form cleared', 'info');
        }

 function showNotification(message, type = 'info') {
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notification => notification.remove());

    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    
    let backgroundColor, icon;
    switch(type) {
        case 'success':
            backgroundColor = '#27ae60';
            icon = '<i class="fas fa-check-circle"></i>';
            break;
        case 'error':
            backgroundColor = '#e74c3c';
            icon = '<i class="fas fa-exclamation-circle"></i>';
            break;
        case 'warning':
            backgroundColor = '#f39c12';
            icon = '<i class="fas fa-exclamation-triangle"></i>';
            break;
        case 'info':
            backgroundColor = '#3498db';
            icon = '<i class="fas fa-info-circle"></i>';
            break;
        default:
            backgroundColor = '#3498db';
            icon = '<i class="fas fa-info-circle"></i>';
    }

    notification.style.cssText = `
        position: fixed;
        top: 100px;
        right: 20px;
        padding: 15px 20px;
        background: ${backgroundColor};
        color: white;
        border-radius: 8px;
        box-shadow: 0 4px 15px rgba(0,0,0,0.2);
        z-index: 10000;
        font-weight: 500;
        font-size: 14px;
        max-width: 400px;
        word-wrap: break-word;
        display: flex;
        align-items: center;
        gap: 10px;
        border-left: 4px solid ${type === 'success' ? '#2ecc71' : type === 'error' ? '#c0392b' : type === 'warning' ? '#e67e22' : '#2980b9'};
    `;
    
    notification.innerHTML = `
        <span style="font-size: 18px;">${icon}</span>
        <span>${message}</span>
    `;
    
    document.body.appendChild(notification);
    
    setTimeout(() => {
        if (notification.parentNode) {
            notification.style.opacity = '0';
            notification.style.transform = 'translateX(100%)';
            setTimeout(() => notification.remove(), 300);
        }
    }, 5000);
}
        function highlightDuplicateFields() {
    // Highlight fields that might have duplicates
    const emailField = document.getElementById('studentEmail');
    const phoneField = document.getElementById('studentPhone');
    
    if (emailField) {
        emailField.style.borderColor = '#e74c3c';
        emailField.style.backgroundColor = '#fff5f5';
    }
    
    if (phoneField) {
        phoneField.style.borderColor = '#e74c3c';
        phoneField.style.backgroundColor = '#fff5f5';
    }
    
    // Show suggestion to user
    setTimeout(() => {
        showNotification('Please use different email or phone number', 'info');
    }, 1000);
}
function setupFieldListeners() {
    const emailField = document.getElementById('studentEmail');
    const phoneField = document.getElementById('studentPhone');
    
    if (emailField) {
        emailField.addEventListener('input', function() {
            this.style.borderColor = '';
            this.style.backgroundColor = '';
        });
    }
    
    if (phoneField) {
        phoneField.addEventListener('input', function() {
            this.style.borderColor = '';
            this.style.backgroundColor = '';
        });
    }
}

// Call this when the page loads
document.addEventListener('DOMContentLoaded', function() {
    loadRoomDetails();
    setDefaultDates();
    updateSummary();
    setupFieldListeners(); // Add this line
});
        selectPayment('cash');