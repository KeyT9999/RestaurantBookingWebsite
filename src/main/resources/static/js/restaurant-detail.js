/**
 * Restaurant Detail Page JavaScript Modules
 * Modular, maintainable, and performant code
 */

// Constants
const CONSTANTS = {
    TIME_BUFFER_MINUTES: 30,
    TIME_SLOTS: [
        { value: '17:00', text: '5:00 PM' },
        { value: '17:30', text: '5:30 PM' },
        { value: '18:00', text: '6:00 PM' },
        { value: '18:30', text: '6:30 PM' },
        { value: '19:00', text: '7:00 PM' },
        { value: '19:30', text: '7:30 PM' },
        { value: '20:00', text: '8:00 PM' },
        { value: '20:30', text: '8:30 PM' },
        { value: '21:00', text: '9:00 PM' }
    ],
    SELECTORS: {
        TAB_BUTTONS: '[data-bs-toggle="tab"]',
        TAB_PANES: '.tab-pane',
        FILTER_BUTTONS: '.restaurant-detail-page__filter-btn',
        MENU_ITEMS: '.restaurant-detail-page__menu-item',
        TABLE_ITEMS: '.restaurant-detail-page__table-item',
        FORM: 'form[th\\:action="@{/booking/new}"]',
        DATE_INPUT: 'input[name="date"]',
        TIME_SELECT: 'select[name="time"]',
        GUESTS_SELECT: 'select[name="guests"]',
        RATING_ELEMENTS: '.restaurant-detail-page__rating-stars'
    }
};

// Utility Functions
const Utils = {
    /**
     * Debounce function to limit function calls
     */
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    /**
     * Toggle active class on elements
     */
    toggleActive(elements, activeElement) {
        elements.forEach(el => el.classList.remove('active'));
        if (activeElement) {
            activeElement.classList.add('active');
        }
    },

    /**
     * Safe query selector with error handling
     */
    safeQuerySelector(selector, context = document) {
        try {
            const element = context.querySelector(selector);
            if (!element) {
                console.warn(`Element not found: ${selector}`);
            }
            return element;
        } catch (error) {
            console.error(`Query selector error for ${selector}:`, error);
            return null;
        }
    },

    /**
     * Safe query selector all with error handling
     */
    safeQuerySelectorAll(selector, context = document) {
        try {
            const elements = context.querySelectorAll(selector);
            if (elements.length === 0) {
                console.warn(`No elements found: ${selector}`);
            }
            return elements;
        } catch (error) {
            console.error(`Query selector all error for ${selector}:`, error);
            return [];
        }
    },

    /**
     * Format date for input
     */
    formatDateForInput(date) {
        return date.toISOString().split('T')[0];
    },

    /**
     * Check if date is today
     */
    isToday(date) {
        const today = new Date();
        return date.toDateString() === today.toDateString();
    },

    /**
     * Get current time in minutes
     */
    getCurrentTimeInMinutes() {
        const now = new Date();
        return now.getHours() * 60 + now.getMinutes();
    }
};

// Tab Manager Class
class TabManager {
    constructor() {
        this.tabButtons = Utils.safeQuerySelectorAll(CONSTANTS.SELECTORS.TAB_BUTTONS);
        this.tabPanes = Utils.safeQuerySelectorAll(CONSTANTS.SELECTORS.TAB_PANES);
        
        if (this.tabButtons.length === 0) {
            console.warn('No tab buttons found');
            return;
        }
        
        this.init();
    }

    init() {
        this.tabButtons.forEach(button => {
            button.addEventListener('click', this.handleTabClick.bind(this));
        });
    }

    handleTabClick(e) {
        try {
            e.preventDefault();
            
            // Remove active class from all tabs and panes
            Utils.toggleActive(this.tabButtons);
            this.tabPanes.forEach(pane => {
                pane.classList.remove('show', 'active');
            });
            
            // Add active class to clicked tab
            e.currentTarget.classList.add('active');
            
            // Show corresponding tab pane
            const targetPane = Utils.safeQuerySelector(e.currentTarget.getAttribute('data-bs-target'));
            if (targetPane) {
                targetPane.classList.add('show', 'active');
            }
        } catch (error) {
            console.error('Tab click error:', error);
        }
    }
}

// Menu Filter Manager Class
class MenuFilterManager {
    constructor() {
        this.filterButtons = Utils.safeQuerySelectorAll(CONSTANTS.SELECTORS.FILTER_BUTTONS);
        this.menuItems = Utils.safeQuerySelectorAll(CONSTANTS.SELECTORS.MENU_ITEMS);
        
        if (this.filterButtons.length === 0) {
            console.warn('No filter buttons found');
            return;
        }
        
        this.init();
    }

    init() {
        this.filterButtons.forEach(button => {
            button.addEventListener('click', this.handleFilterClick.bind(this));
        });
    }

