/**
 * SmartPark — Dashboard (Map, Geolocation, Parking Areas)
 */
document.addEventListener('DOMContentLoaded', () => {
    SmartPark.updateNavbar();

    let allLots = [];
    let map = null;
    let markers = [];
    let userCity = '';

    // ---- Geolocation ----
    detectLocation();

    async function detectLocation() {
        const locationText = document.getElementById('locationText');
        const cityDisplay = document.getElementById('cityDisplay');

        if ('geolocation' in navigator) {
            try {
                const position = await new Promise((resolve, reject) => {
                    navigator.geolocation.getCurrentPosition(resolve, reject, {
                        timeout: 10000,
                        enableHighAccuracy: false
                    });
                });

                const { latitude, longitude } = position.coords;

                // Reverse geocode to get city name
                const city = await reverseGeocode(latitude, longitude);
                userCity = city;
                cityDisplay.textContent = city;
                locationText.textContent = `Showing parking areas in ${city}`;

                // Set city filter
                const cityFilter = document.getElementById('cityFilter');
                for (let i = 0; i < cityFilter.options.length; i++) {
                    if (cityFilter.options[i].value.toLowerCase() === city.toLowerCase()) {
                        cityFilter.selectedIndex = i;
                        break;
                    }
                }

                initMap(latitude, longitude);
                loadParkingLots(city);

            } catch (error) {
                console.log('Geolocation error:', error);
                cityDisplay.textContent = 'All Cities';
                locationText.textContent = 'Location access denied. Showing all parking areas.';
                initMap(20.5937, 78.9629, 5); // India center
                loadParkingLots('');
            }
        } else {
            cityDisplay.textContent = 'All Cities';
            locationText.textContent = 'Geolocation not supported. Showing all parking areas.';
            initMap(20.5937, 78.9629, 5);
            loadParkingLots('');
        }
    }

    async function reverseGeocode(lat, lng) {
        // Use a free reverse geocoding service
        try {
            const response = await fetch(
                `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=10`
            );
            const data = await response.json();
            const city = data.address?.city || data.address?.town || data.address?.state_district || data.address?.state || 'Unknown';

            // Map to our known cities
            const knownCities = ['Mumbai', 'Delhi', 'Bangalore', 'Pune', 'Hyderabad'];
            const match = knownCities.find(c => city.toLowerCase().includes(c.toLowerCase()) ||
                                                  c.toLowerCase().includes(city.toLowerCase()));
            return match || city;
        } catch (e) {
            return 'Mumbai'; // fallback
        }
    }

    // ---- Map (using Leaflet.js via CDN for free maps) ----
    function initMap(lat, lng, zoom = 12) {
        const mapDiv = document.getElementById('map');
        mapDiv.innerHTML = '';

        // Load Leaflet CSS and JS dynamically
        if (!document.querySelector('link[href*="leaflet"]')) {
            const link = document.createElement('link');
            link.rel = 'stylesheet';
            link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
            document.head.appendChild(link);
        }

        loadScript('https://unpkg.com/leaflet@1.9.4/dist/leaflet.js', () => {
            map = L.map('map').setView([lat, lng], zoom);

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; OpenStreetMap contributors',
                maxZoom: 19
            }).addTo(map);

            // User location marker
            const userIcon = L.divIcon({
                html: '<div style="width:16px;height:16px;background:#3b82f6;border:3px solid white;border-radius:50%;box-shadow:0 0 10px rgba(59,130,246,0.5);"></div>',
                iconSize: [16, 16],
                className: ''
            });

            L.marker([lat, lng], { icon: userIcon })
                .addTo(map)
                .bindPopup('📍 You are here');
        });
    }

    function loadScript(src, callback) {
        if (document.querySelector(`script[src="${src}"]`)) {
            // Already loaded, just wait a bit
            setTimeout(callback, 100);
            return;
        }
        const script = document.createElement('script');
        script.src = src;
        script.onload = callback;
        document.head.appendChild(script);
    }

    function addMapMarkers(lots) {
        if (!map) return;

        // Clear existing markers
        markers.forEach(m => map.removeLayer(m));
        markers = [];

        lots.forEach(lot => {
            const parkingIcon = L.divIcon({
                html: `<div style="width:32px;height:32px;background:linear-gradient(135deg,#10b981,#06b6d4);border:2px solid white;border-radius:8px;display:flex;align-items:center;justify-content:center;font-size:14px;box-shadow:0 2px 8px rgba(0,0,0,0.3);">🅿</div>`,
                iconSize: [32, 32],
                className: ''
            });

            const marker = L.marker([lot.latitude, lot.longitude], { icon: parkingIcon })
                .addTo(map)
                .bindPopup(`
                    <div style="font-family:Inter,sans-serif;min-width:200px;">
                        <strong>${lot.name}</strong><br>
                        <small style="color:#666;">${lot.address}</small><br>
                        <hr style="margin:6px 0;border-color:#eee;">
                        <span style="color:#10b981;font-weight:600;">₹${lot.ratePerHour}/hr</span> · 
                        <span>${lot.availableSlots}/${lot.totalSlots} available</span><br>
                        <a href="slots.html?lotId=${lot.id}" style="color:#3b82f6;font-weight:600;">View Slots →</a>
                    </div>
                `);

            markers.push(marker);
        });

        // Fit map to markers if we have any
        if (markers.length > 0) {
            const group = L.featureGroup(markers);
            map.fitBounds(group.getBounds().pad(0.2));
        }
    }

    // ---- Load Parking Lots ----
    async function loadParkingLots(city) {
        const lotsGrid = document.getElementById('lotsGrid');
        lotsGrid.innerHTML = '<div class="loading-spinner"></div>';

        try {
            const endpoint = city ? `/parking/lots?city=${encodeURIComponent(city)}` : '/parking/lots';
            allLots = await SmartPark.apiFetch(endpoint);

            renderLots(allLots);
            addMapMarkers(allLots);
            updateStats(allLots);
        } catch (error) {
            lotsGrid.innerHTML = `
                <div class="empty-state">
                    <div class="empty-icon">⚠️</div>
                    <h3>Cannot load parking areas</h3>
                    <p>${error.message}</p>
                </div>
            `;
        }
    }

    function renderLots(lots) {
        const lotsGrid = document.getElementById('lotsGrid');

        if (lots.length === 0) {
            lotsGrid.innerHTML = `
                <div class="empty-state">
                    <div class="empty-icon">🅿️</div>
                    <h3>No parking areas found</h3>
                    <p>Try selecting a different city</p>
                </div>
            `;
            return;
        }

        lotsGrid.innerHTML = lots.map(lot => {
            const availPercent = lot.totalSlots > 0 ? (lot.availableSlots / lot.totalSlots) * 100 : 0;
            let availClass = 'high';
            if (availPercent < 30) availClass = 'low';
            else if (availPercent < 60) availClass = 'medium';

            return `
                <div class="card lot-card" onclick="window.location.href='slots.html?lotId=${lot.id}'">
                    <div class="lot-name">${lot.name}</div>
                    <div class="lot-address">📍 ${lot.address}</div>
                    <div class="lot-info">
                        <div class="lot-rate">₹${lot.ratePerHour} <span>/hr</span></div>
                        <div class="lot-availability">
                            <div class="avail-dot ${availClass}"></div>
                            <span>${lot.availableSlots}/${lot.totalSlots} slots</span>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    }

    function updateStats(lots) {
        document.getElementById('statTotalLots').textContent = lots.length;
        const totalAvailable = lots.reduce((sum, l) => sum + l.availableSlots, 0);
        document.getElementById('statAvailableSlots').textContent = totalAvailable;
        const cities = [...new Set(lots.map(l => l.city))];
        document.getElementById('statCities').textContent = cities.length;
    }

    // ---- City Filter ----
    document.getElementById('cityFilter').addEventListener('change', (e) => {
        const city = e.target.value;
        const cityDisplay = document.getElementById('cityDisplay');
        const locationText = document.getElementById('locationText');

        if (city) {
            cityDisplay.textContent = city;
            locationText.textContent = `Showing parking areas in ${city}`;
        } else {
            cityDisplay.textContent = 'All Cities';
            locationText.textContent = 'Showing all parking areas';
        }

        loadParkingLots(city);
    });
});
