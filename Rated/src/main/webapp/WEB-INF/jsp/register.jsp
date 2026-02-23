<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register</title>
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/static/images/favicon.ico">
    <link rel="stylesheet" href="static/css/loginRegister.css">
    <script src="static/scripts/registerValidation.js" defer></script>
</head>
<body>
    <jsp:include page="header.jsp" />
    
    <main>
        <div class="login-container">
            <h3>Una ricca community ti aspetta</h3>
            <h4>Register</h4>
            <form id="regForm" action="<%= request.getContextPath() %>/register" method="post" enctype="multipart/form-data">
                
                <input type="text" id="username" name="username" placeholder="Username" required>
                <span id="errorUsername" aria-live="polite"></span>
                
                <input type="text" id="email" name="email" placeholder="E-mail" required>
                <span id="errorEmail" aria-live="polite"></span>
                
                <input type="password" id="password" name="password" placeholder="Password" required>
                <span id="errorPassword" aria-live="polite"></span>
                
                <input type="password" id="confirm_password" name="confirm_password" placeholder="Confirm Password" required>
                <span id="errorConfirmPassword" aria-live="polite"></span>
                
                <input type="file" id="profile_icon" name="profile_icon" accept="image/*" required>
                <span id="errorProfileIcon" aria-live="polite"></span>
                
                <textarea id="bio" name="biography" placeholder="Biografia" rows="4" required></textarea>
                <span id="errorBio" aria-live="polite"></span>

                <div class="genres-section">
                    <label class="section-label">Generi Preferiti (Seleziona almeno 2):</label>
                    <div id="genresContainer" class="genres-container">
                        <% 
                            // Recupero la lista come List<String> (come in profile.jsp)
                            List<String> genresList = (List<String>) session.getAttribute("genres");

                            if (genresList != null && !genresList.isEmpty()) {
                                for (String g : genresList) { 
                        %>
                                <div class="genre-item">
                                    <input type="checkbox" id="genre_<%= g %>" name="genres" value="<%= g %>">
                                    <label for="genre_<%= g %>"><%= g %></label>
                                </div>
                        <% 
                                }
                            } else { 
                        %>
                                <div class="backend-error">
                                    <p>⚠ Generi non disponibili al momento.</p>
                                </div>
                        <% 
                            } 
                        %>
                    </div>
                    <span id="errorGenres" aria-live="polite"></span>
                </div>
                <br>
                
                <button type="submit">Register</button>
            </form>
            <p>Hai già un account? <a href="<%= request.getContextPath() %>/login" class="login-now">Login</a></p>
        </div>
    </main>
</body>
</html>