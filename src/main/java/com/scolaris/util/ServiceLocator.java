package com.scolaris.util;

import com.scolaris.dao.*;
import com.scolaris.dao.impl.*;
import com.scolaris.service.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ServiceLocator implements a lightweight dependency injection container.
 * It ensures singletons for all DAOs and Services, decoupling controllers from implementations.
 */
public class ServiceLocator {
    private static ServiceLocator instance;
    private final Map<Class<?>, Object> services = new HashMap<>();

    private ServiceLocator() {
        instance = this;
        // Initialize DAOs
        StudentDao studentDao = new StudentDaoImpl();
        TeacherDao teacherDao = new TeacherDaoImpl();
        ModuleDao moduleDao = new ModuleDaoImpl();
        SubjectDao subjectDao = new SubjectDaoImpl();
        GradeDao gradeDao = new GradeDaoImpl();
        UserDao userDao = new UserDaoImpl();
        CertificateRequestDao certificateRequestDao = new CertificateRequestDaoImpl();
        SystemNotificationDao systemNotificationDao = new SystemNotificationDaoImpl();
        ModuleResourceDao moduleResourceDao = new ModuleResourceDaoImpl();

        // Register DAOs
        services.put(StudentDao.class, studentDao);
        services.put(TeacherDao.class, teacherDao);
        services.put(ModuleDao.class, moduleDao);
        services.put(SubjectDao.class, subjectDao);
        services.put(GradeDao.class, gradeDao);
        services.put(UserDao.class, userDao);
        services.put(CertificateRequestDao.class, certificateRequestDao);
        services.put(SystemNotificationDao.class, systemNotificationDao);
        services.put(ModuleResourceDao.class, moduleResourceDao);

        // Initialize Services (In a real app, these would take DAOs as constructor parameters)
        // Since the current services create their own DAOs internally or statically, 
        // we'll just register them as singletons.
        services.put(AuthenticationService.class, new AuthenticationService());
        services.put(CertificateService.class, new CertificateService());
        services.put(GradeService.class, new GradeService());
        services.put(ModuleResourceService.class, new ModuleResourceService());
        services.put(ModuleSubjectService.class, new ModuleSubjectService());
        services.put(StudentService.class, new StudentService());
        services.put(TeacherService.class, new TeacherService());
        services.put(ImportExportService.class, new ImportExportService());
    }

    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        Object service = services.get(serviceClass);
        if (service == null) {
            throw new IllegalArgumentException("Service not found: " + serviceClass.getName());
        }
        return (T) service;
    }
}
