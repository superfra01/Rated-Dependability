<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Entity.UtenteBean" %>
<%@ page import="model.Entity.FilmBean" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Base64" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Rated - Home</title>
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/static/images/favicon.ico">
    <link rel="stylesheet" href="static/css/HomePage.css">
    
    <script src="static/scripts/homePageWarn.js" defer></script>
    <script src="static/scripts/homePageRecs.js" defer></script>
</head>
<body>
    <%@ include file="header.jsp" %>

    <%
        // Recupero l'utente dalla sessione
        UtenteBean utente = (UtenteBean) session.getAttribute("user");
        int nWarning = 0;
        String tipoUtente = "";
        
        if (utente != null) {
            nWarning = utente.getNWarning();
            tipoUtente = utente.getTipoUtente();
        }

        // Recupero la lista dei consigliati dalla SESSIONE (come impostato nella HomePageServlet)
        List<FilmBean> filmConsigliati = (List<FilmBean>) session.getAttribute("filmConsigliati");

        // Controllo parametro loginSuccess
        String loginSuccessParam = request.getParameter("loginSuccess");
        boolean loginSuccess = "true".equals(loginSuccessParam);
    %>

    <main>
        <div class="about-container">
            <img src="static/images/RATED_icon.png" alt="Rated Logo" class="logo-large">
            <p class="description">
                Rated è una piattaforma pensata per chi ama il cinema e vuole condividere opinioni sui film, 
                scoprire nuove recensioni e interagire con altri appassionati.
                Il nostro obiettivo è promuovere discussioni di qualità e valorizzare i contenuti più apprezzati 
                dalla community. Unisciti a noi, pubblica le tue recensioni e diventa parte della nostra famiglia di cinefili!
            </p>
            <a href="catalogo">
                <button class="catalogue-button">Scopri il catalogo di film</button>
            </a>
        </div>

        <%
            if (utente != null && "RECENSORE".equals(tipoUtente) && filmConsigliati != null && !filmConsigliati.isEmpty()) {
        %>
        <div class="recommendations-container">
            <h2 class="section-title">Consigliati per te</h2>
            
            <div class="carousel-wrapper">
                <button class="nav-btn left-btn" onclick="scrollCarousel(-1)">&#10094;</button>

                <div class="carousel-track" id="recTrack">
                    <%
                        for (FilmBean film : filmConsigliati) {
                            // Link al dettaglio del film
                            String linkDettaglio = "film?idFilm=" + film.getIdFilm();
                    %>
                    <div class="film-card-mini" id="film-card-<%= film.getIdFilm() %>">
                        <div class="poster-container" onclick="window.location.href='<%= linkDettaglio %>'">
                            <img src="<%= film.getLocandina() != null 
                                          ? "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(film.getLocandina()) 
                                          : "static/images/RATED_icon.png" %>" 
                                 alt="<%= film.getNome() %>">
                        </div>
                        
                        <div class="info-container">
                            <h4 class="film-title" onclick="window.location.href='<%= linkDettaglio %>'"><%= film.getNome() %></h4>
                            <button class="remove-btn" onclick="rimuoviConsiglio(<%= film.getIdFilm() %>)">
                                Non mi interessa
                            </button>
                        </div>
                    </div>
                    <%
                        }
                    %>
                </div>

                <button class="nav-btn right-btn" onclick="scrollCarousel(1)">&#10095;</button>
            </div>
        </div>
        <%
            }
        %>
    </main>

    <script>
        // Passaggio dati al JS esistente per i warning
        const data = {
            loginSuccess: <%= loginSuccess ? "true" : "false" %>,
            nWarning: <%= nWarning %>
        };
    </script>
</body>
</html>