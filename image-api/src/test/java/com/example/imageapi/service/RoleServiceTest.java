package com.example.imageapi.service;

import com.example.imageapi.config.BaseTest;
import com.example.imageapi.exception.EntityNotFoundException;
import com.example.imageapi.model.enumeration.RoleEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

public class RoleServiceTest extends BaseTest {

    @Autowired
    private RoleService roleService;

    @ParameterizedTest
    @EnumSource(RoleEnum.class)
    public void getRoleByName_RoleExists(RoleEnum role) {
        var roleName = role.name();

        var result = roleService.getRoleByName(roleName);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals(roleName, result.getName())
        );

    }

    @Test
    public void getRoleByName_RoleDoesNotExist() {
        var roleName = "ROLE_NON_EXISTENT";

        Assertions.assertThrows(EntityNotFoundException.class, () -> roleService.getRoleByName(roleName));
    }

}
