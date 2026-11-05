package com.scolaris.dao;

import com.scolaris.model.User;
import java.util.Optional;

public interface UserDao extends GenericDao<User, Integer> {
    Optional<User> findByUsername(String username);
}
