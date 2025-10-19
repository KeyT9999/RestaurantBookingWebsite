/**
 * HOME PAGE - RESY STYLE
 * JavaScript for interactive elements
 */

(function() {
    'use strict';

    /**
     * Initialize when DOM is ready
     */
    document.addEventListener('DOMContentLoaded', function() {
        initSmoothScrolling();
        initBookButtons();
        initCategoryCards();
    });

    /**
     * Smooth scrolling for anchor links
     */
    function initSmoothScrolling() {
        const anchorLinks = document.querySelectorAll('a[href^="#"]');
        
        anchorLinks.forEach(anchor => {
            anchor.addEventListener('click', function(e) {
                const href = this.getAttribute('href');
                
                // Ignore empty anchors
                if (href === '#' || href === '#!') {
                    e.preventDefault();
                    return;
                }
                
                const target = document.querySelector(href);
                
                if (target) {
                    e.preventDefault();
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            });
        });
    }

    /**
     * Book button interactions
     */
    function initBookButtons() {
        const bookButtons = document.querySelectorAll('.book-btn');
        
        bookButtons.forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                
                // Get restaurant name from card
                const card = this.closest('.restaurant-card');
                const restaurantName = card ? card.querySelector('.restaurant-name')?.textContent : 'nhà hàng này';
                
                // TODO: Replace with actual booking flow
                alert(`Đang chuyển đến trang đặt bàn cho ${restaurantName}...`);
                
                // Uncomment when booking page is ready:
                // window.location.href = '/booking/create?restaurant=' + restaurantId;
            });
        });
    }

    /**
     * Category card interactions
     */
    function initCategoryCards() {
        const categoryCards = document.querySelectorAll('.category-card');
        
        categoryCards.forEach(card => {
            card.addEventListener('click', function() {
                const categoryName = this.querySelector('.category-name')?.textContent;
                
                // TODO: Replace with actual category filtering
                console.log(`Filtering by category: ${categoryName}`);
                
                // Uncomment when category filtering is ready:
                // window.location.href = '/restaurants?category=' + encodeURIComponent(categoryName);
            });
        });
    }

    /**
     * Partner card hover effects (optional enhancement)
     */
    function initPartnerCards() {
        const partnerCards = document.querySelectorAll('.partner-card');
        
        partnerCards.forEach(card => {
            card.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-5px)';
            });
            
            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
            });
        });
    }

    /**
     * Video error handling (optional)
     */
    function initVideoErrorHandling() {
        const iframes = document.querySelectorAll('iframe');
        
        iframes.forEach(iframe => {
            iframe.addEventListener('error', function() {
                console.warn('Video failed to load:', this.src);
                
                // Optional: Show fallback image
                const parent = this.parentElement;
                if (parent && parent.classList.contains('video-background')) {
                    parent.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
                }
            });
        });
    }

    // Export for potential external use
    window.HomeResyJS = {
        initSmoothScrolling,
        initBookButtons,
        initCategoryCards
    };

})();

