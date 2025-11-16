/**
 * Header Search Dropdown Functionality
 * Handles the search dropdown toggle and interactions
 * Similar to restaurants page search functionality
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

// Header Search Functionality - Similar to restaurants page
function handleHeaderSearch() {
    const searchInput = document.getElementById('headerSearchInput');
    const searchForm = document.getElementById('headerSearchForm');
    
    if (!searchInput || !searchForm) {
        return;
    }
    
    // Handle Enter key press
    searchInput.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            submitHeaderSearch();
        }
    });
    
    // Handle form submission
    searchForm.addEventListener('submit', function(event) {
        event.preventDefault();
        submitHeaderSearch();
    });
}

function submitHeaderSearch() {
    const searchInput = document.getElementById('headerSearchInput');
    const cuisineFilter = document.getElementById('headerCuisineFilter');
    const priceFilter = document.getElementById('headerPriceFilter');
    const sortSelect = document.getElementById('headerSortSelect');
    
    if (!searchInput || !cuisineFilter || !priceFilter || !sortSelect) {
        console.error('Header filter elements not found');
        return;
    }
    
    const search = searchInput.value.trim();
    const cuisineType = cuisineFilter.value;
    const priceRange = priceFilter.value;
    const sortValue = sortSelect.value;
    const [sortBy, sortDir] = sortValue.split('-');
    
    // Check if we're on the restaurants page
    const isOnRestaurantsPage = window.location.pathname === '/restaurants' || 
                               window.location.pathname.endsWith('/restaurants');
    
    if (isOnRestaurantsPage) {
        // If on restaurants page, preserve existing query params (like location) and update filters
        const url = new URL(window.location);
        
        // Set search params
        if (search) {
            url.searchParams.set('search', search);
        } else {
            url.searchParams.delete('search');
        }
        
        if (cuisineType) {
            url.searchParams.set('cuisineType', cuisineType);
        } else {
            url.searchParams.delete('cuisineType');
        }
        
        if (priceRange) {
            url.searchParams.set('priceRange', priceRange);
        } else {
            url.searchParams.delete('priceRange');
        }
        
        // Preserve location parameters if they exist
        const urlParams = new URLSearchParams(window.location.search);
        const latitude = urlParams.get('latitude');
        const longitude = urlParams.get('longitude');
        const nearby = urlParams.get('nearby');
        
        // Preserve location parameters if they exist
        if (latitude && longitude && nearby === 'true') {
            // Location available - preserve it
            url.searchParams.set('latitude', latitude);
            url.searchParams.set('longitude', longitude);
            url.searchParams.set('nearby', 'true');
            
            // Preserve maxDistance if it exists
            const maxDistance = urlParams.get('maxDistance');
            if (maxDistance && maxDistance !== '') {
                url.searchParams.set('maxDistance', maxDistance);
                // If distance filter is applied, sort by distance
                url.searchParams.set('sortBy', 'distance');
                url.searchParams.set('sortDir', 'asc');
            } else {
                // Use selected sort
                url.searchParams.set('sortBy', sortBy);
                url.searchParams.set('sortDir', sortDir);
            }
        } else {
            // No location - use selected sort
            url.searchParams.set('sortBy', sortBy);
            url.searchParams.set('sortDir', sortDir);
            // Remove maxDistance if no location
            url.searchParams.delete('maxDistance');
        }
        
        // Reset to first page when filtering
        url.searchParams.set('page', '0');
        
        // Navigate to updated URL
        window.location.href = url.toString();
    } else {
        // If not on restaurants page, redirect to restaurants with all filter params
        const url = new URL('/restaurants', window.location.origin);
        
        if (search) {
            url.searchParams.set('search', search);
        }
        
        if (cuisineType) {
            url.searchParams.set('cuisineType', cuisineType);
        }
        
        if (priceRange) {
            url.searchParams.set('priceRange', priceRange);
        }
        
        url.searchParams.set('sortBy', sortBy);
        url.searchParams.set('sortDir', sortDir);
        url.searchParams.set('page', '0');
        
        // Navigate to restaurants page
        window.location.href = url.toString();
    }
}

// Clear all header filters function
window.clearHeaderFilters = function() {
    const searchInput = document.getElementById('headerSearchInput');
    const cuisineFilter = document.getElementById('headerCuisineFilter');
    const priceFilter = document.getElementById('headerPriceFilter');
    const sortSelect = document.getElementById('headerSortSelect');
    
    if (searchInput) searchInput.value = '';
    if (cuisineFilter) cuisineFilter.value = '';
    if (priceFilter) priceFilter.value = '';
    if (sortSelect) sortSelect.value = 'restaurantName-asc';
    
    // Navigate to restaurants page with cleared filters
    const url = new URL('/restaurants', window.location.origin);
    
    // Preserve location if available
    const urlParams = new URLSearchParams(window.location.search);
    const latitude = urlParams.get('latitude');
    const longitude = urlParams.get('longitude');
    const nearby = urlParams.get('nearby');
    
    if (latitude && longitude && nearby === 'true') {
        url.searchParams.set('latitude', latitude);
        url.searchParams.set('longitude', longitude);
        url.searchParams.set('nearby', 'true');
    }
    
    url.searchParams.set('page', '0');
    url.searchParams.set('sortBy', 'restaurantName');
    url.searchParams.set('sortDir', 'asc');
    
    window.location.href = url.toString();
};

// Initialize when DOM is ready
(function() {
    // Use DOMContentLoaded or immediate execution if DOM already loaded
    function init() {
        // Initialize header search functionality
        handleHeaderSearch();
        
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

