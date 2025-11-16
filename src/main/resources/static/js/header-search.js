/**
 * Header Search Dropdown Functionality
 * Handles the search dropdown toggle and interactions
 */

// Make functions globally available
window.toggleSearchDropdown = function() {
    console.log('🔍 toggleSearchDropdown CALLED'); // Debug log
    const dropdown = document.getElementById('searchDropdown');
    if (!dropdown) {
        console.error('❌ searchDropdown not found!');
        return;
    }
    
    const isOpen = dropdown.classList.contains('show');
    console.log('📊 Current state:', isOpen ? 'OPEN' : 'CLOSED'); // Debug log
    
    // Close all other dropdowns first
    document.querySelectorAll('.search-dropdown').forEach(dd => {
        dd.classList.remove('show');
    });
    
    // Toggle current dropdown
    if (!isOpen) {
        dropdown.classList.add('show');
        console.log('✅ Dropdown opened, class "show" added'); // Debug log
        // Focus on search input when opened
        setTimeout(() => {
            const searchInput = dropdown.querySelector('.search-input');
            if (searchInput) {
                searchInput.focus();
            }
        }, 100);
    } else {
        console.log('✅ Dropdown closed, class "show" removed'); // Debug log
    }
};

window.clearSearch = function() {
    const form = document.querySelector('.search-form');
    if (form) {
        form.reset();
    }
};

// Initialize when DOM is ready
(function() {
    // Use DOMContentLoaded or immediate execution if DOM already loaded
    function init() {
        // Close dropdown when clicking outside
        document.addEventListener('click', function(event) {
            const searchContainer = document.querySelector('.header-search');
            const dropdown = document.getElementById('searchDropdown');
            
            if (searchContainer && dropdown && !searchContainer.contains(event.target)) {
                dropdown.classList.remove('show');
            }
        });
        
        // Close dropdown on escape key
        document.addEventListener('keydown', function(event) {
            if (event.key === 'Escape') {
                const dropdown = document.getElementById('searchDropdown');
                if (dropdown) {
                    dropdown.classList.remove('show');
                }
            }
        });
    }
    
    // Check if DOM is already loaded
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        // DOM already loaded, execute immediately
        init();
    }
})();

