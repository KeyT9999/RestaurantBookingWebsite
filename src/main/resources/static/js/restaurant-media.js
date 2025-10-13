/**
 * Restaurant Media Management JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeMediaManagement();
});

function initializeMediaManagement() {
    // Initialize filter functionality
    initializeFilters();
    
    // Initialize delete functionality
    initializeDeleteButtons();
    
    // Initialize image loading states
    initializeImageLoading();
}

/**
 * Initialize media filters
 */
function initializeFilters() {
    const filterButtons = document.querySelectorAll('.filter-btn');
    const mediaCards = document.querySelectorAll('.media-card');
    
    filterButtons.forEach(button => {
        button.addEventListener('click', function() {
            const filterType = this.getAttribute('data-type');
            
            // Update active button
            filterButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            
            // Filter media cards
            filterMediaCards(mediaCards, filterType);
        });
    });
}

/**
 * Filter media cards by type
 */
function filterMediaCards(cards, filterType) {
    cards.forEach(card => {
        const cardType = card.getAttribute('data-type');
        
        if (filterType === 'all' || cardType === filterType) {
            card.style.display = 'block';
            // Add animation
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            
            setTimeout(() => {
                card.style.transition = 'all 0.3s ease';
                card.style.opacity = '1';
                card.style.transform = 'translateY(0)';
            }, 50);
        } else {
            card.style.transition = 'all 0.3s ease';
            card.style.opacity = '0';
            card.style.transform = 'translateY(-20px)';
            
            setTimeout(() => {
                card.style.display = 'none';
            }, 300);
        }
    });
    
    // Update empty state visibility
    updateEmptyState();
}

/**
 * Update empty state visibility
 */
function updateEmptyState() {
    const mediaGrid = document.getElementById('mediaGrid');
    const visibleCards = Array.from(mediaGrid.querySelectorAll('.media-card'))
        .filter(card => card.style.display !== 'none');
    
    const emptyState = mediaGrid.querySelector('.empty-state');
    if (emptyState) {
        emptyState.style.display = visibleCards.length === 0 ? 'flex' : 'none';
    }
}

/**
 * Initialize delete buttons
 */
function initializeDeleteButtons() {
    const deleteButtons = document.querySelectorAll('.delete-media-btn');
    
    deleteButtons.forEach(button => {
        button.addEventListener('click', function() {
            const mediaId = this.getAttribute('data-media-id');
            const mediaType = this.getAttribute('data-media-type');
            
            showDeleteModal(mediaId, mediaType);
        });
    });
}

/**
 * Show delete confirmation modal
 */
function showDeleteModal(mediaId, mediaType) {
    const modal = document.getElementById('deleteModal');
    const deleteForm = document.getElementById('deleteForm');
    const mediaPreview = document.getElementById('mediaPreview');
    
    // Update form action
    deleteForm.action = `/restaurant-owner/restaurants/${window.restaurantId}/media/${mediaId}/delete`;
    
    // Update preview content
    mediaPreview.innerHTML = `
        <div class="media-preview-content">
            <strong>Loại:</strong> ${getMediaTypeLabel(mediaType)}<br>
            <strong>ID:</strong> ${mediaId}
        </div>
    `;
    
    // Show modal
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
    
    // Add animation
    modal.style.opacity = '0';
    setTimeout(() => {
        modal.style.transition = 'opacity 0.3s ease';
        modal.style.opacity = '1';
    }, 10);
}

/**
 * Close delete modal
 */
function closeDeleteModal() {
    const modal = document.getElementById('deleteModal');
    
    modal.style.transition = 'opacity 0.3s ease';
    modal.style.opacity = '0';
    
    setTimeout(() => {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }, 300);
}

/**
 * Get media type label
 */
function getMediaTypeLabel(type) {
    const labels = {
        'gallery': 'Gallery',
        'menu': 'Menu',
        'interior': 'Nội thất',
        'exterior': 'Ngoại thất',
        'table_layout': 'Layout bàn'
    };
    return labels[type] || type;
}

/**
 * Initialize image loading states
 */
