<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="model.Entity.FilmBean" %>
<%@ page import="model.Entity.FilmGenereBean" %>
<%@ page import="model.Entity.RecensioneBean" %>
<%@ page import="model.Entity.UtenteBean" %>
<%@ page import="model.Entity.ValutazioneBean" %>
<%@ page import="java.util.Base64" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Film</title>
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/static/images/favicon.ico">
    <link rel="stylesheet" href="static/css/Film.css" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" />
    <script src="static/scripts/filmFunctions.js" defer></script>
</head>
<body>

<jsp:include page="header.jsp" />

<%
    // --- RECUPERO DATI DALLA SESSIONE ---
    FilmBean film = (FilmBean) session.getAttribute("film");
    List<RecensioneBean> recensioni = (List<RecensioneBean>) session.getAttribute("recensioni");
    UtenteBean user = (UtenteBean) session.getAttribute("user");
    HashMap<String, ValutazioneBean> valutazioni = (HashMap<String, ValutazioneBean>) session.getAttribute("valutazioni");

    // La servlet "VisualizzaFilmServlet" ha già calcolato se il film è nelle liste
    Boolean watchedSession = (Boolean) session.getAttribute("watched");
    Boolean watchlistSession = (Boolean) session.getAttribute("inwatchlist");

    // Gestione null safety (default false se l'attributo non esiste)
    boolean isInWatched = (watchedSession != null) ? watchedSession : false;
    boolean isInWatchlist = (watchlistSession != null) ? watchlistSession : false;

    // --- GESTIONE GENERI ---
    List<FilmGenereBean> listaGeneri = (List<FilmGenereBean>) session.getAttribute("Generi");
    
    String displayGeneri = "Nessun genere";
    List<String> nomiGeneriAttuali = new ArrayList<>();

    if (listaGeneri != null && !listaGeneri.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        for (FilmGenereBean g : listaGeneri) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(g.getNomeGenere());
            nomiGeneriAttuali.add(g.getNomeGenere());
        }
        displayGeneri = sb.toString();
    }

    // --- VERIFICA PRELIMINARE SE L'UTENTE HA RECENSITO ---
    // Serve per bloccare il bottone "Watched" (se hai recensito, l'hai visto per forza)
    boolean userHasReviewed = false;
    if (user != null && "RECENSORE".equals(user.getTipoUtente()) && recensioni != null) {
        for (RecensioneBean r : recensioni) {
            if (r.getEmail().equals(user.getEmail())) {
                userHasReviewed = true;
                break;
            }
        }
    }
%>

