/**
 * Gooey Navigation Effect for Book Eat Header
 * Adapted from React component to vanilla JavaScript
 */

class GooeyNav {
  constructor(options = {}) {
    this.options = {
      animationTime: 600,
      particleCount: 15,
      particleDistances: [90, 10],
      particleR: 100,
      timeVariance: 300,
      colors: [1, 2, 3, 1, 2, 3, 1, 4],
      initialActiveIndex: 0,
      ...options
    };
    
    this.container = null;
    this.nav = null;
    this.filter = null;
    this.text = null;
    this.activeIndex = this.options.initialActiveIndex;
    this.isInitialized = false;
  }

  init(containerSelector) {
    this.container = document.querySelector(containerSelector);
    if (!this.container) {
      console.error('GooeyNav: Container not found');
      return;
    }

    this.setupElements();
    this.setupEventListeners();
    this.updateEffectPosition();
    this.isInitialized = true;
  }

  setupElements() {
    // Create effect elements
    this.filter = document.createElement('span');
    this.filter.className = 'effect filter';
    this.text = document.createElement('span');
    this.text.className = 'effect text';
    
    // Find the nav element
    this.nav = this.container.querySelector('nav ul');
    if (!this.nav) {
      console.error('GooeyNav: Navigation list not found');
      return;
    }

    // Add gooey container class
    this.container.classList.add('gooey-nav-container');
    
    // Append effect elements
    this.container.appendChild(this.filter);
    this.container.appendChild(this.text);
  }

  setupEventListeners() {
    if (!this.nav) return;

    const items = this.nav.querySelectorAll('li');
    items.forEach((item, index) => {
      const link = item.querySelector('a');
      if (link) {
        link.addEventListener('click', (e) => this.handleClick(e, index));
        link.addEventListener('keydown', (e) => this.handleKeyDown(e, index));
      }
    });

    // Set initial active state
    this.setActiveIndex(this.activeIndex);
  }

  handleClick(e, index) {
    e.preventDefault();
    
    if (this.activeIndex === index) return;

    this.setActiveIndex(index);
    this.updateEffectPosition(e.currentTarget.parentElement);
    this.makeParticles();
  }

  handleKeyDown(e, index) {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      this.handleClick(e, index);
    }
  }

  setActiveIndex(index) {
    const items = this.nav.querySelectorAll('li');
    
    // Remove active class from all items
    items.forEach(item => item.classList.remove('active'));
    
    // Add active class to current item
    if (items[index]) {
      items[index].classList.add('active');
      this.activeIndex = index;
    }
  }

  updateEffectPosition(element = null) {
    if (!this.container || !this.filter || !this.text) return;

    const targetElement = element || this.nav.querySelectorAll('li')[this.activeIndex];
    if (!targetElement) return;

    const containerRect = this.container.getBoundingClientRect();
    const pos = targetElement.getBoundingClientRect();

    const styles = {
      left: `${pos.x - containerRect.x}px`,
      top: `${pos.y - containerRect.y}px`,
      width: `${pos.width}px`,
      height: `${pos.height}px`
    };

    Object.assign(this.filter.style, styles);
    Object.assign(this.text.style, styles);
    
    // Update text content
    const link = targetElement.querySelector('a');
    if (link) {
      this.text.innerText = link.innerText.trim();
    }

    // Trigger text animation
    this.text.classList.remove('active');
    void this.text.offsetWidth; // Force reflow
    this.text.classList.add('active');
  }

  noise(n = 1) {
    return n / 2 - Math.random() * n;
  }

  getXY(distance, pointIndex, totalPoints) {
    const angle = ((360 + this.noise(8)) / totalPoints) * pointIndex * (Math.PI / 180);
    return [distance * Math.cos(angle), distance * Math.sin(angle)];
  }

  createParticle(i, t, d, r) {
    let rotate = this.noise(r / 10);
    return {
      start: this.getXY(d[0], this.options.particleCount - i, this.options.particleCount),
      end: this.getXY(d[1] + this.noise(7), this.options.particleCount - i, this.options.particleCount),
      time: t,
      scale: 1 + this.noise(0.2),
      color: this.options.colors[Math.floor(Math.random() * this.options.colors.length)],
      rotate: rotate > 0 ? (rotate + r / 20) * 10 : (rotate - r / 20) * 10
    };
  }

  makeParticles() {
    if (!this.filter) return;

    // Clear existing particles
    const existingParticles = this.filter.querySelectorAll('.particle');
    existingParticles.forEach(p => p.remove());

    const d = this.options.particleDistances;
    const r = this.options.particleR;
    const bubbleTime = this.options.animationTime * 2 + this.options.timeVariance;
    
    this.filter.style.setProperty('--time', `${bubbleTime}ms`);

    for (let i = 0; i < this.options.particleCount; i++) {
      const t = this.options.animationTime * 2 + this.noise(this.options.timeVariance * 2);
      const p = this.createParticle(i, t, d, r);

      setTimeout(() => {
        const particle = document.createElement('span');
        const point = document.createElement('span');
        
        particle.classList.add('particle');
        particle.style.setProperty('--start-x', `${p.start[0]}px`);
        particle.style.setProperty('--start-y', `${p.start[1]}px`);
        particle.style.setProperty('--end-x', `${p.end[0]}px`);
        particle.style.setProperty('--end-y', `${p.end[1]}px`);
        particle.style.setProperty('--time', `${p.time}ms`);
        particle.style.setProperty('--scale', `${p.scale}`);
        particle.style.setProperty('--color', `var(--color-${p.color}, var(--primary-blue))`);
        particle.style.setProperty('--rotate', `${p.rotate}deg`);

        point.classList.add('point');
        particle.appendChild(point);
        this.filter.appendChild(particle);

        requestAnimationFrame(() => {
          this.filter.classList.add('active');
        });

        setTimeout(() => {
          try {
            if (particle.parentNode) {
              particle.parentNode.removeChild(particle);
            }
          } catch (e) {
            // Particle already removed
          }
        }, t);
      }, 30);
    }
  }

  // Public method to update active item programmatically
  setActive(index) {
    if (index >= 0 && index < this.nav.querySelectorAll('li').length) {
      this.setActiveIndex(index);
      this.updateEffectPosition();
    }
  }

  // Public method to destroy the effect
  destroy() {
    if (this.filter) {
      const particles = this.filter.querySelectorAll('.particle');
      particles.forEach(p => p.remove());
    }
    
    if (this.text) {
      this.text.classList.remove('active');
    }
    
    this.isInitialized = false;
  }
}

// Initialize GooeyNav when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
  // Wait a bit for the header to be fully rendered
  setTimeout(() => {
    const headerNav = document.querySelector('.navbar-nav.gooey-nav-items');
    if (headerNav) {
      const gooeyNav = new GooeyNav({
        particleCount: 12,
        particleDistances: [80, 15],
        animationTime: 500,
        colors: [1, 2, 3, 1, 2, 3, 1, 4]
      });
      
      gooeyNav.init('.navbar-nav.gooey-nav-items');
      
      // Store reference globally for potential external access
      window.bookEatGooeyNav = gooeyNav;
      
      console.log('GooeyNav initialized successfully');
    } else {
      console.warn('GooeyNav: Navigation container not found');
    }
  }, 200);
});

// Export for potential module usage
if (typeof module !== 'undefined' && module.exports) {
  module.exports = GooeyNav;
}
