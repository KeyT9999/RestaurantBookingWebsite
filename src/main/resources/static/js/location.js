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
  }

  function clearLocation() {
    sessionStorage.removeItem(storageKey);
    localStorage.removeItem(permissionKey);
  }

  function hasLocation() {
    return !!sessionStorage.getItem(storageKey);
  }

  function getLocation() {
    try { return JSON.parse(sessionStorage.getItem(storageKey) || '{}'); } catch { return {}; }
  }

  function hasPermissionBeenHandled() {
    return localStorage.getItem(permissionKey) === 'granted' || localStorage.getItem(permissionKey) === 'denied';
  }

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
      setPermissionDenied();
      picker && (picker.style.display = 'block');
    });

    applyManual && applyManual.addEventListener('click', async () => {
      if (!manual || !manual.value) return;
      const [latStr, lngStr] = manual.value.split(',');
      const lat = parseFloat(latStr), lng = parseFloat(lngStr);
      if (Number.isFinite(lat) && Number.isFinite(lng)) {
        setLocation(lat, lng);
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
    setupButtons();

    // Clear location if not authenticated (logout case)
    if (!APP_CTX.authenticated) {
      clearLocation();
      sessionStorage.removeItem(promptKey);
    }

    // Skip entirely for restaurant owners
    if (APP_CTX.isRestaurantOwner) {
      clearLocation();
      return;
    }

    // Location permission feature enabled
    
    // 1) Check if user has already handled location permission
    if (APP_CTX.authenticated && hasPermissionBeenHandled()) {
      // User already granted or denied permission, don't show modal again
      if (hasLocation()) {
        const loc = getLocation();
        const items = await fetchNearby(loc.lat, loc.lng);
        renderNearby(items);
        startWatch();
      }
      return;
    }

    // 2) Immediately after login: show modal only if not handled before
    const firstAfterLogin = !!APP_CTX.showLocationPrompt && !sessionStorage.getItem(promptKey);
    if (APP_CTX.authenticated && firstAfterLogin) {
      showModal();
      return; // stop here to avoid auto-fetch even if permission is already granted
    }

    // If we already have a location, render nearby immediately
    if (hasLocation()) {
      const loc = getLocation();
      const items = await fetchNearby(loc.lat, loc.lng);
      renderNearby(items);
      startWatch();
      return;
    }

    // If permission is already granted, auto-fetch location on load
    if (APP_CTX.authenticated && navigator.geolocation) {
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
            }, () => { /* ignore failure; fallback to prompt logic */ }, { enableHighAccuracy: false, timeout: 5000, maximumAge: 300000 });
            return; // stop further prompt logic; we're auto-loading
          }
        }
      } catch { /* noop */ }
    }

    // Otherwise, if server still indicates prompting is desired (fallback)
    const shouldPrompt = !!APP_CTX.showLocationPrompt && !sessionStorage.getItem(promptKey) && !hasPermissionBeenHandled();
    if (APP_CTX.authenticated && shouldPrompt) showModal();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initLocationFeature);
  } else {
    initLocationFeature();
  }
})();
