document.addEventListener('DOMContentLoaded', () => {
    const starfield = document.getElementById('starfield');
    const starCount = 300; // Increased star count
    const shootingStarInterval = 1000; // More frequent shooting stars
    
    // NASA color variables
    const nasaBlue = 'rgba(11, 61, 145, 0.8)';
    const nasaRed = 'rgba(252, 61, 33, 0.8)';
    const nasaWhite = 'rgba(255, 255, 255, 0.9)';
    const nasaLightBlue = 'rgba(79, 195, 247, 0.8)';

    // Create regular stars (now faster)
    for (let i = 0; i < starCount; i++) {
        createStar();
    }

    // Create nebula effects
    createNebula(nasaLightBlue, 300, 15);
    createNebula(nasaRed, 200, 20);
    createNebula(nasaWhite, 250, 10);

    // Start shooting star interval (more frequent)
    setInterval(createShootingStar, shootingStarInterval);

    // Handle window resize
    window.addEventListener('resize', () => {
        const stars = document.querySelectorAll('.star');
        stars.forEach(star => {
            star.style.left = `${Math.random() * window.innerWidth}px`;
        });
    });

    function createStar() {
        const star = document.createElement('div');
        star.className = 'star';
        
        // Faster stars: reduced duration range
        const size = Math.random() * 2.5; // Smaller stars appear faster
        const posX = Math.random() * window.innerWidth;
        const duration = 10 + Math.random() * 20; // Much faster (10-30s)
        const delay = Math.random() * 5;
        const opacity = 0.7 + Math.random() * 0.3; // Brighter stars
        
        star.style.width = `${size}px`;
        star.style.height = `${size}px`;
        star.style.left = `${posX}px`;
        star.style.animationDuration = `${duration}s`;
        star.style.animationDelay = `-${delay}s`;
        star.style.opacity = opacity;
        
        starfield.appendChild(star);
        
        star.addEventListener('animationiteration', () => {
            star.style.left = `${Math.random() * window.innerWidth}px`;
            star.style.opacity = 0.7 + Math.random() * 0.3;
        });
    }

    function createShootingStar() {
        const shootingStar = document.createElement('div');
        shootingStar.className = 'shooting-star';
        
        // Faster shooting stars
        const posX = Math.random() * window.innerWidth;
        const posY = Math.random() * window.innerHeight;
        const duration = 0.5 + Math.random(); // Much faster (0.5-1.5s)
        const size = 2 + Math.random();
        
        shootingStar.style.left = `${posX}px`;
        shootingStar.style.top = `${posY}px`;
        shootingStar.style.width = `${size}px`;
        shootingStar.style.height = `${size}px`;
        shootingStar.style.animationDuration = `${duration}s`;
        
        // NASA color rotation
        const colors = [nasaWhite, nasaLightBlue, nasaRed];
        const randomColor = colors[Math.floor(Math.random() * colors.length)];
        shootingStar.style.backgroundColor = randomColor;
        
        starfield.appendChild(shootingStar);
        
        setTimeout(() => {
            shootingStar.remove();
        }, duration * 1000);
    }

    function createNebula(color, size, duration) {
        const nebula = document.createElement('div');
        nebula.className = 'nebula';
        
        nebula.style.width = `${size}px`;
        nebula.style.height = `${size}px`;
        nebula.style.background = color;
        nebula.style.left = `${Math.random() * window.innerWidth}px`;
        nebula.style.top = `${Math.random() * window.innerHeight}px`;
        nebula.style.animationDuration = `${duration}s`;
        
        starfield.appendChild(nebula);
    }
});