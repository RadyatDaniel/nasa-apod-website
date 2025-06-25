document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const fetchBtn = document.getElementById('fetch-btn');
    const dateSelector = document.getElementById('date-selector');
    const loadingElement = document.getElementById('loading');
    const apodContainer = document.getElementById('apod-container');
    const errorElement = document.getElementById('error-message');
    const hdButton = document.createElement('button');
    hdButton.id = 'hd-btn';
    hdButton.textContent = 'View HD';
    hdButton.classList.add('hd-button', 'hidden');
    document.querySelector('.controls').appendChild(hdButton);

    // NASA API Key - DEMO_KEY has rate limits, consider getting your own
    const NASA_API_KEY = 'JrkWpXt53vb33k0tQXfQTSCOpvEAEbyuTWeFzE6R';

    // Set date constraints (today as max/default)
    const today = new Date().toISOString().split('T')[0];
    dateSelector.max = today;
    dateSelector.value = today;

    // Initial fetch
    fetchAPOD(today);

    // Event Listeners
    fetchBtn.addEventListener('click', () => {
        const selectedDate = dateSelector.value;
        console.log('Fetching APOD for:', selectedDate);
        fetchAPOD(selectedDate);
    });

    hdButton.addEventListener('click', toggleHD);

    // Main Functions
    async function fetchAPOD(date) {
        showLoading();
        hdButton.classList.add('hidden');
        
        if (!isValidDate(date)) {
            showError("Invalid date format. Please use YYYY-MM-DD");
            return;
        }

        try {
            const response = await fetch(`https://api.nasa.gov/planetary/apod?api_key=${NASA_API_KEY}&date=${date}`);
            
            if (!response.ok) {
                const error = await tryParseError(response);
                throw new Error(error || `NASA API request failed (${response.status})`);
            }

            const data = await response.json();
            
            if (!data?.url) {
                throw new Error("Invalid data received from NASA API");
            }

            displayAPOD(data);
        } catch (error) {
            console.error('Fetch error:', error);
            showError(error.message || "Failed to load APOD data from NASA");
        }
    }

    function displayAPOD(data) {
        try {
            // Update text content
            document.getElementById('apod-title').textContent = data.title || "Untitled";
            document.getElementById('apod-explanation').textContent = data.explanation || "No explanation available.";
            document.getElementById('apod-date').textContent = data.date || "Date unknown";

            // Handle copyright
            const copyrightElement = document.getElementById('apod-copyright');
            if (data.copyright) {
                copyrightElement.textContent = `© ${data.copyright}`;
                copyrightElement.classList.remove('hidden');
            } else {
                copyrightElement.classList.add('hidden');
            }

            // Handle media
            const mediaContainer = document.getElementById('media-container');
            mediaContainer.innerHTML = '';

            if (data.media_type === 'image') {
                createImageMedia(data, mediaContainer);
                if (data.hdurl) {
                    hdButton.dataset.hdUrl = data.hdurl;
                    hdButton.classList.remove('hidden');
                }
            } else if (data.media_type === 'video') {
                createVideoMedia(data, mediaContainer);
            } else {
                mediaContainer.innerHTML = '<p>Unsupported media type</p>';
            }

            // Show content
            hideLoading();
            apodContainer.classList.remove('hidden');
            errorElement.classList.add('hidden');

        } catch (error) {
            console.error('Display error:', error);
            showError("Error displaying content");
        }
    }

    // Helper Functions
    function createImageMedia(data, container) {
        const img = document.createElement('img');
        img.id = 'apod-image';
        img.src = data.url;
        img.alt = data.title || "NASA Astronomy Picture";
        img.loading = 'lazy';
        img.onerror = () => mediaErrorHandler(container, "Failed to load image");
        container.appendChild(img);
    }

    function createVideoMedia(data, container) {
        const iframe = document.createElement('iframe');
        iframe.src = data.url;
        iframe.allowFullscreen = true;
        iframe.title = data.title || "NASA Astronomy Video";
        iframe.onerror = () => mediaErrorHandler(container, "Failed to load video");
        container.appendChild(iframe);
    }

    function toggleHD() {
        const img = document.getElementById('apod-image');
        if (img) {
            if (img.src === this.dataset.hdUrl) {
                img.src = img.dataset.standardUrl || '';
                this.textContent = 'View HD';
            } else {
                img.dataset.standardUrl = img.src;
                img.src = this.dataset.hdUrl;
                this.textContent = 'View Standard';
            }
        }
    }

    function mediaErrorHandler(container, message) {
        container.innerHTML = `<p class="media-error">${message}</p>`;
        showError(message);
    }

    async function tryParseError(response) {
        try {
            const errorData = await response.json();
            return errorData.error || response.statusText;
        } catch {
            return await response.text();
        }
    }

    function isValidDate(dateString) {
        return /^\d{4}-\d{2}-\d{2}$/.test(dateString);
    }

    function showLoading() {
        loadingElement.classList.remove('hidden');
        apodContainer.classList.add('hidden');
        errorElement.classList.add('hidden');
    }

    function hideLoading() {
        loadingElement.classList.add('hidden');
    }

    function showError(message) {
        errorElement.textContent = message;
        errorElement.classList.remove('hidden');
        loadingElement.classList.add('hidden');
        apodContainer.classList.add('hidden');
    }
});