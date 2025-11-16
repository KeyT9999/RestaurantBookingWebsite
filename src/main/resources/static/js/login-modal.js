/**
 * Login Modal Helper Functions and Initialization
 * Handles login modal interactions and Bootstrap dropdown initialization
 */

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    'use strict';
    
    // Guard: Check if modal element exists before proceeding
    const modalElement = document.getElementById('loginModal');
    
    if (!modalElement) {
        console.warn('Login modal (#loginModal) not found in DOM, skip init');
        return; // CRITICAL: Don't try to initialize if element doesn't exist
    }
    
    // Guard: Check if Bootstrap is available
    if (typeof bootstrap === 'undefined' || !bootstrap.Modal) {
        console.warn('Bootstrap Modal not available, will retry...');
        // Retry after a short delay
        setTimeout(arguments.callee, 100);
        return;
    }
    
    // Create modal instance once
    let loginModal;
    try {
        loginModal = bootstrap.Modal.getOrCreateInstance(modalElement);
        console.log('Login modal initialized successfully');
    } catch (e) {
        console.error('Error initializing login modal:', e);
        return; // Don't proceed if initialization failed
    }
    
    // Global function to close login modal
    window.closeLoginModal = function() {
        if (loginModal) {
            try {
                loginModal.hide();
            } catch (e) {
                console.error('Error closing login modal:', e);
            }
        }
    };
    
    // Intercept login button clicks BEFORE Bootstrap auto-initializes
    // This prevents Bootstrap from trying to initialize modal before element exists
    document.addEventListener('click', function(event) {
        const target = event.target.closest('[data-bs-toggle="modal"][data-bs-target="#loginModal"]');
        if (!target) return;
        
        // Stop Bootstrap's auto-initialization
        event.preventDefault();
        event.stopPropagation();
        event.stopImmediatePropagation();
        
        // Show the modal using our pre-initialized instance
        if (loginModal) {
            try {
                loginModal.show();
            } catch (e) {
                console.error('Error showing login modal:', e);
            }
        }
    }, true); // Use capture phase to intercept before Bootstrap
    
    // Auto-focus on username input when modal opens
    modalElement.addEventListener('shown.bs.modal', function() {
        const usernameInput = modalElement.querySelector('#username');
        if (usernameInput) {
            setTimeout(() => usernameInput.focus(), 100);
        }
    });
    
    // Initialize Bootstrap dropdowns
    function initBootstrapDropdowns() {
        if (typeof bootstrap !== 'undefined' && bootstrap.Dropdown) {
            const dropdownElements = document.querySelectorAll('[data-bs-toggle="dropdown"]');
            dropdownElements.forEach(function(dropdownElement) {
                try {
                    bootstrap.Dropdown.getOrCreateInstance(dropdownElement);
                } catch (e) {
                    // Dropdown already initialized, ignore
                }
            });
        }
    }
    
    initBootstrapDropdowns();
});
