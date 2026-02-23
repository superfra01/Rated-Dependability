/**
 * Conferma un'azione da parte del moderatore prima di inviare il form.
 * @param {string} messaggio - Il messaggio di conferma da mostrare.
 * @param {HTMLFormElement} form - Il form da inviare se confermato.
 */
function confermaAzione(messaggio, form) {
    if (confirm(messaggio)) {
        form.submit();
    }
}
