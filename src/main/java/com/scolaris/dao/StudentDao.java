package com.scolaris.dao;

import com.scolaris.model.Student;
import java.util.Optional;

public interface StudentDao extends GenericDao<Student, Integer> {
    Optional<Student> findByCne(String cne);
}