    handleFilterClick(e) {
        try {
            const clickedButton = e.currentTarget;
            const filter = clickedButton.getAttribute('data-filter');
            
            // Update button states
            Utils.toggleActive(this.filterButtons, clickedButton);
            
            // Filter menu items
            this.filterMenuItems(filter);
        } catch (error) {
            console.error('Filter click error:', error);
        }
    }

    filterMenuItems(filter) {
        this.menuItems.forEach(item => {
            if (filter === 'all') {
                item.style.display = 'flex';
            } else {
                const shouldShow = this.shouldShowMenuItem(item, filter);
                item.style.display = shouldShow ? 'flex' : 'none';
            }
        });
    }

    shouldShowMenuItem(item, filter) {
        const itemName = item.querySelector('.restaurant-detail-page__menu-item-name')?.textContent.toLowerCase() || '';
        const itemDescription = item.querySelector('.restaurant-detail-page__menu-item-description')?.textContent.toLowerCase() || '';
        
        const filterKeywords = {
            'appetizers': ['appetizer', 'starter', 'khai vị'],
            'main': ['main', 'entree', 'món chính', 'main course'],
            'desserts': ['dessert', 'sweet', 'tráng miệng', 'món ngọt']
        };
        
        const keywords = filterKeywords[filter] || [];
        return keywords.some(keyword => 
            itemName.includes(keyword) || itemDescription.includes(keyword)
        );
    }
}

// Table Layout Manager Class
class TableLayoutManager {
    constructor() {
        this.tableItems = Utils.safeQuerySelectorAll(CONSTANTS.SELECTORS.TABLE_ITEMS);
        
        if (this.tableItems.length === 0) {
            console.warn('No table items found');
            return;
        }
        
        this.init();
    }

    init() {
        this.tableItems.forEach(table => {
            table.addEventListener('click', this.handleTableClick.bind(this));
        });
    }

    handleTableClick(e) {
        try {
            const tableId = e.currentTarget.getAttribute('data-table');
            const isAvailable = e.currentTarget.classList.contains('restaurant-detail-page__table-item--available');
            
            if (isAvailable) {
                this.showTableDetails(tableId);
            } else {
                this.showTableUnavailable(tableId);
            }
        } catch (error) {
            console.error('Table click error:', error);
        }
    }

    showTableDetails(tableId) {
        // Create modal or show details
        console.log(`Showing details for table ${tableId}`);
        // Implementation for table details modal
    }

    showTableUnavailable(tableId) {
        // Show user-friendly message
        const message = `Bàn ${tableId} đã được đặt trước. Vui lòng chọn bàn khác.`;
        
        // Use a more user-friendly notification instead of alert
        this.showNotification(message, 'warning');
    }

    showNotification(message, type = 'info') {
        // Create a custom notification instead of using alert
        const notification = document.createElement('div');
        notification.className = `restaurant-detail-page__notification restaurant-detail-page__notification--${type}`;
        notification.textContent = message;
        
        // Style the notification
        Object.assign(notification.style, {
            position: 'fixed',
            top: '20px',
            right: '20px',
            padding: '1rem',
            borderRadius: '8px',
            color: 'white',
            backgroundColor: type === 'warning' ? '#F59E0B' : '#4A90E2',
            zIndex: '9999',
            maxWidth: '300px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
        });
        
        document.body.appendChild(notification);
        
        // Auto remove after 3 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 3000);
    }
}

// Reservation Form Manager Class
class ReservationFormManager {
    constructor() {
        this.form = Utils.safeQuerySelector(CONSTANTS.SELECTORS.FORM);
        this.dateInput = Utils.safeQuerySelector(CONSTANTS.SELECTORS.DATE_INPUT);
        this.timeSelect = Utils.safeQuerySelector(CONSTANTS.SELECTORS.TIME_SELECT);
        this.guestsSelect = Utils.safeQuerySelector(CONSTANTS.SELECTORS.GUESTS_SELECT);
        
        if (!this.form) {
            console.warn('Reservation form not found');
            return;
        }
        
        this.init();
    }

    init() {
        this.setDefaultDate();
        this.setupFormValidation();
        this.setupTimeOptions();
    }

    setDefaultDate() {
        if (this.dateInput) {
            const tomorrow = new Date();
            tomorrow.setDate(tomorrow.getDate() + 1);
            this.dateInput.value = Utils.formatDateForInput(tomorrow);
        }
    }

    setupFormValidation() {
        this.form.addEventListener('submit', this.handleFormSubmit.bind(this));
    }

    handleFormSubmit(e) {
        try {
            const date = this.dateInput?.value;
            const time = this.timeSelect?.value;
            const guests = this.guestsSelect?.value;
            
            if (!date || !time || !guests) {
                e.preventDefault();
                this.showFormError('Vui lòng điền đầy đủ thông tin đặt bàn.');
                return false;
            }
            
            if (!this.validateDate(date)) {
                e.preventDefault();
                this.showFormError('Không thể đặt bàn trong quá khứ. Vui lòng chọn ngày khác.');
                return false;
            }
            
            if (!this.validateTime(date, time)) {
                e.preventDefault();
                this.showFormError('Giờ đặt bàn không hợp lệ. Vui lòng chọn giờ khác.');
                return false;
            }
            
        } catch (error) {
            console.error('Form validation error:', error);
            e.preventDefault();
            this.showFormError('Có lỗi xảy ra. Vui lòng thử lại.');
        }
    }

