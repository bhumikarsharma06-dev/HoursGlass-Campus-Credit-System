package com.hourglass.repository;

import com.hourglass.db.Database;
import com.hourglass.model.ServiceListing;
import com.hourglass.model.ServiceRequest;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HourGlassRepository {
    public String[] authenticate(String email, String password) throws SQLException {
        String sql = "SELECT id, full_name, password_hash FROM users WHERE email = ?";
        try (Connection connection = Database.open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !BCrypt.checkpw(password, result.getString(3))) return null;
                return new String[]{String.valueOf(result.getLong(1)), result.getString(2)};
            }
        }
    }

    public List<ServiceListing> findServices() throws SQLException {
        String sql = "SELECT s.id, s.title, s.category, s.duration_hours, s.mode, s.description, u.full_name " +
                "FROM services s JOIN users u ON u.id = s.provider_id WHERE s.active = TRUE ORDER BY s.created_at DESC";
        List<ServiceListing> services = new ArrayList<>();
        try (Connection connection = Database.open(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                services.add(new ServiceListing(result.getLong(1), result.getString(2), result.getString(3),
                        result.getInt(4), result.getString(5), result.getString(6), result.getString(7)));
            }
        }
        return services;
    }

    public List<ServiceRequest> findRequests(long requesterId) throws SQLException {
        String sql = "SELECT r.id, s.title, u.full_name, r.status FROM service_requests r " +
                "JOIN services s ON s.id = r.service_id JOIN users u ON u.id = s.provider_id " +
                "WHERE r.requester_id = ? ORDER BY r.created_at DESC";
        List<ServiceRequest> requests = new ArrayList<>();
        try (Connection connection = Database.open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, requesterId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) requests.add(new ServiceRequest(result.getLong(1), result.getString(2), result.getString(3), result.getString(4)));
            }
        }
        return requests;
    }

    public int findBalance(long userId) throws SQLException {
        try (Connection connection = Database.open(); PreparedStatement statement = connection.prepareStatement("SELECT balance FROM wallets WHERE user_id = ?")) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new SQLException("Wallet not found");
                return result.getInt(1);
            }
        }
    }

    public void createService(long providerId, String title, String category, int hours, String mode, String description) throws SQLException {
        String sql = "INSERT INTO services(provider_id, title, category, duration_hours, mode, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, providerId); statement.setString(2, title); statement.setString(3, category);
            statement.setInt(4, hours); statement.setString(5, mode); statement.setString(6, description); statement.executeUpdate();
        }
    }

    public void createRequest(long requesterId, long serviceId) throws SQLException {
        String sql = "INSERT INTO service_requests(requester_id, service_id, status) SELECT ?, ?, 'PENDING' FROM services s WHERE s.id = ? AND s.provider_id <> ? AND s.active = TRUE";
        try (Connection connection = Database.open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, requesterId); statement.setLong(2, serviceId); statement.setLong(3, serviceId); statement.setLong(4, requesterId);
            if (statement.executeUpdate() == 0) throw new SQLException("Invalid service or self-request");
        }
    }

    public void updateRequestStatus(long requestId, long requesterId, String expected, String next) throws SQLException {
        String sql = "UPDATE service_requests SET status = ? WHERE id = ? AND requester_id = ? AND status = ?";
        try (Connection connection = Database.open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, next); statement.setLong(2, requestId); statement.setLong(3, requesterId); statement.setString(4, expected);
            if (statement.executeUpdate() != 1) throw new SQLException("Request is not in the expected state");
        }
    }

    public void verifyAndTransfer(long requestId, long requesterId) throws SQLException {
        String requestSql = "SELECT r.service_id, s.provider_id, s.duration_hours FROM service_requests r JOIN services s ON s.id = r.service_id WHERE r.id = ? AND r.requester_id = ? AND r.status = 'COMPLETED' FOR UPDATE";
        try (Connection connection = Database.open()) {
            connection.setAutoCommit(false);
            try {
                long providerId; int credits;
                try (PreparedStatement statement = connection.prepareStatement(requestSql)) {
                    statement.setLong(1, requestId); statement.setLong(2, requesterId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) throw new SQLException("Request is not ready for verification");
                        providerId = result.getLong(2); credits = result.getInt(3);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement("SELECT balance FROM wallets WHERE user_id = ? FOR UPDATE")) {
                    statement.setLong(1, requesterId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next() || result.getInt(1) < credits) throw new SQLException("Insufficient Time Credits");
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement("UPDATE wallets SET balance = balance - ? WHERE user_id = ?")) {
                    statement.setInt(1, credits); statement.setLong(2, requesterId); statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("UPDATE wallets SET balance = balance + ? WHERE user_id = ?")) {
                    statement.setInt(1, credits); statement.setLong(2, providerId); statement.executeUpdate();
                }
                String token = UUID.randomUUID().toString();
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO transactions(request_id, from_user_id, to_user_id, credits, reason, verification_token) VALUES (?, ?, ?, ?, ?, ?)")) {
                    statement.setLong(1, requestId); statement.setLong(2, requesterId); statement.setLong(3, providerId); statement.setInt(4, credits); statement.setString(5, "Verified service exchange"); statement.setString(6, token); statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("UPDATE service_requests SET status = 'VERIFIED', verified_at = CURRENT_TIMESTAMP WHERE id = ? AND status = 'COMPLETED'")) {
                    statement.setLong(1, requestId); if (statement.executeUpdate() != 1) throw new SQLException("Request was already verified");
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
