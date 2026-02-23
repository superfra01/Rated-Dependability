<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="java.util.*" import="model.Entity.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Rated</title>
    <link rel="stylesheet" href="static/css/Header.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
</head>
<body>
    <header>
        <div class="logo">
            <a href="<%= request.getContextPath() %>/">
                <span>RATED</span>
            </a>
        </div>
        
        <div class="search-container">
            <form action="<%= request.getContextPath() %>/ricerca" method="get" style="display: flex; align-items: center;">
                <div class="search-bar">
                    <!-- L'input deve avere name="filmCercato" -->
                    <input type="text" name="filmCercato" placeholder="Cerca Film su RATED">
                </div>
                <button type="submit" class="catalogue-button">Cerca</button>
            </form>

            <a href="<%= request.getContextPath() %>/catalogo">
                <button class="catalogue-button">Catalogo</button>
            </a>

            <%
                UtenteBean user = (UtenteBean) request.getSession().getAttribute("user");
                if (user != null && "MODERATORE".equals(user.getTipoUtente())) {
            %>
            <a href="<%= request.getContextPath() %>/moderator">
                <button class="catalogue-button">Moderazione</button>
            </a>
            <% } %>
        </div>
        
        <div class="user-icon">
            <%
            if (user != null) { %>
                <a href="<%= request.getContextPath() %>/profile?visitedUser=<%= user.getUsername() %>">
                    <i class="fas fa-user-circle"></i> 
                </a>
            <% } else { %>
                <a href="<%= request.getContextPath() %>/login">
                    <i class="fas fa-user-circle"></i>
                </a>
            <% } %>
        </div>
    </header>
</body>
</html>
