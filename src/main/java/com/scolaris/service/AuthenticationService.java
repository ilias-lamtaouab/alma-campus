package com.scolaris.service;

import com.scolaris.dao.UserDao;
import com.scolaris.dao.impl.UserDaoImpl;
import com.scolaris.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthenticationService {

    private final UserDao userDao;
    private static User currentUser;

    public AuthenticationService() {
        this.userDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.UserDao.class);
    }

    public boolean authenticate(String username, String plainPassword) {
        Optional<User> optionalUser = userDao.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if ("BLOCKED".equals(user.getStatus())) {
                System.out.println("Login blocked: User account is deactivated.");
                return false;
            }
            if (BCrypt.checkpw(plainPassword, user.getPasswordHash())) {
                currentUser = user;
                return true;
            }
        }
        return false;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }

    public boolean register(String username, String plainPassword, String role) {
        Optional<User> existingUser = userDao.findByUsername(username);
        if (existingUser.isPresent()) {
            return false; // Username already taken
        }

        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(hashedPassword);
        newUser.setRole(role);
        newUser.setStatus("ACTIVE");

        User savedUser = userDao.save(newUser);
        return savedUser.getId() > 0;
    }
}