<div class="page-container">
    <div class="content-section">
        
        <div class="left-column">
            <div class="sort-bar">
                </div>

            <% if (recensioni != null && !recensioni.isEmpty()) {
                HashMap<String, String> users = (HashMap<String, String>) session.getAttribute("users");

                for (RecensioneBean r : recensioni) {
                    String emailRecensore = r.getEmail();
                    String titoloRecensione = r.getTitolo();
                    String testoRecensione = r.getContenuto();
                    int stelle = r.getValutazione();
                    ValutazioneBean val = (valutazioni != null) ? valutazioni.get(emailRecensore) : null;

                    String usernameRecensore = (users != null && users.containsKey(emailRecensore))
                        ? users.get(emailRecensore)
                        : emailRecensore;
            %>

            <div class="review-card">
                <div class="review-username">
                    <a href="<%= request.getContextPath() %>/profile?visitedUser=<%= usernameRecensore %>" class="profile-link">
                        <%= usernameRecensore %>
                    </a>
                </div>
                <div class="review-title">
                    <%= titoloRecensione %>
                </div>
                <div class="review-text">
                    "<%= testoRecensione %>"
                </div>
                <div class="review-stars">
                    <% for (int i = 1; i <= 5; i++) {
                        if (i <= stelle) { %>
                            <i class="fas fa-star"></i>
                    <% } else { %>
                            <i class="far fa-star"></i>
                    <% } } %>
                </div>
                <div class="review-actions">
                    <div class="likes-dislikes">
                        <button class="btn-like <%= (val != null && val.isLikeDislike()) ? "active" : "" %>" 
                                <%= (user != null && "RECENSORE".equals(user.getTipoUtente())) 
                                    ? "onclick=\"voteReview('" + film.getIdFilm() + "', '" + emailRecensore + "', true)\"" 
                                    : "disabled" %>>
                            <i class="fas fa-thumbs-up"></i> <span><%= r.getNLike() %></span>
                        </button>
                        <button class="btn-dislike <%= (val != null && !val.isLikeDislike()) ? "active" : "" %>" 
                                <%= (user != null && "RECENSORE".equals(user.getTipoUtente())) 
                                    ? "onclick=\"voteReview('" + film.getIdFilm() + "', '" + emailRecensore + "', false)\"" 
                                    : "disabled" %>>
                            <i class="fas fa-thumbs-down"></i> <span><%= r.getNDislike() %></span>
                        </button>
                    </div>
                    <% if (user != null && "RECENSORE".equals(user.getTipoUtente())) { %>
                        <button class="btn-report" 
                                onclick="reportReview('<%= film.getIdFilm() %>', '<%= emailRecensore %>')">
                            <i class="fas fa-flag"></i> Segnala
                        </button>
                    <% } %>
                </div>
            </div>

            <% 
                } 
            } else { 
            %>
            <p class="no-reviews-msg">Nessuna recensione presente per questo film.</p>
            <% } %>

        </div>

        <div class="right-column">
            <div class="film-details">
                <%
                    String LocandinaBase64 = "";
                    if (film != null && film.getLocandina() != null) {
                        byte[] iconaBytes = film.getLocandina();
                        if (iconaBytes.length > 0) {
                            LocandinaBase64 = Base64.getEncoder().encodeToString(iconaBytes);
                        }
                    }
                %>
                
                <% if (film != null) { %>
                <img class="film-poster" 
                     src="<%= LocandinaBase64.isEmpty() ? request.getContextPath() + "/static/images/RATED_icon.png" : "data:image/png;base64," + LocandinaBase64 %>" 
                    alt="Locandina di <%= film.getNome() %>" />

                <h2 class="film-title"><%= film.getNome() %></h2>
                
                <div class="review-stars">
                    <%
                    int stelleFilm = film.getValutazione();
                    for (int i = 1; i <= 5; i++) {
                        if (i <= stelleFilm) { %>
                            <i class="fas fa-star"></i>
                    <% } else { %>
                            <i class="far fa-star"></i>
                    <% }} %>
                </div>
                
                <div class="film-meta-details">
                    <p class="film-year-genre">
                        <strong><%= film.getAnno() %></strong> <%= displayGeneri %>
                    </p>
                    
                    <p><strong>Durata:</strong> <%= film.getDurata() %> min</p>
                    <p><strong>Regista:</strong> <%= film.getRegista() %></p>
                    <p><strong>Cast:</strong> <%= film.getAttori() %></p>
                </div>

                <p class="film-description"><%= film.getTrama() %></p>

                <%-- --- SEZIONE PULSANTI LISTE (WATCHLIST / VISTO) --- --%>
                <% if (user != null && "RECENSORE".equals(user.getTipoUtente())) { %>
                    <div class="user-lists-actions">
                        
                        <%-- Pulsante Watchlist --%>
                        <button type="button" 
                                class="btn-list-action btn-watchlist <%= isInWatchlist ? "active" : "" %>" 
                                id="btnWatchlist"
                                onclick="toggleUserList('<%= film.getIdFilm() %>', 'watchlist', this)"
                        >
                            <% if(isInWatchlist) { %>
                                <i class="fas fa-minus-circle"></i> Rimuovi dalla Watchlist
                            <% } else { %>
                                <i class="fas fa-bookmark"></i> Aggiungi alla Watchlist
                            <% } %>
                        </button>

                        <%-- Pulsante Watched --%>
                        <button type="button" 
                                class="btn-list-action btn-watched <%= (isInWatched || userHasReviewed) ? "active" : "" %>" 
                                id="btnWatched"
                                <%= userHasReviewed ? "disabled title='Recensione presente: segnato automaticamente come visto'" : "onclick=\"toggleUserList('" + film.getIdFilm() + "', 'watched', this)\"" %>
                        >
                            <% if(userHasReviewed) { %>
                                <i class="fas fa-check-double"></i> Visto (Recensito)
                            <% } else if (isInWatched) { %>
                                <i class="fas fa-times-circle"></i> Rimuovi da Visti
                            <% } else { %>
                                <i class="fas fa-check-circle"></i> Segna come Visto
                            <% } %>
                        </button>
                    
                    </div>
                <% } %>
                
                <div class="rate-film">
                    <% if (user != null && "RECENSORE".equals(user.getTipoUtente()) && (user.getNWarning() < 3)) { %>
                        
                        <% if (userHasReviewed) { %>
                            <button type="button" class="btn-rate" 
                                    onclick="alert('Hai già recensito questo film!')">
                                RATE IT
                            </button>
                        <% } else { %>
                            <button type="button" class="btn-rate" id="btnRateFilm" onclick="showReviewForm()">
                                RATE IT
                            </button>
                        <% } %>
                
                    <% } else { %>
                        <button type="button" class="btn-rate" disabled>RATE IT</button>
                    <% } %>
                </div>


                <% if (user != null && "GESTORE".equals(user.getTipoUtente())) { %>
                    <div class="manage-film">
                        <button type="button" class="btn-delete" onclick="deleteFilm('<%= film.getIdFilm() %>')">Elimina Film</button>
                        <button type="button" class="btn-modify" onclick="showModifyForm()">Modifica Informazioni Film</button>
                    </div>
                <% } %>

                <% } else { %>
                    <p>Errore: Dati del film non disponibili.</p>
                <% } %>

            </div>
        </div>
    </div>
