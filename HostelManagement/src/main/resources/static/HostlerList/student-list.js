 const totalCapacity = 4;
        const roomNumber = "Room 101";

        // IMPORTANT: The studentsData array is now using the new field names.
        const studentsData = [
            { 
                student_id: "STU2024001", admin_id: "ADM001", room_id: "R101", student_name: "Rajesh Kumar", 
                student_email: "rajesh.kumar@college.edu", student_phone: "+91 98765 43210", 
                student_password: "encrypted_hash_1", date_of_birth: "2000-07-25", 
                parent_name: "Gopal Kumar", parent_phone: "+91 98765 43211", 
                join_date: "2024-01-15", payment_status: "Paid", 
                is_active: true, last_login: "2025-11-05 11:45 AM"
            },
            { 
                student_id: "STU2024002", admin_id: "ADM001", room_id: "R101", student_name: "Priya Sharma", 
                student_email: "priya.sharma@college.edu", student_phone: "+91 98765 43213", 
                student_password: "encrypted_hash_2", date_of_birth: "2001-03-10", 
                parent_name: "Sunita Sharma", parent_phone: "+91 98765 43214", 
                join_date: "2024-02-01", payment_status: "Paid", 
                is_active: true, last_login: "2025-11-06 09:15 AM"
            },
            { 
                student_id: "STU2024003", admin_id: "ADM002", room_id: "R101", student_name: "Amit Patel", 
                student_email: "amit.patel@college.edu", student_phone: "+91 98765 43216", 
                student_password: "encrypted_hash_3", date_of_birth: "1999-12-01", 
                parent_name: "Manoj Patel", parent_phone: "+91 98765 43217", 
                join_date: "2023-08-20", payment_status: "Pending", 
                is_active: false, last_login: "2025-10-29 04:00 PM"
            },
            // To test the empty state, comment out all student objects above, e.g., studentsData = [];
        ];

        function updateOccupancy(occupied, capacity) {
            const available = capacity - occupied;

            document.querySelector('.sharing-type').textContent = `${capacity / 2}-Sharing (${capacity} Beds)`;
            document.querySelector('.occupied-count').textContent = occupied;
            document.querySelector('.available-count').textContent = available;

            const statusSpan = document.querySelector('.room-status');
            statusSpan.classList.remove('available', 'full');
            
            if (available > 0) {
                statusSpan.classList.add('available');
                statusSpan.innerHTML = '<i class="fas fa-circle"></i> Available';
            } else {
                statusSpan.classList.add('full');
                statusSpan.innerHTML = '<i class="fas fa-circle"></i> Full';
            }
        }

        function createStudentCard(student) {
            // Determine class and icon based on payment status
            let feeClass = '';
            let feeIcon = '';
            if (student.payment_status === "Paid") {
                feeClass = 'status-present';
                feeIcon = 'fas fa-check-circle';
            } else if (student.payment_status === "Pending") {
                feeClass = 'status-pending';
                feeIcon = 'fas fa-clock';
            } else {
                 feeClass = 'status-absent';
                 feeIcon = 'fas fa-times-circle';
            }
            
            // Determine class and icon for active status
            const activeStatusText = student.is_active ? 'Active' : 'Inactive';
            const activeStatusClass = student.is_active ? 'status-active' : 'status-inactive';
            const activeStatusIcon = student.is_active ? 'fas fa-user-check' : 'fas fa-user-times';
            
            // NOTE: Fields are mapped to the new schema names
            return `
                <div class="student-card">
                    <div class="student-card-header" onclick="toggleStudentDetails(this)">
                        <div class="profile-icon">
                            <i class="fas fa-user"></i>
                        </div>
                        <div class="student-basic-info">
                            <div class="student-name">${student.student_name}</div>
                            <div class="student-meta">
                                <span class="student-id">ID: ${student.student_id}</span>
                            </div>
                        </div>
                        <div class="dropdown-arrow">
                            <i class="fas fa-chevron-down"></i>
                        </div>
                    </div>
                    <div class="student-details">
                        <div class="details-grid">
                            
                            <div class="detail-item">
                                <span class="detail-label">Student ID</span>
                                <span class="detail-value">${student.student_id}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Admin ID</span>
                                <span class="detail-value">${student.admin_id}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Room ID</span>
                                <span class="detail-value">${student.room_id}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Date of Birth</span>
                                <span class="detail-value">${student.date_of_birth}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Join Date</span>
                                <span class="detail-value">${student.join_date}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Payment Status</span>
                                <span class="detail-value ${feeClass}">
                                    <i class="${feeIcon}"></i> ${student.payment_status}
                                </span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Active Status</span>
                                <span class="detail-value ${activeStatusClass}">
                                    <i class="${activeStatusIcon}"></i> ${activeStatusText}
                                </span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Last Login</span>
                                <span class="detail-value">${student.last_login}</span>
                            </div>
                            
                            <div class="detail-item full-width">
                                <span class="detail-label">Email</span>
                                <a href="mailto:${student.student_email}" class="detail-value contact-info">
                                    <i class="fas fa-envelope"></i> ${student.student_email}
                                </a>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Student Phone</span>
                                <a href="tel:${student.student_phone.replace(/[^0-9+]/g, '')}" class="detail-value contact-info">
                                    <i class="fas fa-mobile-alt"></i> ${student.student_phone}
                                </a>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Parent Name</span>
                                <span class="detail-value contact-info">
                                    <i class="fas fa-users"></i> ${student.parent_name}
                                </span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Parent Phone</span>
                                <a href="tel:${student.parent_phone.replace(/[^0-9+]/g, '')}" class="detail-value contact-info">
                                    <i class="fas fa-phone-alt"></i> ${student.parent_phone}
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }

        function createEmptyBedCard() {
            return `
                <div class="student-card empty-bed">
                    <div class="student-card-header">
                        <div class="profile-icon">
                            <i class="fas fa-bed"></i>
                        </div>
                        <div class="student-basic-info">
                            <div class="student-name">Available Bed</div>
                            <div class="student-meta">
                                <span class="student-id">Status: Vacant</span>
                            </div>
                        </div>
                        <div class="dropdown-arrow">
                            <i class="fas fa-chevron-down"></i>
                        </div>
                    </div>
                </div>
            `;
        }

        function renderStudents() {
            const listContainer = document.getElementById('studentsList');
            const occupiedCount = studentsData.length;
            const availableBeds = totalCapacity - occupiedCount;
            let listHTML = '';

            // 1. Update Header Occupancy
            updateOccupancy(occupiedCount, totalCapacity);

            if (occupiedCount === 0) {
                // 2. Render Empty State
                listHTML = `
                    <div class="empty-state-card">
                        <i class="fas fa-users-slash empty-icon"></i>
                        <h2 class="empty-title">No Students Assigned to ${roomNumber}</h2>
                        <p class="empty-message">This room is completely vacant. Click the button below or the one in the footer bar to assign the first student.</p>
                        <button class="action-btn floating-action-btn empty-action-btn" onclick="addStudent()">
                            <i class="fas fa-user-plus"></i> Add First Student
                        </button>
                    </div>
                `;
            } else {
                // 3. Render Student Cards
                studentsData.forEach(student => {
                    listHTML += createStudentCard(student);
                });

                // 4. Render Available Bed Cards
                for (let i = 0; i < availableBeds; i++) {
                    listHTML += createEmptyBedCard();
                }
            }
            
            listContainer.innerHTML = listHTML;
        }


        function toggleStudentDetails(header) {
            const card = header.parentElement;
            
            if (card.classList.contains('empty-bed')) {
                return;
            }

            const details = card.querySelector('.student-details');
            const arrow = header.querySelector('.dropdown-arrow');
            const isExpanded = details.classList.contains('expanded');
            
            // Collapse all other expanded cards
            document.querySelectorAll('.student-details.expanded').forEach(expandedDetails => {
                if (expandedDetails !== details) {
                    expandedDetails.classList.remove('expanded');
                    expandedDetails.parentElement.querySelector('.dropdown-arrow').classList.remove('rotated');
                }
            });
            
            // Toggle current card
            if (!isExpanded) {
                details.classList.add('expanded');
                arrow.classList.add('rotated');
            } else {
                details.classList.remove('expanded');
                arrow.classList.remove('rotated');
            }
        }

        function goBack() {
            const message = "Simulating navigation: You would now return to the list of all rooms.";
            showNotification(message);
        }

        function addStudent() {
            const message = `Simulating action: A form or modal would pop up here to add a new student to ${roomNumber}.`;
            showNotification(message);
        }

        function showNotification(message) {
            let notification = document.getElementById('custom-notification');
            if (!notification) {
                notification = document.createElement('div');
                notification.id = 'custom-notification';
                document.body.appendChild(notification);
                
                notification.style.cssText = `
                    position: fixed;
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);
                    background-color: var(--primary-color);
                    color: white;
                    padding: 20px 30px;
                    border-radius: 10px;
                    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
                    z-index: 200;
                    opacity: 0;
                    transition: opacity 0.3s ease-in-out;
                    font-weight: 500;
                    text-align: center;
                `;
            }

            notification.textContent = message;
            notification.style.opacity = '1';
            
            setTimeout(() => {
                notification.style.opacity = '0';
            }, 3000);
        }