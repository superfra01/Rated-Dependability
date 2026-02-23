<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="model.Entity.FilmBean" %>
<%@ page import="model.Entity.FilmGenereBean" %>
<%@ page import="model.Entity.UtenteBean" %>
<%@ page import="java.util.Base64" %>

<%
    // Recupero l'utente visitato per mostrare il titolo corretto
    UtenteBean visitedUser = (UtenteBean) session.getAttribute("visitedUser");
    String visitedUsername = (visitedUser != null) ? visitedUser.getUsername() : "Utente";

    // Recupero le liste dalla sessione
    // Nota: Il backend deve aver popolato questi attributi prima di forwardare qui
    List<FilmBean> watchedList = (List<FilmBean>) session.getAttribute("watchedList");
    List<FilmBean> watchlist = (List<FilmBean>) session.getAttribute("watchlist");

    if (watchedList == null) watchedList = new ArrayList<>();
    if (watchlist == null) watchlist = new ArrayList<>();

    // Determino quale lista mostrare basandomi sul parametro GET 'listType'
    // Default: 'watched'
    String listType = request.getParameter("listType");
    List<FilmBean> filmsToShow;
    String activeLabel = "";

    if ("watchlist".equals(listType)) {
        filmsToShow = watchlist;
        activeLabel = "In Watchlist";
    } else {
        // Default case
        listType = "watched";
        filmsToShow = watchedList;
        activeLabel = "Visti";
    }
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8" />
    <title>Film di <%= visitedUsername %></title>
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/static/images/favicon.ico">
    <link rel="stylesheet" href="static/css/Catalogo.css" />
    <link rel="stylesheet" href="static/css/UserFilms.css" />
    <script src="static/scripts/userFilmsFunctions.js" defer></script>
</head>

<jsp:include page="header.jsp" />

<body>

<div class="catalog-container">
    <div class="user-films-header">
        <h1>Film di <%= visitedUsername %></h1>
        
        <div class="list-toggle-container">
            <button class="toggle-btn <%= "watched".equals(listType) ? "active" : "" %>" 
                    onclick="switchList('watched')">
                Visti (<%= watchedList.size() %>)
            </button>
            <button class="toggle-btn <%= "watchlist".equals(listType) ? "active" : "" %>" 
                    onclick="switchList('watchlist')">
                Watchlist (<%= watchlist.size() %>)
            </button>
        </div>
    </div>

    <%
        if (filmsToShow == null || filmsToShow.isEmpty()) {
    %>
        <div class="empty-state">
            <p>Nessun film presente nella lista "<%= activeLabel %>".</p>
        </div>
    <%
        } else {
    %>
    
    <div class="film-grid">
        <%
            for (FilmBean film : filmsToShow) {
                String dettaglioUrl = "film?idFilm=" + film.getIdFilm();
        %>
            <div class="film-card" onclick="window.location.href='<%= dettaglioUrl %>'">
                <div class="film-poster">
                    <img src="<%= film.getLocandina() != null
                                    ? "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(film.getLocandina())
                                    : request.getContextPath() + "/static/images/RATED_icon.png" 
                              %>"
                        alt="Locandina" />
                </div>
                <div class="film-info">
                    <h3><%= film.getNome() %></h3>

                    <%
                    // Recupero generi (simulato o da sessione come nel catalogo originale)
                    List<FilmGenereBean> listaGeneri = (List<FilmGenereBean>) session.getAttribute(film.getIdFilm() + "Generi");
                    
                    String stringaGeneri = "";
                    if (listaGeneri != null && !listaGeneri.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (FilmGenereBean g : listaGeneri) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(g.getNomeGenere()); 
                        }
                        stringaGeneri = sb.toString();
                    } else {
                        stringaGeneri = "Info generi non disp.";
                    }
                    %>

                    <p class="film-genres"><%= stringaGeneri %></p>
                    <div class="film-rating">
                        <%
                            int stars = film.getValutazione();
                            int maxStars = 5;
                            for (int i = 0; i < stars; i++) {
                                out.print("&#9733; ");
                            }
                            for (int i = stars; i < maxStars; i++) {
                                out.print("&#9734; ");
                            }
                        %>
                    </div>
                </div>
            </div>
        <%
            }
        %>
    </div>
    <%
        } 
    %>
</div>

</body>
</html>