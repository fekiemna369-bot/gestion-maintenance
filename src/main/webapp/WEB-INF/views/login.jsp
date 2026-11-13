<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Connexion</title>
</head>
<body>
<main>
    <h1>Connexion</h1>
    <% if (request.getAttribute("error") != null) { %>
        <p role="alert"><%= request.getAttribute("error") %></p>
    <% } %>
    <form method="post" action="<%= request.getContextPath() %>/login">
        <label for="email">Email</label>
        <input id="email" name="email" type="email" required autocomplete="username">

        <label for="password">Mot de passe</label>
        <input id="password" name="password" type="password" required autocomplete="current-password">

        <button type="submit">Se connecter</button>
    </form>
</main>
</body>
</html>