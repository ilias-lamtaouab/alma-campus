package com.scolaris.dao;

import com.scolaris.model.SystemNotification;
import java.util.List;

public interface SystemNotificationDao extends GenericDao<SystemNotification, Integer> {
    List<SystemNotification> findForStudent(String username);
    List<SystemNotification> findForTeacher(String username);
}