    validateDate(date) {
        const selectedDate = new Date(date);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return selectedDate >= today;
    }

    validateTime(date, time) {
        const selectedDate = new Date(date);
        if (!Utils.isToday(selectedDate)) {
            return true; // Future dates are always valid
        }
        
        const [hours, minutes] = time.split(':').map(Number);
        const selectedTime = hours * 60 + minutes;
        const currentTime = Utils.getCurrentTimeInMinutes();
        
        return selectedTime > currentTime + CONSTANTS.TIME_BUFFER_MINUTES;
    }

    showFormError(message) {
        // Create error notification
        const errorDiv = document.createElement('div');
        errorDiv.className = 'restaurant-detail-page__form-error';
        errorDiv.textContent = message;
        
        Object.assign(errorDiv.style, {
            color: '#EF4444',
            fontSize: '0.9rem',
            marginTop: '0.5rem',
            padding: '0.5rem',
            backgroundColor: '#FEF2F2',
            border: '1px solid #FECACA',
            borderRadius: '4px'
        });
        
        // Remove existing errors
        const existingErrors = this.form.querySelectorAll('.restaurant-detail-page__form-error');
        existingErrors.forEach(error => error.remove());
        
        // Add new error
        this.form.appendChild(errorDiv);
        
        // Auto remove after 5 seconds
        setTimeout(() => {
            if (errorDiv.parentNode) {
                errorDiv.parentNode.removeChild(errorDiv);
            }
        }, 5000);
    }

    setupTimeOptions() {
        if (this.dateInput && this.timeSelect) {
            this.dateInput.addEventListener('change', Utils.debounce(this.updateTimeOptions.bind(this), 300));
            this.updateTimeOptions(this.dateInput.value);
        }
    }

    updateTimeOptions(selectedDate) {
        if (!this.timeSelect) return;
        
        const selectedDateObj = new Date(selectedDate);
        const isToday = Utils.isToday(selectedDateObj);
        
        // Clear existing options
        this.timeSelect.innerHTML = '<option value="">Choose time</option>';
        
        CONSTANTS.TIME_SLOTS.forEach(slot => {
            if (isToday) {
                const [hours, minutes] = slot.value.split(':').map(Number);
                const slotTime = hours * 60 + minutes;
                const currentTime = Utils.getCurrentTimeInMinutes();
                
                if (slotTime > currentTime + CONSTANTS.TIME_BUFFER_MINUTES) {
                    this.addTimeOption(slot);
                }
            } else {
                this.addTimeOption(slot);
            }
        });
    }

    addTimeOption(slot) {
        const option = document.createElement('option');
        option.value = slot.value;
        option.textContent = slot.text;
        this.timeSelect.appendChild(option);
    }
}

// Rating Display Manager Class
class RatingDisplayManager {
    constructor() {
        this.ratingElements = Utils.safeQuerySelectorAll(CONSTANTS.SELECTORS.RATING_ELEMENTS);
        
        if (this.ratingElements.length === 0) {
            console.warn('No rating elements found');
            return;
        }
        
        this.init();
    }

    init() {
        this.ratingElements.forEach(element => {
            const rating = parseInt(element.getAttribute('data-rating')) || 0;
            element.innerHTML = this.generateStarHTML(rating);
        });
    }

    generateStarHTML(rating) {
        const stars = [];
        for (let i = 1; i <= 5; i++) {
            const isFilled = i <= rating;
            const starClass = isFilled ? 'fas fa-star text-warning' : 'far fa-star text-muted';
            stars.push(`<i class="${starClass}"></i>`);
        }
        return stars.join('');
    }
}

// Main Restaurant Detail Manager
class RestaurantDetailManager {
    constructor() {
        this.managers = {};
        this.init();
    }

    init() {
        try {
            // Initialize all managers
            this.managers.tabManager = new TabManager();
            this.managers.menuFilterManager = new MenuFilterManager();
            this.managers.tableLayoutManager = new TableLayoutManager();
            this.managers.reservationFormManager = new ReservationFormManager();
            this.managers.ratingDisplayManager = new RatingDisplayManager();
            
            console.log('Restaurant Detail Manager initialized successfully');
        } catch (error) {
            console.error('Failed to initialize Restaurant Detail Manager:', error);
        }
    }

    // Public API for external access
    getManager(name) {
        return this.managers[name];
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Add loading class to body
    document.body.classList.add('restaurant-detail-page');
    
    // Initialize the main manager
    window.restaurantDetailManager = new RestaurantDetailManager();
});

// Export for module systems (if needed)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        RestaurantDetailManager,
        TabManager,
        MenuFilterManager,
        TableLayoutManager,
        ReservationFormManager,
        RatingDisplayManager,
        Utils,
        CONSTANTS
    };
}
