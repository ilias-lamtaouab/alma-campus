package com.scolaris.dao;

import com.scolaris.model.Teacher;
import java.util.Optional;

public interface TeacherDao extends GenericDao<Teacher, Integer> {
    Optional<Teacher> findByCin(String cin);
}
