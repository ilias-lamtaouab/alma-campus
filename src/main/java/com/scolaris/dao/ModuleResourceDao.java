package com.scolaris.dao;

import com.scolaris.model.ModuleResource;
import java.util.List;

public interface ModuleResourceDao {
    ModuleResource save(ModuleResource resource);
    List<ModuleResource> findAll();
    List<ModuleResource> findByModuleId(int moduleId);
    ModuleResource findById(int id);
    boolean delete(int id);
}