</div>

<% if (film != null) { %>
<div id="reviewOverlay" class="overlay">
    <div class="overlay-content">
        <span class="close-btn" onclick="hideReviewForm()">&times;</span>
        <h2>Pubblica una Recensione</h2>
        <form action="<%= request.getContextPath() %>/ValutaFilm" method="post" onsubmit="return validateReviewForm()">
                <input type="hidden" name="idFilm" value="<%= film.getIdFilm() %>" />
                
                <label for="titolo">Titolo:</label>
                <input type="text" id="titolo" name="titolo" required />

                <label for="recensione">Recensione:</label>
                <textarea id="recensione" name="recensione" rows="5" required></textarea>

                <label>Valutazione:</label>
                <div class="star-rating">
                    <input type="radio" id="star5" name="valutazione" value="5" />
                    <label for="star5" title="5 stelle"><i class="fas fa-star"></i></label>
                    <input type="radio" id="star4" name="valutazione" value="4" />
                    <label for="star4" title="4 stelle"><i class="fas fa-star"></i></label>
                    <input type="radio" id="star3" name="valutazione" value="3" />
                    <label for="star3" title="3 stelle"><i class="fas fa-star"></i></label>
                    <input type="radio" id="star2" name="valutazione" value="2" />
                    <label for="star2" title="2 stelle"><i class="fas fa-star"></i></label>
                    <input type="radio" id="star1" name="valutazione" value="1" />
                    <label for="star1" title="1 stella"><i class="fas fa-star"></i></label>
                </div>

                <p style="font-size: 12px; color: #ccc; margin-top: 10px;">
                    *Pubblicando la recensione, il film verrà automaticamente aggiunto alla lista dei film visti.
                </p>

                <button type="submit" class="btn-submit">Pubblica</button>
            </form>
    </div>
</div>

<div id="modifyOverlay" class="overlay">
    <div class="overlay-content">
        <span class="close-btn" onclick="hideModifyForm()">&times;</span>
        <h2>Modifica Informazioni Film</h2>
        <form action="<%= request.getContextPath() %>/filmModify" method="post" enctype="multipart/form-data">
                <input type="hidden" name="idFilm" value="<%= film.getIdFilm() %>" />

                <label for="nomeFilm">Nome Film:</label>
                <input type="text" id="nomeFilm" name="nomeFilm" value="<%= film.getNome() %>" required />

                <label for="registaFilm">Regista:</label>
                <input type="text" id="registaFilm" name="registaFilm" value="<%= film.getRegista() %>" required />

                <label for="annoFilm">Anno:</label>
                <input type="number" id="annoFilm" name="annoFilm" value="<%= film.getAnno() %>" required />

                <label for="generiFilm">Generi (Ctrl+Click per modificare la selezione):</label>
                <select name="generiFilm" id="generiFilm" multiple required style="height: 120px; width: 100%; background-color: #555; color: white; border: 1px solid #ccc; padding: 5px;">
                <%
                    String[] allGeneri = {
                        "Animazione", "Avventura", "Azione", "Biografico", "Commedia",
                        "Crimine", "Documentario", "Drammatico", "Epico", "Famiglia",
                        "Fantascienza", "Fantasy", "Giallo", "Guerra", "Horror",
                        "Mistero", "Musicale", "Noir", "Poliziesco", "Romantico",
                        "Sentimentale", "Sportivo", "Storico", "Thriller", "Western"
                    };

                    for (String g : allGeneri) {
                        String isSelected = (nomiGeneriAttuali.contains(g)) ? "selected" : "";
                %>
                    <option value="<%= g %>" <%= isSelected %>><%= g %></option>
                <%
                    }
                %>
                </select>
                
                <label for="tramaFilm">Trama:</label>
                <textarea id="tramaFilm" name="tramaFilm" rows="4" required><%= film.getTrama() %></textarea>

                <label for="durataFilm">Durata (minuti):</label>
                <input type="number" id="durataFilm" name="durataFilm" value="<%= film.getDurata() %>" required />

                <label for="attoriFilm">Attori:</label>
                <input type="text" id="attoriFilm" name="attoriFilm" value="<%= film.getAttori() %>" required />

                <label for="locandinaFilm">Nuova Locandina (opzionale):</label>
                <input type="file" id="locandinaFilm" name="locandinaFilm" accept="image/*" />

                <button type="submit" class="btn-submit">Salva Modifiche</button>
            </form>
    </div>
</div>
<% } %>

</body>
</html>