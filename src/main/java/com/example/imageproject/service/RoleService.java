package com.example.imageproject.service;

import com.example.imageproject.exception.EntityNotFoundException;
import com.example.imageproject.model.entity.Role;
import com.example.imageproject.repository.RoleRepository;
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