function initializeImageLoading() {
    const images = document.querySelectorAll('.media-img');
    
    images.forEach(img => {
        const card = img.closest('.media-card');
        const loadingDiv = card.querySelector('.media-loading');
        const placeholderDiv = card.querySelector('.media-placeholder');
        
        // Don't show loading for existing images that are already loaded
        if (img.complete && img.naturalHeight !== 0) {
            // Image is already loaded
            if (loadingDiv) {
                loadingDiv.classList.remove('show');
            }
            if (placeholderDiv) {
                placeholderDiv.style.display = 'none';
            }
        } else {
            // Show loading for images that are still loading
            if (loadingDiv) {
                loadingDiv.classList.add('show');
            }
        }
        
        // Handle image load
        img.addEventListener('load', function() {
            if (loadingDiv) {
                loadingDiv.classList.remove('show');
            }
            if (placeholderDiv) {
                placeholderDiv.style.display = 'none';
            }
        });
        
        // Handle image error
        img.addEventListener('error', function() {
            if (loadingDiv) {
                loadingDiv.classList.remove('show');
            }
            if (placeholderDiv) {
                placeholderDiv.style.display = 'flex';
            }
        });
    });
}

/**
 * Utility function to show toast notifications
 */
function showToast(message, type = 'info') {
    // Create toast element
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <i class="icon">${type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️'}</i>
            <span>${message}</span>
        </div>
    `;
    
    // Add styles
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#10b981' : type === 'error' ? '#ef4444' : '#3b82f6'};
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 8px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
        z-index: 1001;
        transform: translateX(100%);
        transition: transform 0.3s ease;
    `;
    
    // Add to page
    document.body.appendChild(toast);
    
    // Animate in
    setTimeout(() => {
        toast.style.transform = 'translateX(0)';
    }, 10);
    
    // Remove after delay
    setTimeout(() => {
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => {
            document.body.removeChild(toast);
        }, 300);
    }, 3000);
}

/**
 * Handle form submission with loading state
 */
function handleFormSubmission(form) {
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    
    // Show loading state
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="icon">⏳</i> Đang xử lý...';
    
    // Re-enable after 5 seconds as fallback
    setTimeout(() => {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }, 5000);
}

/**
 * Initialize drag and drop for file upload
 */
function initializeDragAndDrop() {
    const uploadArea = document.getElementById('fileUploadArea');
    const fileInput = document.getElementById('mediaFiles');
    
    if (!uploadArea || !fileInput) return;
    
    // Prevent default drag behaviors
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        uploadArea.addEventListener(eventName, preventDefaults, false);
        document.body.addEventListener(eventName, preventDefaults, false);
    });
    
    // Highlight drop area when item is dragged over it
    ['dragenter', 'dragover'].forEach(eventName => {
        uploadArea.addEventListener(eventName, highlight, false);
    });
    
    ['dragleave', 'drop'].forEach(eventName => {
        uploadArea.addEventListener(eventName, unhighlight, false);
    });
    
    // Handle dropped files
    uploadArea.addEventListener('drop', handleDrop, false);
    
    function preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }
    
    function highlight(e) {
        uploadArea.classList.add('drag-over');
    }
    
    function unhighlight(e) {
        uploadArea.classList.remove('drag-over');
    }
    
    function handleDrop(e) {
        const dt = e.dataTransfer;
        const files = dt.files;
        
        fileInput.files = files;
        
        // Trigger change event
        const event = new Event('change', { bubbles: true });
        fileInput.dispatchEvent(event);
    }
}

/**
 * Initialize file validation
 */
function initializeFileValidation() {
    const fileInput = document.getElementById('mediaFiles');
    
    if (!fileInput) return;
    
    fileInput.addEventListener('change', function() {
        const files = Array.from(this.files);
        const maxSize = 10 * 1024 * 1024; // 10MB
        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
        
        let hasErrors = false;
        
        files.forEach(file => {
            if (!allowedTypes.includes(file.type)) {
                showToast(`File "${file.name}" không đúng định dạng. Chỉ chấp nhận JPG, PNG, WebP.`, 'error');
                hasErrors = true;
            }
            
            if (file.size > maxSize) {
                showToast(`File "${file.name}" quá lớn. Tối đa 10MB.`, 'error');
                hasErrors = true;
            }
        });
        
        if (hasErrors) {
            this.value = '';
        }
    });
}

// Initialize additional features when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeDragAndDrop();
    initializeFileValidation();
    
    // Handle form submissions
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function() {
            handleFormSubmission(this);
        });
    });
    
    // Close modal when clicking outside
    const modal = document.getElementById('deleteModal');
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeDeleteModal();
            }
        });
    }
    
    // Close modal with Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeDeleteModal();
        }
    });
});
