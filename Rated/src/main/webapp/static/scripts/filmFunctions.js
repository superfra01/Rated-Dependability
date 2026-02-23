function voteReview(idFilm, emailRecensore, valutazione) {
    const formData = new URLSearchParams();
    formData.append("idFilm", idFilm);
    formData.append("emailRecensore", emailRecensore);
    formData.append("valutazione", valutazione);

    fetch("VoteReview", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: formData.toString()
    })
    .then(response => {
        if (response.ok) {
            window.location.reload();
        } else {
            alert("Errore durante la votazione. Riprova più tardi.");
        }
    })
    .catch(error => {
        console.error("Errore nella richiesta:", error);
        alert("Errore durante la votazione.");
    });
}

// --- FUNZIONE GESTIONE LISTE CORRETTA ---
function toggleUserList(idFilm, listType, buttonElement) {
    let urlServlet = "";
    
    // Selezione Servlet corretta
    if (listType === 'watchlist') {
        urlServlet = "AggiungiWatchlistServlet";
    } else if (listType === 'watched') {
        urlServlet = "SegnaComeVistoServlet";
    }

    if (!urlServlet) {
        console.error("Tipo di lista non valido");
        return;
    }

    const formData = new URLSearchParams();
    // Nota: La servlet si aspetta "filmId" come parametro
    formData.append("filmId", idFilm); 

    fetch(urlServlet, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: formData.toString()
    })
    .then(async response => {
        if (response.ok) {
            // SUCCESS: Aggiorna la UI
            buttonElement.classList.toggle("active");
            const isActive = buttonElement.classList.contains("active");

            if(listType === 'watchlist') {
                if (isActive) {
                    buttonElement.innerHTML = '<i class="fas fa-minus-circle"></i> Rimuovi dalla Watchlist';
                } else {
                    buttonElement.innerHTML = '<i class="fas fa-bookmark"></i> Aggiungi alla Watchlist';
                }
            } else if (listType === 'watched') {
                if (isActive) {
                    buttonElement.innerHTML = '<i class="fas fa-times-circle"></i> Rimuovi da Visti';
                } else {
                    buttonElement.innerHTML = '<i class="fas fa-check-circle"></i> Segna come Visto';
                }
            }
        } else {
            // ERROR: Leggi il messaggio inviato dalla Servlet
            const errorMessage = await response.text();
            console.error("Errore server:", errorMessage);
            alert(errorMessage || "Impossibile aggiornare la lista.");
        }
    })
    .catch(error => {
        console.error("Errore chiamata liste:", error);
        alert("Errore di connessione al server.");
    });
}

function showReviewForm() {
    const overlay = document.getElementById('reviewOverlay');
    if(overlay) overlay.style.display = 'flex';
    
    const rateButton = document.getElementById('btnRateFilm');
    if (rateButton) rateButton.disabled = true;
}

function hideReviewForm() {
    const overlay = document.getElementById('reviewOverlay');
    if(overlay) overlay.style.display = 'none';
    
    const rateButton = document.getElementById('btnRateFilm');
    if (rateButton) rateButton.disabled = false;
}

function showModifyForm() {
    document.getElementById('modifyOverlay').style.display = 'flex';
}

function hideModifyForm() {
    document.getElementById('modifyOverlay').style.display = 'none';
}

function validateReviewForm() {
    const titolo = document.getElementById('titolo').value.trim();
    const recensione = document.getElementById('recensione').value.trim();
    const valutazione = document.querySelector('input[name="valutazione"]:checked');

    if (titolo === "" || recensione === "" || !valutazione) {
        alert("Per favore, completa tutti i campi.");
        return false;
    }
    return true;
}

function deleteFilm(idFilm) {
    if (confirm("Sei sicuro di voler eliminare questo film? Questa azione non può essere annullata.")) {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = 'deleteFilm';
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'idFilm';
        input.value = idFilm;
        form.appendChild(input);
        document.body.appendChild(form);
        form.submit();
    }
}

function reportReview(idFilm, emailRecensore) {
    if (confirm("Sei sicuro di voler segnalare questa recensione?")) {
        const formData = new URLSearchParams();
        formData.append("idFilm", idFilm);
        formData.append("reviewerEmail", emailRecensore);

        fetch("ReportReview", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData.toString()
        })
        .then(response => {
            if (response.ok) alert("Recensione segnalata con successo.");
            else alert("Errore durante la segnalazione.");
        })
        .catch(error => console.error(error));
    }
}