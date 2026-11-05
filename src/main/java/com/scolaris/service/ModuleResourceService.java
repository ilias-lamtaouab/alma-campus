package com.scolaris.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scolaris.dao.ModuleResourceDao;
import com.scolaris.dao.impl.ModuleResourceDaoImpl;
import com.scolaris.model.ModuleResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

public class ModuleResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ModuleResourceService.class);


    private final ModuleResourceDao resourceDao;

    public ModuleResourceService() {
        this.resourceDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.ModuleResourceDao.class);
    }

    public boolean addResource(int moduleId, String title, String description, File sourceFile, String uploadedBy) {
        if (title == null || title.isEmpty() || sourceFile == null || !sourceFile.exists()) {
            return false;
        }

        String resourcesDir = System.getProperty("user.dir") + "/resources/modules/" + moduleId;
        File dir = new File(resourcesDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
        File destFile = new File(dir, fileName);

        try {
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Error occurred in ModuleResourceService", e);
            return false;
        }

        ModuleResource resource = new ModuleResource();
        resource.setModuleId(moduleId);
        resource.setTitle(title);
        resource.setDescription(description);
        resource.setFileName(sourceFile.getName());
        resource.setFilePath(destFile.getAbsolutePath());
        resource.setFileSize(sourceFile.length());
        resource.setUploadedBy(uploadedBy);
        resource.setUploadedAt(LocalDateTime.now());

        return resourceDao.save(resource) != null;
    }

    public boolean addResourceWithoutFile(int moduleId, String title, String description, String uploadedBy) {
        if (title == null || title.isEmpty()) return false;

        ModuleResource resource = new ModuleResource();
        resource.setModuleId(moduleId);
        resource.setTitle(title);
        resource.setDescription(description);
        resource.setFileName("");
        resource.setFilePath("");
        resource.setFileSize(0);
        resource.setUploadedBy(uploadedBy);
        resource.setUploadedAt(LocalDateTime.now());

        return resourceDao.save(resource) != null;
    }

    public List<ModuleResource> getAllResources() {
        return resourceDao.findAll();
    }

    public List<ModuleResource> getResourcesByModule(int moduleId) {
        return resourceDao.findByModuleId(moduleId);
    }

    public boolean deleteResource(int id) {
        ModuleResource resource = resourceDao.findById(id);
        if (resource != null) {
            String filePath = resource.getFilePath();
            if (filePath != null && !filePath.isEmpty()) {
                new File(filePath).delete();
            }
        }
        return resourceDao.delete(id);
    }
}