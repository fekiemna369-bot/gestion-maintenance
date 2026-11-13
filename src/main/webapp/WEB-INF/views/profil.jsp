<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Mon profil</title>
</head>
<body>
<main>
    <h1>Mon profil</h1>
    <dl>
        <dt>Nom</dt>
        <dd><%= session.getAttribute("nom") %></dd>
        <dt>Prénom</dt>
        <dd><%= session.getAttribute("prenom") %></dd>
        <dt>Email</dt>
        <dd><%= session.getAttribute("email") %></dd>
        <dt>Rôle</dt>
        <dd><%= session.getAttribute("role") %></dd>
    </dl>
    <form method="post" action="<%= request.getContextPath() %>/logout">
        <button type="submit">Se déconnecter</button>
    </form>
</main>
</body>
</html>