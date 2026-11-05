package com.scolaris.dao;

import com.scolaris.model.Grade;
import java.util.List;

public interface GradeDao extends GenericDao<Grade, Integer> {
    List<Grade> findByStudentId(int studentId);
    List<Grade> findBySubjectId(int subjectId);
}
