<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="model.Entity.UtenteBean" %>
<%@ page import="model.Entity.RecensioneBean" %>
<%@ page import="model.Entity.FilmBean" %>
<%@ include file="header.jsp" %>

<%
    UtenteBean visitedUser = (UtenteBean) session.getAttribute("visitedUser");
    UtenteBean currentUser = (UtenteBean) session.getAttribute("user");
    List<RecensioneBean> recensioni = (List<RecensioneBean>) session.getAttribute("recensioni");
    HashMap<Integer, FilmBean> filmMap = (HashMap<Integer, FilmBean>) session.getAttribute("films");

    // Recupero liste generi per il form di modifica
    List<String> allGenres = (List<String>) session.getAttribute("allGenres");
    List<String> userGenres = (List<String>) session.getAttribute("userGenres");

    if (allGenres == null) allGenres = new ArrayList<>();
    if (userGenres == null) userGenres = new ArrayList<>();

    int reputationScore = 0;
    if (recensioni != null) {
        for (RecensioneBean rec : recensioni) {
            reputationScore += (rec.getNLike() - rec.getNDislike());
        }
    }

    boolean isProfileOwner = false;
    if (currentUser != null && visitedUser.getUsername().equals(currentUser.getUsername())) {
        isProfileOwner = true;
    }
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Profilo di <%= visitedUser.getUsername() %></title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/static/images/favicon.ico">
    <link rel="stylesheet" href="static/css/Profile.css">
    <script src="static/scripts/profileScripts.js" defer></script>
</head>
<body>

<div class="profile-container">
    <div class="user-info">
        <%
            String iconaBase64 = "";
            if (visitedUser.getIcona() != null) {
                byte[] iconaBytes = visitedUser.getIcona();
                if (iconaBytes.length > 0) {
                    iconaBase64 = java.util.Base64.getEncoder().encodeToString(iconaBytes);
                }
            }
        %>
        <% if (visitedUser.getTipoUtente().equals("RECENSORE") && visitedUser.getIcona() != null) { %>
        <img src="data:image/png;base64,<%= iconaBase64 %>" alt="User Icon" />
        <% } %>
        <div>
            <div class="username"><%= visitedUser.getUsername() %></div>
            <div class="biografia"><%= visitedUser.getBiografia() %></div>
            <div class="reputation">Reputation Score: <%= reputationScore %></div>
            
            <% if (!userGenres.isEmpty()) { %>
            <div class="user-genres-display">
                <i class="fas fa-tags"></i>
                <% for(String g : userGenres) { %>
                    <span class="genre-tag"><%= g %></span>
                <% } %>
            </div>
            <% } %>
        </div>
    </div>

    <div class="buttons-container">
        <button onclick="window.location.href='${pageContext.request.contextPath}/userFilms?username=<%= visitedUser.getUsername() %>'">
            <i class="fas fa-film"></i> Film Utente
        </button>

        <% if (isProfileOwner) { %>
            <button onclick="openOverlay('changePasswordOverlay')">Cambia Password</button>
            <% if(currentUser.getTipoUtente().equals("RECENSORE")){ %>
                <button onclick="openOverlay('changeProfileOverlay')">Modifica Profilo</button>
                <button onclick="openOverlay('changeGenresOverlay')">Modifica Generi</button>
            <% } %>
            <form method="get" action="<%= request.getContextPath() %>/logout" style="display: inline;">
                <button type="submit">Logout</button>
            </form>
        <% } %>
    </div>

    <div class="reviews-container">
        <h3>Recensioni pubblicate:</h3>
        <%
            if (recensioni != null && !recensioni.isEmpty() && filmMap != null) {
                for (RecensioneBean recensione : recensioni) {
                    FilmBean film = filmMap.get(recensione.getIdFilm());
                    String nomeFilm = (film != null) ? film.getNome() : "Film non trovato";
                    String testoRecensione = recensione.getContenuto();
        %>
        <div class="review-item">
            <div>
                <span class="film-name"><%= nomeFilm %></span>
                <p class="review-text"><%= testoRecensione %></p>
            </div>
            <% if (isProfileOwner) { %>
                <a href="#"
                   onclick="if(confirm('Sei sicuro di voler eliminare questa recensione?')) { 
                                 document.getElementById('deleteReviewForm_<%= recensione.getIdFilm() %>').submit();
                             }"
                >
                    <i class="fas fa-trash-alt delete-icon"></i>
                </a>
                <form id="deleteReviewForm_<%= recensione.getIdFilm() %>" 
                      method="post" 
                      action="<%= request.getContextPath() %>/DeleteReview">
                    <input type="hidden" name="operationType" value="ReviewDelete"/>
                    <input type="hidden" name="DeleteFilmID" value="<%= recensione.getIdFilm() %>" />
                </form>
            <% } %>
        </div>
        <%
                }
            } else {
        %>
        <p>Nessuna recensione pubblicata.</p>
        <%
            }
        %>
    </div>
