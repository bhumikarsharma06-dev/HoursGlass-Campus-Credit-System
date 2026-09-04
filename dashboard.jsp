<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>HourGlass | Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/hourglass.css">
</head>
<body>
<header class="topbar">
    <a class="brand" href="${pageContext.request.contextPath}/hourglass">HourGlass</a>
    <nav><a href="${pageContext.request.contextPath}/hourglass">Services</a><a href="#requests">Requests</a><a href="#wallet">Wallet</a></nav>
    <form method="post" action="${pageContext.request.contextPath}/hourglass"><input type="hidden" name="action" value="logout"><button>Logout</button></form>
</header>
<main class="container">
    <section class="hero"><p class="eyebrow">Campus time-credit exchange</p><h1>Welcome, <c:out value="${sessionScope.userName}"/>.</h1><p>One hour of help equals one Time Credit.</p><div class="balance"><span>Available balance</span><strong><c:out value="${walletBalance}"/> credits</strong></div></section>
    <c:if test="${not empty message}"><p class="notice"><c:out value="${message}"/></p></c:if>
    <section><div class="section-heading"><h2>Available services</h2><a class="button" href="#offer">Offer a service</a></div><div class="service-grid">
        <c:forEach var="service" items="${services}"><article class="service-card"><span class="tag"><c:out value="${service.category}"/></span><h3><c:out value="${service.title}"/></h3><p><c:out value="${service.description}"/></p><small><c:out value="${service.providerName}"/> · <c:out value="${service.durationHours}"/> hour(s) · <c:out value="${service.mode}"/></small><form method="post" action="${pageContext.request.contextPath}/hourglass"><input type="hidden" name="action" value="request"><input type="hidden" name="serviceId" value="${service.id}"><button class="button" type="submit">Request help</button></form></article></c:forEach>
    </div></section>
    <section id="requests" class="panel"><h2>Recent requests</h2><table><thead><tr><th>Service</th><th>Provider</th><th>Status</th><th>Action</th></tr></thead><tbody><c:forEach var="request" items="${requests}"><tr><td><c:out value="${request.serviceTitle}"/></td><td><c:out value="${request.providerName}"/></td><td><span class="status"><c:out value="${request.status}"/></span></td><td><c:if test="${request.status eq 'PENDING'}"><form method="post" action="${pageContext.request.contextPath}/hourglass"><input type="hidden" name="action" value="accept"><input type="hidden" name="requestId" value="${request.id}"><button type="submit">Accept</button></form></c:if><c:if test="${request.status eq 'ACCEPTED'}"><form method="post" action="${pageContext.request.contextPath}/hourglass"><input type="hidden" name="action" value="complete"><input type="hidden" name="requestId" value="${request.id}"><button type="submit">Complete</button></form></c:if><c:if test="${request.status eq 'COMPLETED'}"><form method="post" action="${pageContext.request.contextPath}/hourglass"><input type="hidden" name="action" value="verify"><input type="hidden" name="requestId" value="${request.id}"><button type="submit">Verify QR</button></form></c:if></td></tr></c:forEach></tbody></table></section>
    <section id="offer" class="panel"><h2>Offer a service</h2><form class="form" method="post" action="${pageContext.request.contextPath}/hourglass"><input type="hidden" name="action" value="offer"><input name="title" placeholder="Service title" required><input name="category" placeholder="Category" required><input name="durationHours" type="number" min="1" placeholder="Duration in hours" required><select name="mode"><option>Online</option><option>Offline</option></select><textarea name="description" placeholder="Description" required></textarea><button class="button" type="submit">Publish service</button></form></section>
</main>
</body>
</html>
