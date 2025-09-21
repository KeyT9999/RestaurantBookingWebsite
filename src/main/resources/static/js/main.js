/**
 * AURELIUS LUXURY - Main JavaScript
 * Handles navigation, scroll effects, and UI interactions
 */

(function() {
    'use strict';

    // DOM Elements
    const navbar = document.getElementById('luxuryNavbar');
    const navToggler = document.querySelector('.navbar-toggler');
    const navCollapse = document.querySelector('.navbar-collapse');
    const navLinks = document.querySelectorAll('.nav-link');

    // Initialize when DOM is loaded
    document.addEventListener('DOMContentLoaded', function() {
        initNavbarScroll();
        initSmoothScroll();
        initMobileMenu();
        initFormValidation();
        initScrollToTop();
        initLazyLoading();
    });

    /**
     * Navbar scroll effect
     */
    function initNavbarScroll() {
        if (!navbar) return;

        let lastScrollY = window.scrollY;
        let ticking = false;

        function updateNavbar() {
            const scrollY = window.scrollY;
            
            if (scrollY > 50) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }

            // Hide/show navbar on scroll
            if (scrollY > lastScrollY && scrollY > 100) {
                navbar.style.transform = 'translateY(-100%)';
            } else {
                navbar.style.transform = 'translateY(0)';
            }

            lastScrollY = scrollY;
            ticking = false;
        }

        function requestTick() {
            if (!ticking) {
                requestAnimationFrame(updateNavbar);
                ticking = true;
            }
        }

        window.addEventListener('scroll', requestTick, { passive: true });
    }

    /**
     * Smooth scrolling for anchor links
     */
    function initSmoothScroll() {
        const smoothScrollLinks = document.querySelectorAll('a[href^="#"]');

        smoothScrollLinks.forEach(link => {
            link.addEventListener('click', function(e) {
                const targetId = this.getAttribute('href');
                if (targetId === '#') return;

                const targetElement = document.querySelector(targetId);
                if (!targetElement) return;

                e.preventDefault();

                const headerHeight = navbar ? navbar.offsetHeight : 0;
                const targetPosition = targetElement.offsetTop - headerHeight - 20;

                window.scrollTo({
                    top: targetPosition,
                    behavior: 'smooth'
                });

                // Close mobile menu if open
                if (navCollapse && navCollapse.classList.contains('show')) {
                    bootstrap.Collapse.getInstance(navCollapse).hide();
                }

                // Update active nav link
                updateActiveNavLink(targetId);
            });
        });
    }

    /**
     * Mobile menu interactions
     */
    function initMobileMenu() {
        if (!navToggler || !navCollapse) return;

        // Close menu when clicking nav links
        navLinks.forEach(link => {
            link.addEventListener('click', function() {
                if (navCollapse.classList.contains('show')) {
                    bootstrap.Collapse.getInstance(navCollapse).hide();
                }
            });
        });

        // Close menu when clicking outside
        document.addEventListener('click', function(e) {
            const isClickInsideNav = navbar.contains(e.target);
            
            if (!isClickInsideNav && navCollapse.classList.contains('show')) {
                bootstrap.Collapse.getInstance(navCollapse).hide();
            }
        });

        // Handle escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && navCollapse.classList.contains('show')) {
                bootstrap.Collapse.getInstance(navCollapse).hide();
            }
        });
    }

    /**
     * Update active navigation link
     */
    function updateActiveNavLink(targetId) {
        navLinks.forEach(link => {
            link.classList.remove('active');
            if (link.getAttribute('href') === targetId) {
                link.classList.add('active');
            }
        });
    }

    /**
     * Form validation enhancements
     */
    function initFormValidation() {
        const forms = document.querySelectorAll('.needs-validation');
        
        forms.forEach(form => {
            form.addEventListener('submit', function(e) {
                if (!form.checkValidity()) {
                    e.preventDefault();
                    e.stopPropagation();
                    
                    // Focus first invalid field
                    const firstInvalid = form.querySelector(':invalid');
                    if (firstInvalid) {
                        firstInvalid.focus();
                    }
                }
                
                form.classList.add('was-validated');
            });
        });

        // Real-time validation for booking form
        const bookingForm = document.querySelector('.booking-form');
        if (bookingForm) {
            initBookingFormValidation(bookingForm);
        }
    }

    /**
     * Booking form specific validation
     */
    function initBookingFormValidation(form) {
        const dateInput = form.querySelector('#date');
        const timeInput = form.querySelector('#time');
        const guestsInput = form.querySelector('#guests');

        // Set minimum date to today
        if (dateInput) {
            const today = new Date().toISOString().split('T')[0];
            dateInput.min = today;
            
            // If no date selected, set to today
            if (!dateInput.value) {
                dateInput.value = today;
            }
        }

        // Validate date selection
        if (dateInput) {
            dateInput.addEventListener('change', function() {
                const selectedDate = new Date(this.value);
                const today = new Date();
                today.setHours(0, 0, 0, 0);

                if (selectedDate < today) {
                    this.setCustomValidity('Ngày đặt bàn không thể là ngày trong quá khứ');
                } else {
                    this.setCustomValidity('');
                }
            });
        }

        // Validate time selection based on current time
        if (timeInput && dateInput) {
            function validateTime() {
                const selectedDate = new Date(dateInput.value);
                const today = new Date();
                const selectedTime = timeInput.value;

                if (selectedDate.toDateString() === today.toDateString()) {
                    const currentHour = today.getHours();
                    const selectedHour = parseInt(selectedTime.split(':')[0]);

                    if (selectedHour <= currentHour) {
                        timeInput.setCustomValidity('Vui lòng chọn giờ sau thời gian hiện tại');
                        return false;
                    }
                }
                
                timeInput.setCustomValidity('');
                return true;
            }

            timeInput.addEventListener('change', validateTime);
            dateInput.addEventListener('change', validateTime);
        }
    }

    /**
     * Scroll to top functionality
     */
    function initScrollToTop() {
        // Create scroll to top button
        const scrollTopBtn = document.createElement('button');
        scrollTopBtn.className = 'scroll-top-btn';
        scrollTopBtn.innerHTML = '<i class="fas fa-chevron-up"></i>';
        scrollTopBtn.setAttribute('aria-label', 'Scroll to top');
        scrollTopBtn.style.cssText = `
            position: fixed;
            bottom: 30px;
            right: 30px;
            width: 50px;
            height: 50px;
            background: linear-gradient(135deg, var(--luxury-gold), var(--luxury-gold-light));
            color: var(--luxury-black);
            border: none;
            border-radius: 50%;
            font-size: 1.2rem;
            cursor: pointer;
            opacity: 0;
            visibility: hidden;
            transition: all 0.3s ease;
            z-index: 1000;
            box-shadow: 0 4px 15px rgba(212, 175, 55, 0.3);
        `;

        document.body.appendChild(scrollTopBtn);

        // Show/hide button based on scroll position
        function toggleScrollTopBtn() {
            if (window.scrollY > 300) {
                scrollTopBtn.style.opacity = '1';
                scrollTopBtn.style.visibility = 'visible';
            } else {
                scrollTopBtn.style.opacity = '0';
                scrollTopBtn.style.visibility = 'hidden';
            }
        }

        // Scroll to top on click
        scrollTopBtn.addEventListener('click', function() {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });

        // Hover effects
        scrollTopBtn.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-3px) scale(1.1)';
            this.style.boxShadow = '0 6px 20px rgba(212, 175, 55, 0.5)';
        });

        scrollTopBtn.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
            this.style.boxShadow = '0 4px 15px rgba(212, 175, 55, 0.3)';
        });

        window.addEventListener('scroll', toggleScrollTopBtn, { passive: true });
    }

    /**
     * Lazy loading for images
     */
    function initLazyLoading() {
        if ('IntersectionObserver' in window) {
            const imageObserver = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const img = entry.target;
                        img.src = img.dataset.src;
                        img.classList.remove('lazy');
                        imageObserver.unobserve(img);
                    }
                });
            });

            const lazyImages = document.querySelectorAll('img[data-src]');
            lazyImages.forEach(img => imageObserver.observe(img));
        }
    }

    /**
     * Section reveal animation
     */
    function initSectionReveal() {
        if ('IntersectionObserver' in window && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
            const revealObserver = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        entry.target.classList.add('revealed');
                        revealObserver.unobserve(entry.target);
                    }
                });
            }, {
                threshold: 0.1,
                rootMargin: '0px 0px -50px 0px'
            });

            const revealElements = document.querySelectorAll('.reveal');
            revealElements.forEach(el => revealObserver.observe(el));
        }
    }

    /**
     * Newsletter form handling
     */
    function initNewsletterForm() {
        const newsletterForm = document.querySelector('.newsletter-form');
        if (!newsletterForm) return;

        newsletterForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const email = this.querySelector('input[type="email"]').value;
            const submitBtn = this.querySelector('button[type="submit"]');
            const originalText = submitBtn.innerHTML;
            
            // Show loading state
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
            submitBtn.disabled = true;
            
            // Simulate API call (replace with actual endpoint)
            setTimeout(() => {
                // Show success message
                const successMsg = document.createElement('div');
                successMsg.className = 'alert alert-success mt-2';
                successMsg.innerHTML = '<i class="fas fa-check-circle"></i> Cảm ơn bạn đã đăng ký!';
                
                this.appendChild(successMsg);
                this.reset();
                
                // Reset button
                submitBtn.innerHTML = originalText;
                submitBtn.disabled = false;
                
                // Remove success message after 3 seconds
                setTimeout(() => {
                    successMsg.remove();
                }, 3000);
            }, 1000);
        });
    }

    /**
     * Utility functions
     */
    const utils = {
        // Debounce function
        debounce(func, wait, immediate) {
            let timeout;
            return function executedFunction(...args) {
                const later = () => {
                    timeout = null;
                    if (!immediate) func(...args);
                };
                const callNow = immediate && !timeout;
                clearTimeout(timeout);
                timeout = setTimeout(later, wait);
                if (callNow) func(...args);
            };
        },

        // Throttle function
        throttle(func, limit) {
            let inThrottle;
            return function() {
                const args = arguments;
                const context = this;
                if (!inThrottle) {
                    func.apply(context, args);
                    inThrottle = true;
                    setTimeout(() => inThrottle = false, limit);
                }
            };
        },

        // Check if element is in viewport
        isInViewport(element) {
            const rect = element.getBoundingClientRect();
            return (
                rect.top >= 0 &&
                rect.left >= 0 &&
                rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
                rect.right <= (window.innerWidth || document.documentElement.clientWidth)
            );
        }
    };

    // Performance optimization
    if (window.requestIdleCallback) {
        window.requestIdleCallback(() => {
            initSectionReveal();
            initNewsletterForm();
        });
    } else {
        setTimeout(() => {
            initSectionReveal();
            initNewsletterForm();
        }, 100);
    }

    // Export utils for global use
    window.AureliusUtils = utils;

    // Handle page visibility changes
    document.addEventListener('visibilitychange', function() {
        if (document.hidden) {
            // Pause any animations or auto-playing content
            console.log('Page hidden - pausing animations');
        } else {
            // Resume animations
            console.log('Page visible - resuming animations');
        }
    });

})(); 