</div>

<% if (isProfileOwner) { %>
<div class="overlay" id="changePasswordOverlay">
    <div class="modal">
        <button class="close-modal" onclick="closeOverlay('changePasswordOverlay')">&times;</button>
        <h2>Cambia Password</h2>
        <form method="post" action="<%= request.getContextPath() %>/passwordModify" onsubmit="return validatePasswordChangeForm()">
            <input type="hidden" name="operationType" value="PasswordModify" />
            <input type="hidden" name="email" value="<%= visitedUser.getEmail() %>" />

            <label for="newPassword">Nuova Password</label>
            <input type="password" id="newPassword" name="password" required />

            <label for="confirmPassword">Conferma Password</label>
            <input type="password" id="confirmPassword" required />

            <p id="passwordError" style="color: red; display: none;">Le password non coincidono.</p>

            <button type="submit">Conferma</button>
        </form>
    </div>
</div>

<div class="overlay" id="changeProfileOverlay">
    <div class="modal">
        <button class="close-modal" onclick="closeOverlay('changeProfileOverlay')">&times;</button>
        <h2>Modifica Profilo</h2>
        <form method="post" action="<%= request.getContextPath() %>/profileModify" enctype="multipart/form-data" onsubmit="return validateProfileForm()">
            <input type="hidden" name="operationType" value="ProfileModify" />

            <label for="username">Username</label>
            <input type="text" id="username" name="username" value="<%= visitedUser.getUsername() %>" required />

            <input type="hidden" id="email" name="email" value="<%= visitedUser.getEmail() %>" />

            <label for="password">Password</label>
            <input type="password" id="password" name="password" placeholder="Nuova password..." required />

            <label for="confirmProfilePassword">Conferma Password</label>
            <input type="password" id="confirmProfilePassword" required />

            <p id="profilePasswordError" style="color: red; display: none;">Le password non coincidono.</p>

            <label for="biography">Biografia</label>
            <textarea id="biography" name="biography" rows="3"><%= visitedUser.getBiografia() %></textarea>

            <label for="icon">Icona (file immagine)</label>
            <input type="file" id="icon" name="icon" accept="image/*" />

            <button type="submit">Salva Modifiche</button>
        </form>
    </div>
</div>

<div class="overlay" id="changeGenresOverlay">
    <div class="modal modal-wide"> <button class="close-modal" onclick="closeOverlay('changeGenresOverlay')">&times;</button>
        <h2>Modifica Generi Preferiti</h2>
        <p style="color: #C4C4C4; font-size: 13px; margin-bottom: 15px;">Seleziona i generi che ti interessano:</p>
        
        <form method="post" action="<%= request.getContextPath() %>/ModificaPreferenzeServlet">
            <input type="hidden" name="operationType" value="GenresModify" />
            <input type="hidden" name="email" value="<%= visitedUser.getEmail() %>" />

            <div class="genres-grid">
                <% 
                if (allGenres != null && !allGenres.isEmpty()) {
                    for (String genere : allGenres) { 
                        boolean isSelected = userGenres.contains(genere);
                %>
                <label class="genre-item">
                    <input type="checkbox" name="selectedGenres" value="<%= genere %>" <%= isSelected ? "checked" : "" %> />
                    <span><%= genere %></span>
                </label>
                <% 
                    } 
                } else {
                %>
                    <p>Nessun genere disponibile nel sistema.</p>
                <% } %>
            </div>

            <button type="submit" style="margin-top: 20px;">Salva Preferenze</button>
        </form>
    </div>
</div>
<% } %>

</body>
</html>