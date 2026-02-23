/**
 * Gestisce il cambio tra la lista dei film visti e la watchlist.
 * @param {string} type - 'watched' oppure 'watchlist'
 */
function switchList(type) {
    // Crea un oggetto URL basato sull'indirizzo corrente
    const currentUrl = new URL(window.location.href);
    
    // Aggiorna o aggiunge il parametro 'listType' mantenendo 'username'
    currentUrl.searchParams.set("listType", type);
    
    // Reindirizza al nuovo URL
    window.location.href = currentUrl.toString();
}