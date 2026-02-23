/**
 * Gestisce lo scroll orizzontale del carosello film.
 * @param {number} direction - 1 per destra, -1 per sinistra
 */
function scrollCarousel(direction) {
    const track = document.getElementById('recTrack');
    const scrollAmount = 300; // Pixel di scorrimento per click
    
    if (track) {
        track.scrollBy({
            left: scrollAmount * direction,
            behavior: 'smooth'
        });
    }
}

/**
 * Invia una richiesta alla Servlet per ignorare un film e rimuoverlo dalla UI.
 * @param {number} filmId - L'ID del film da rimuovere
 */
function rimuoviConsiglio(filmId) {
    // URL della Servlet (relativo alla root del contesto web)
    const url = 'NonInteressatoServlet';
    
    // Preparo i dati form-urlencoded (come si aspetta request.getParameter)
    const params = new URLSearchParams();
    params.append('filmId', filmId);

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        body: params
    })
    .then(response => {
        if (response.ok) {
            // Se il server risponde 200 OK, rimuoviamo la card visivamente
            animazioneRimozione(filmId);
        } else {
            // Gestione errori (es. sessione scaduta)
            return response.text().then(text => {
                console.error("Errore server:", text);
                alert("Impossibile rimuovere il film al momento.");
            });
        }
    })
    .catch(error => {
        console.error('Errore di rete:', error);
    });
}

/**
 * Rimuove l'elemento HTML con un effetto di fade-out.
 * @param {number} filmId 
 */
function animazioneRimozione(filmId) {
    const card = document.getElementById('film-card-' + filmId);
    
    if (card) {
        // Applica stile per fade-out
        card.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
        card.style.opacity = '0';
        card.style.transform = 'scale(0.8)';
        
        // Aspetta la fine della transizione prima di rimuovere dal DOM
        setTimeout(() => {
            card.remove();
            
            // Controllo opzionale: se non ci sono più film, nascondi l'intera sezione
            const track = document.getElementById('recTrack');
            if (track && track.children.length === 0) {
                const container = document.querySelector('.recommendations-container');
                if (container) container.style.display = 'none';
            }
        }, 400);
    }
}