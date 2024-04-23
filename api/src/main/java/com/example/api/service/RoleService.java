package com.example.api.service;

import com.example.api.exception.EntityNotFoundException;
import com.example.api.model.entity.Role;
import com.example.api.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("There is no role with that name"));
    }

}
