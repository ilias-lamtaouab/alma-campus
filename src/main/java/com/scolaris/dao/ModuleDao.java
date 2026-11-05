package com.scolaris.dao;

import com.scolaris.model.Module;
import java.util.Optional;

public interface ModuleDao extends GenericDao<Module, Integer> {
    Optional<Module> findByCode(String code);
}
