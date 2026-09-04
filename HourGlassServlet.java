package com.hourglass.web;

import com.hourglass.repository.HourGlassRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/hourglass")
public class HourGlassServlet extends HttpServlet {
    private final HourGlassRepository repository = new HourGlassRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getSession().getAttribute("userId") == null) {
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }
        try {
            long userId = (long) request.getSession().getAttribute("userId");
            request.setAttribute("services", repository.findServices());
            request.setAttribute("requests", repository.findRequests(userId));
            request.setAttribute("walletBalance", repository.findBalance(userId));
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Unable to load HourGlass dashboard", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String action = request.getParameter("action");
        try {
            if ("login".equals(action)) {
                String[] user = repository.authenticate(required(request, "email"), required(request, "password"));
                if (user == null) {
                    request.setAttribute("error", "Invalid college email or password");
                    request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                    return;
                }
                HttpSession session = request.getSession(true);
                session.setAttribute("userId", Long.valueOf(user[0]));
                session.setAttribute("userName", user[1]);
                response.sendRedirect(request.getContextPath() + "/hourglass");
                return;
            }
            if ("logout".equals(action)) {
                request.getSession().invalidate();
                response.sendRedirect(request.getContextPath() + "/hourglass");
                return;
            }
            long userId = requireUser(request);
            switch (action) {
                case "offer" -> repository.createService(userId, required(request, "title"), required(request, "category"), Integer.parseInt(required(request, "durationHours")), required(request, "mode"), required(request, "description"));
                case "request" -> repository.createRequest(userId, Long.parseLong(required(request, "serviceId")));
                case "accept" -> repository.updateRequestStatus(Long.parseLong(required(request, "requestId")), userId, "PENDING", "ACCEPTED");
                case "complete" -> repository.updateRequestStatus(Long.parseLong(required(request, "requestId")), userId, "ACCEPTED", "COMPLETED");
                case "verify" -> repository.verifyAndTransfer(Long.parseLong(required(request, "requestId")), userId);
                default -> throw new ServletException("Unknown action");
            }
            response.sendRedirect(request.getContextPath() + "/hourglass");
        } catch (SQLException | NumberFormatException exception) {
            request.setAttribute("error", exception.getMessage());
            doGet(request, response);
        }
    }

    private long requireUser(HttpServletRequest request) throws ServletException {
        Object userId = request.getSession().getAttribute("userId");
        if (!(userId instanceof Long)) throw new ServletException("Login required");
        return (Long) userId;
    }

    private String required(HttpServletRequest request, String name) throws ServletException {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) throw new ServletException(name + " is required");
        return value.trim();
    }
}
