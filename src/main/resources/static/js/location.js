(() => {
  const storageKey = 'userLocation';
  const promptKey = 'locationPromptHandled';
  const permissionKey = 'locationPermissionState';
  let geoWatchId = null;
  let lastUpdate = { ts: 0, lat: null, lng: null };
  const MIN_UPDATE_MS = 3 * 60 * 1000; // 3 minutes
  const MIN_MOVE_M = 200; // 200 meters

  function showModal() {
    const el = document.getElementById('locationPermissionModal');
    if (!el) return;
    const modal = new (window.bootstrap && window.bootstrap.Modal ? window.bootstrap.Modal : function(){}) (el);
    modal.show();
  }

  function hideModal() {
    const el = document.getElementById('locationPermissionModal');
    if (!el) return;
    const Modal = window.bootstrap && window.bootstrap.Modal;
    if (!Modal) return;
    const modal = Modal.getInstance(el) || new Modal(el);
    modal.hide();
  }

  function setLocation(lat, lng) {
    sessionStorage.setItem(storageKey, JSON.stringify({ lat, lng, ts: Date.now() }));
    sessionStorage.setItem(promptKey, '1');
    localStorage.setItem(permissionKey, 'granted');
    // Also store in localStorage for cross-page access
    localStorage.setItem(storageKey, JSON.stringify({ lat, lng, ts: Date.now() }));
  }

  function clearLocation() {
    sessionStorage.removeItem(storageKey);
    sessionStorage.removeItem(promptKey);
    localStorage.removeItem(storageKey);
    localStorage.removeItem(permissionKey);
  }

  function hasLocation() {
    return !!(sessionStorage.getItem(storageKey) || localStorage.getItem(storageKey));
  }

  function getLocation() {
    try { 
      const sessionLoc = sessionStorage.getItem(storageKey);
      if (sessionLoc) return JSON.parse(sessionLoc);
      const localLoc = localStorage.getItem(storageKey);
      if (localLoc) return JSON.parse(localLoc);
      return {};
    } catch { return {}; }
  }
  
  /**
   * Get user location - returns cached location if available, otherwise requests permission
   * This function automatically uses cached location if permission was granted before,
   * allowing location access across all pages without re-prompting until logout
   * @param {Function} onSuccess - Callback with {latitude, longitude}
   * @param {Function} onError - Callback with error message
   * @param {Object} options - Options for geolocation request
   */
  function getUserLocation(onSuccess, onError, options = {}) {
    console.log('📍 getUserLocation() called');
    
    // PRIORITY 1: Check if we have cached location (fastest path)
    if (hasLocation()) {
      const loc = getLocation();
      // Check if location is still fresh (less than 30 minutes old for better UX)
      const age = Date.now() - (loc.ts || 0);
      const maxAge = options.maximumAge || (30 * 60 * 1000); // Default 30 minutes
      
      if (age < maxAge) {
        console.log('✅ Using cached location (age:', Math.round(age / 1000), 'seconds)');
        console.log('   Location:', loc.lat, loc.lng);
        if (onSuccess) onSuccess({ latitude: loc.lat, longitude: loc.lng });
        return;
      } else {
        console.log('⚠️ Cached location is too old (age:', Math.round(age / 1000), 'seconds), will refresh');
      }
    }
    
    // PRIORITY 2: Check browser permission state (if API available)
    if (navigator.permissions && navigator.permissions.query) {
      navigator.permissions.query({ name: 'geolocation' }).then(function(result) {
        console.log('🔐 Browser permission state:', result.state);
        
        if (result.state === 'granted') {
          // Browser has granted permission - automatically get location
          console.log('✅ Browser permission is granted, getting location automatically');
          getLocationFromBrowser(onSuccess, onError, options, true);
          return;
        } else if (result.state === 'denied') {
          // Browser has denied permission
          console.log('❌ Browser permission is denied');
          if (hasLocation()) {
            // Use cached location if available
            const loc = getLocation();
            console.log('⚠️ Using cached location despite denied permission');
            if (onSuccess) onSuccess({ latitude: loc.lat, longitude: loc.lng });
          } else {
            setPermissionDenied();
            if (onError) onError('Quyền truy cập vị trí đã bị từ chối. Vui lòng cấp quyền trong cài đặt trình duyệt.');
          }
          return;
        } else {
          // Permission is 'prompt' - check if we have granted permission in our app
          if (localStorage.getItem(permissionKey) === 'granted') {
            console.log('✅ App permission is granted, getting location silently (using cached if available)');
            // Use silent=true to avoid triggering browser prompt
            // Browser will use cached location if available, or get location silently if permission was granted before
            getLocationFromBrowser(onSuccess, onError, options, true);
          } else {
            // No permission yet - request it (will show browser prompt)
            console.log('❓ No permission yet, requesting permission (will show browser prompt)');
            getLocationFromBrowser(onSuccess, onError, options, false);
          }
        }
      }).catch(function(error) {
        console.log('⚠️ Permission query API not supported, falling back to standard flow');
        // Fallback to standard flow if permission API is not supported
        handleLocationRequest(onSuccess, onError, options);
      });
      return; // Don't continue, permission query is async
    }
    
    // PRIORITY 3: Fallback - check if permission was granted in our app
    handleLocationRequest(onSuccess, onError, options);
  }
  
  /**
   * Handle location request based on app permission state
   */
  function handleLocationRequest(onSuccess, onError, options) {
    // Check if permission was previously denied in our app
    if (hasPermissionBeenHandled() && localStorage.getItem(permissionKey) === 'denied') {
      console.log('❌ App permission was previously denied');
      if (hasLocation()) {
        // Use cached location if available
        const loc = getLocation();
        console.log('⚠️ Using cached location despite denied permission');
        if (onSuccess) onSuccess({ latitude: loc.lat, longitude: loc.lng });
      } else {
        if (onError) onError('Quyền truy cập vị trí đã bị từ chối. Vui lòng cấp quyền trong cài đặt trình duyệt.');
      }
      return;
    }
    
    // Check if permission was granted in our app - if so, get location silently
    if (localStorage.getItem(permissionKey) === 'granted' || hasLocation()) {
      console.log('✅ App permission is granted, getting location silently');
      getLocationFromBrowser(onSuccess, onError, options, true);
      return;
    }
    
    // No permission yet - request it (will show browser prompt)
    console.log('❓ No permission yet, requesting permission (will show browser prompt)');
    getLocationFromBrowser(onSuccess, onError, options, false);
  }
  
  /**
   * Get location from browser geolocation API
   * @param {Function} onSuccess - Success callback
   * @param {Function} onError - Error callback
   * @param {Object} options - Geolocation options
   * @param {Boolean} silent - If true, use cached location from browser if available
   */
  function getLocationFromBrowser(onSuccess, onError, options, silent = false) {
    if (!navigator.geolocation) {
      if (onError) onError('Trình duyệt của bạn không hỗ trợ định vị.');
      return;
    }
    
    // Use maximumAge to allow using cached location from browser if available
    // If silent is true, use longer cache time (1 hour) to avoid frequent requests
    const maxAge = silent ? (60 * 60 * 1000) : (options.maximumAge !== undefined ? options.maximumAge : (30 * 60 * 1000));
    
    const geolocationOptions = {
      enableHighAccuracy: options.enableHighAccuracy !== false,
      timeout: options.timeout || 10000,
      maximumAge: maxAge
    };
    
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords;
        const accuracy = position.coords.accuracy || 'N/A';
        
        console.log('✅ Location retrieved from browser:', {
          latitude: latitude,
          longitude: longitude,
          accuracy: accuracy + ' meters',
          timestamp: new Date(position.timestamp || Date.now()).toISOString(),
          silent: silent
        });
        
        // Validate coordinates
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
          console.error('❌ Invalid coordinates:', latitude, longitude);
          if (onError) onError('Tọa độ không hợp lệ. Vui lòng thử lại.');
          return;
        }
        
        // Save location and mark permission as granted
        setLocation(latitude, longitude);
        if (onSuccess) onSuccess({ latitude, longitude, accuracy });
      },
      (error) => {
        console.log('❌ Error getting location from browser:', error.code, error.message);
        
        // If we have cached location, use it even if fresh request fails
        if (hasLocation()) {
          const loc = getLocation();
          console.log('⚠️ Using cached location as fallback');
          if (onSuccess) onSuccess({ latitude: loc.lat, longitude: loc.lng });
          return;
        }
        
        let errorMessage = 'Không thể lấy vị trí của bạn. ';
        switch(error.code) {
          case error.PERMISSION_DENIED:
            setPermissionDenied();
            errorMessage += 'Bạn đã từ chối quyền truy cập vị trí.';
            break;
          case error.POSITION_UNAVAILABLE:
            errorMessage += 'Vị trí không khả dụng.';
            break;
          case error.TIMEOUT:
            errorMessage += 'Yêu cầu lấy vị trí đã hết thời gian chờ.';
            break;
          default:
            errorMessage += 'Đã xảy ra lỗi không xác định.';
            break;
        }
        if (onError) onError(errorMessage);
      },
      geolocationOptions
    );
  }
  
  function hasPermissionBeenHandled() {
    return localStorage.getItem(permissionKey) === 'granted' || localStorage.getItem(permissionKey) === 'denied';
  }
  
  // Export functions for global use
  window.LocationUtils = {
    getUserLocation,
    getLocation,
    hasLocation,
    setLocation,
    clearLocation,
    hasPermissionBeenHandled
  };

  function setPermissionDenied() {
    localStorage.setItem(permissionKey, 'denied');
  }

  function stopWatch() {
    if (geoWatchId != null && navigator.geolocation && navigator.geolocation.clearWatch) {
      try { navigator.geolocation.clearWatch(geoWatchId); } catch {}
    }
    geoWatchId = null;
  }

  function distanceMeters(lat1, lon1, lat2, lon2) {
    const R = 6371008.8; // meters
    const toRad = (d) => d * Math.PI / 180;
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a = Math.sin(dLat/2)**2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon/2)**2;
    const c = 2 * Math.asin(Math.sqrt(a));
    return R * c;
  }

  function startWatch() {
    if (!navigator.geolocation || geoWatchId != null) return;
    geoWatchId = navigator.geolocation.watchPosition(async (pos) => {
      const now = Date.now();
      const { latitude, longitude } = pos.coords;
      const moved = (lastUpdate.lat == null) ? Infinity : distanceMeters(lastUpdate.lat, lastUpdate.lng, latitude, longitude);
      const tooSoon = (now - lastUpdate.ts) < MIN_UPDATE_MS;
      if (tooSoon && moved < MIN_MOVE_M) return;
      lastUpdate = { ts: now, lat: latitude, lng: longitude };
      setLocation(latitude, longitude);
      const items = await fetchNearby(latitude, longitude);
      renderNearby(items);
    }, () => {}, { enableHighAccuracy: false, timeout: 5000, maximumAge: 300000 });

    // Clean up on page unload
    window.addEventListener('beforeunload', stopWatch, { once: true });
  }

  function renderNearby(restaurants) {
    const section = document.querySelector('.nearby-section');
    const grid = document.getElementById('nearbyGrid');
    const summary = document.getElementById('nearbySummary');
    if (!section || !grid) return;
    grid.innerHTML = '';
    if (!restaurants || restaurants.length === 0) {
      section.style.display = 'none';
      return;
    }
    section.style.display = '';
    summary && (summary.textContent = `${restaurants.length} gợi ý trong bán kính 3km`);
    restaurants.forEach(r => {
      const card = document.createElement('div');
      card.className = 'nearby-card';
      card.innerHTML = `
        <h4>${escapeHtml(r.name || '')}</h4>
        <div class="nearby-meta">
          <i class="fas fa-location-dot"></i>
          <span>${escapeHtml(r.address || '—')}</span>
        </div>
        <div class="nearby-meta">
          <i class="fas fa-route"></i>
          <span>${r.distanceKm != null ? r.distanceKm.toFixed(1) + ' km' : ''}</span>
        </div>
        <div class="nearby-actions">
          <a class="btn btn-sm btn-primary" href="/public/restaurant-detail?rid=${r.restaurantId}">Xem chi tiết</a>
        </div>`;
      grid.appendChild(card);
    });
  }

  function escapeHtml(str) {
    return String(str).replace(/[&<>"]+/g, s => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[s]));
  }

  async function fetchNearby(lat, lng) {
    try {
      const url = `/api/booking/restaurants/nearby?lat=${encodeURIComponent(lat)}&lng=${encodeURIComponent(lng)}&radius=3000&limit=12`;
      const res = await fetch(url, { credentials: 'same-origin' });
      if (!res.ok) return [];
      return await res.json();
    } catch { return []; }
  }

  function setupButtons() {
    const allow = document.getElementById('btnAllowLocation');
    const deny = document.getElementById('btnDenyLocation');
    const picker = document.getElementById('locationPicker');
    const manual = document.getElementById('manualCity');
    const applyManual = document.getElementById('btnApplyManualCity');
    const disable = document.getElementById('btnDisableLocation');

    allow && allow.addEventListener('click', () => {
      if (!navigator.geolocation) {
        // fallback to manual picker
        picker && (picker.style.display = 'block');
        return;
      }
      navigator.geolocation.getCurrentPosition(async (pos) => {
        const { latitude, longitude } = pos.coords;
        setLocation(latitude, longitude);
        // Mark that prompt has been handled in this session
        sessionStorage.setItem(promptKey, '1');
        hideModal();
        const items = await fetchNearby(latitude, longitude);
        renderNearby(items);
        startWatch();
      }, () => {
        // denied -> show manual picker
        picker && (picker.style.display = 'block');
      }, { enableHighAccuracy: false, timeout: 5000, maximumAge: 300000 });
    });

    deny && deny.addEventListener('click', () => {
      // Mark that prompt has been handled in this session (so won't ask again in same session)
      sessionStorage.setItem(promptKey, '1');
      // Don't set permission denied in localStorage - we want to ask again on next login
      picker && (picker.style.display = 'block');
    });

    applyManual && applyManual.addEventListener('click', async () => {
      if (!manual || !manual.value) return;
      const [latStr, lngStr] = manual.value.split(',');
      const lat = parseFloat(latStr), lng = parseFloat(lngStr);
      if (Number.isFinite(lat) && Number.isFinite(lng)) {
        setLocation(lat, lng);
        // Mark that prompt has been handled in this session
        sessionStorage.setItem(promptKey, '1');
        hideModal();
        const items = await fetchNearby(lat, lng);
        renderNearby(items);
        // Manual city: do not start geolocation watch by default
      }
    });

    disable && disable.addEventListener('click', () => {
      clearLocation();
      stopWatch();
      const section = document.querySelector('.nearby-section');
      const grid = document.getElementById('nearbyGrid');
      section && (section.style.display = 'none');
      grid && (grid.innerHTML = '');
    });
  }

  async function initLocationFeature() {
    console.log('🔍 initLocationFeature() called');
    
    // Only setup buttons if modal exists on this page
    const modalExists = document.getElementById('locationPermissionModal');
    console.log('📍 Modal exists:', !!modalExists);
    
    if (modalExists) {
      setupButtons();
    }

    // Clear location if not authenticated (logout case)
    // Check both APP_CTX and a global flag for authentication status
    const isAuthenticated = (window.APP_CTX && window.APP_CTX.authenticated) || 
                           (document.body && document.body.getAttribute('data-authenticated') === 'true');
    console.log('🔐 isAuthenticated:', isAuthenticated);
    console.log('📋 APP_CTX:', window.APP_CTX);
    
    if (!isAuthenticated) {
      console.log('❌ User not authenticated, clearing location');
      clearLocation();
      sessionStorage.removeItem(promptKey);
      // Also clear any location-related data
      localStorage.removeItem(storageKey);
      return; // Don't proceed if not authenticated
    }

    // Skip entirely for restaurant owners
    if (window.APP_CTX && window.APP_CTX.isRestaurantOwner) {
      console.log('🏢 Restaurant owner detected, skipping location feature');
      clearLocation();
      return;
    }

    // Location permission feature enabled
    
    // PRIORITY 1: Check if already asked in THIS SESSION (sessionStorage)
    // This is the key check - if already asked in this session, don't ask again
    // But on new login (new session), sessionStorage will be empty, so modal will show
    const alreadyAsked = sessionStorage.getItem(promptKey);
    console.log('❓ Already asked in this session:', !!alreadyAsked);
    
    if (alreadyAsked) {
      console.log('✅ Already asked in this session, trying to get location silently');
      // Already asked in this session, don't ask again
      // But try to get location silently if permission was granted before
      if (navigator.geolocation) {
        try {
          if (navigator.permissions && navigator.permissions.query) {
            const status = await navigator.permissions.query({ name: 'geolocation' });
            if (status && status.state === 'granted') {
              navigator.geolocation.getCurrentPosition(async (pos) => {
                const { latitude, longitude } = pos.coords;
                setLocation(latitude, longitude);
                const items = await fetchNearby(latitude, longitude);
                renderNearby(items);
                startWatch();
              }, () => { /* ignore failure */ }, { enableHighAccuracy: false, timeout: 5000, maximumAge: 300000 });
              return; // Don't show modal
            }
          }
        } catch { /* noop */ }
      }
      // If we have location in sessionStorage, use it
      const sessionLoc = sessionStorage.getItem(storageKey);
      if (sessionLoc) {
        try {
          const loc = JSON.parse(sessionLoc);
          const age = Date.now() - (loc.ts || 0);
          if (age < 10 * 60 * 1000) { // 10 minutes
            const items = await fetchNearby(loc.lat, loc.lng);
            renderNearby(items);
            startWatch();
            return; // Don't show modal
          }
        } catch { /* noop */ }
      }
      return; // Don't show modal if already asked in this session
    }

    // PRIORITY 2: Show modal if this is first time in this login session
    // This is the main logic - show modal EVERY TIME user logs in (new session)
    // Only show modal if modal element exists on this page
    const modalElement = document.getElementById('locationPermissionModal');
    const showLocationPrompt = !!(window.APP_CTX && window.APP_CTX.showLocationPrompt);
    
    console.log('🔍 Modal check:');
    console.log('  - modalElement exists:', !!modalElement);
    console.log('  - isAuthenticated:', isAuthenticated);
    console.log('  - showLocationPrompt:', showLocationPrompt);
    console.log('  - APP_CTX.showLocationPrompt value:', window.APP_CTX?.showLocationPrompt);
    console.log('  - sessionStorage.getItem(promptKey):', sessionStorage.getItem(promptKey));
    
    // Show modal if:
    // 1. Modal exists on page
    // 2. User is authenticated
    // 3. Server indicates we should prompt (SHOW_LOCATION_PROMPT is set on login)
    // 4. NOT already shown in this session (sessionStorage check - this is the key!)
    // IMPORTANT: We ask EVERY TIME on new login because sessionStorage is cleared on logout
    const shouldShowModal = modalElement && 
                           isAuthenticated &&
                           showLocationPrompt && 
                           !alreadyAsked;
    
    console.log('🎯 shouldShowModal:', shouldShowModal);
    
    if (shouldShowModal) {
      console.log('✅ Showing location permission modal - new login session detected');
      showModal();
      return; // Stop here after showing modal
    } else {
      console.log('❌ Modal NOT showing. Reasons:');
      if (!modalElement) console.log('  - Modal element not found');
      if (!isAuthenticated) console.log('  - User not authenticated');
      if (!showLocationPrompt) console.log('  - showLocationPrompt is false');
      if (alreadyAsked) console.log('  - Already asked in this session');
    }

    // PRIORITY 5: Fallback - if we reach here and still authenticated, don't show modal
    // (This should rarely happen, but prevents unwanted modals)
  }

  // Only initialize location feature if modal exists or if explicitly requested
  // This allows LocationUtils to be available on all pages without running modal logic
  function initLocationFeatureIfNeeded() {
    console.log('🔧 initLocationFeatureIfNeeded() called');
    console.log('📄 document.readyState:', document.readyState);
    
    const modalExists = document.getElementById('locationPermissionModal');
    const nearbySectionExists = document.querySelector('.nearby-section');
    
    console.log('🔍 Elements check:');
    console.log('  - modalExists:', !!modalExists);
    console.log('  - nearbySectionExists:', !!nearbySectionExists);
    
    // Only run full initialization if modal or nearby section exists (home page)
    if (modalExists || nearbySectionExists) {
      console.log('✅ Running initLocationFeature()');
      // Wait a bit to ensure APP_CTX is set (it's set in inline script before this file)
      setTimeout(() => {
        initLocationFeature();
      }, 100);
    } else {
      console.log('⏭️ Skipping initLocationFeature() - modal/nearby section not found');
      console.log('✅ LocationUtils is still available for use (getUserLocation, etc.)');
      // Just ensure LocationUtils is available, but don't run modal logic
      // This allows other pages to use LocationUtils.getUserLocation() etc.
    }
  }

  // Wait for both DOM and APP_CTX to be ready
  function waitForAppContext() {
    if (window.APP_CTX !== undefined) {
      console.log('✅ APP_CTX is ready');
      initLocationFeatureIfNeeded();
    } else {
      console.log('⏳ Waiting for APP_CTX...');
      setTimeout(waitForAppContext, 50);
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      console.log('📄 DOMContentLoaded fired');
      waitForAppContext();
    });
  } else {
    console.log('📄 DOM already ready');
    waitForAppContext();
  }
})();
