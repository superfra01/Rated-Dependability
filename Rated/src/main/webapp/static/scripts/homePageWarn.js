document.addEventListener("DOMContentLoaded", () => {
    // Recupera i dati dal server tramite il contesto della pagina
    const loginSuccess = data.loginSuccess;
    const nWarning = data.nWarning;

    // Mostra l'alert solo se loginSuccess è vero
    if (loginSuccess) {
        if (nWarning > 0 && nWarning < 3) {
            alert(
                `Attenzione: Hai ricevuto un warning da un moderatore.\n` +
                `Ciò è avvenuto a causa della rimozione di una recensione inappropriata.\n` +
                `Ti invitiamo a essere rispettoso nelle recensioni ed evitare spoiler.\n\n` +
                `In totale hai ricevuto: ${nWarning} warning.\n` +
                `Ricorda: al raggiungimento di 3 warning, il tuo account sarà limitato e non potrai più scrivere recensioni.`
            );
        } else if (nWarning >= 3) {
            alert(
                `Il tuo account è attualmente limitato a causa di 3 o più warning ricevuti.\n` +
                `Non puoi più scrivere recensioni.`
            );
        }

        // Rimuove il parametro "loginSuccess" dalla URL per evitare alert multipli al refresh
        window.history.replaceState({}, document.title, window.location.pathname);
    }
});
