package com.example.imageapi.service;

import com.example.imageapi.model.entity.Privilege;
import com.example.imageapi.repository.PrivilegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivilegeService {

    private final PrivilegeRepository privilegeRepository;

    public List<Privilege> findPrivilegesByRoleId(Long id) {
        return privilegeRepository.findPrivilegesByRoleId(id);
    }

}
