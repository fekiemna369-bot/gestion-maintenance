<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="maintenance.utilisateur.model.User" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Gestion des utilisateurs</title>
</head>
<body>
<main>
    <h1>Gestion des utilisateurs</h1>
    <% if (request.getAttribute("error") != null) { %>
        <p role="alert"><%= request.getAttribute("error") %></p>
    <% } %>
    <form method="post" action="<%= request.getContextPath() %>/app/admin/utilisateurs">
        <label for="nom">Nom</label>
        <input id="nom" name="nom" required>
        <label for="prenom">Prénom</label>
        <input id="prenom" name="prenom" required>
        <label for="email">Email</label>
        <input id="email" name="email" type="email" required>
        <label for="password">Mot de passe</label>
        <input id="password" name="password" type="password" minlength="10" required>
        <label for="role">Rôle</label>
        <select id="role" name="role" required>
            <option value="requester">Demandeur</option>
            <option value="technician">Technicien</option>
            <option value="maintenance_manager">Responsable maintenance</option>
        </select>
        <button type="submit">Créer l'utilisateur</button>
    </form>

    <h2>Utilisateurs existants</h2>
    <table>
        <thead>
            <tr><th>Nom</th><th>Prénom</th><th>Email</th><th>Rôle</th><th>Actif</th></tr>
        </thead>
        <tbody>
        <% for (User user : (java.util.List<User>) request.getAttribute("users")) { %>
            <tr>
                <td><%= user.getNom() %></td>
                <td><%= user.getPrenom() %></td>
                <td><%= user.getEmail() %></td>
                <td><%= user.getRole() %></td>
                <td><%= user.isActif() ? "Oui" : "Non" %></td>
            </tr>
        <% } %>
        </tbody>
    </table>
</main>
</body>
</html>