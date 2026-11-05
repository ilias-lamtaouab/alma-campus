package com.scolaris.dao;

import com.scolaris.model.Subject;
import java.util.List;

public interface SubjectDao extends GenericDao<Subject, Integer> {
    List<Subject> findByModuleId(int moduleId);
    List<Subject> findByTeacherId(int teacherId);
}
