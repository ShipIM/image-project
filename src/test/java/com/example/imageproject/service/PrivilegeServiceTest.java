package com.example.imageproject.service;

import com.example.imageproject.config.BaseTest;
import com.example.imageproject.model.enumeration.PrivilegeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class PrivilegeServiceTest extends BaseTest {

    @Autowired
    private RoleService roleService;
    @Autowired
    private PrivilegeService privilegeService;

    @ParameterizedTest
    @MethodSource("roleAndPrivileges")
    public void findPrivilegesByRoleId_PrivilegesExist(String role, List<PrivilegeEnum> privileges) {
        var roleId = roleService.getRoleByName(role).getId();
        var retrievedPrivileges = privilegeService.findPrivilegesByRoleId(roleId);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(retrievedPrivileges),
                () -> Assertions.assertEquals(privileges.size(), retrievedPrivileges.size()),
                () -> Assertions.assertTrue(retrievedPrivileges.stream()
                        .allMatch(retrievedPrivilege -> privileges.stream()
                                .anyMatch(privilege -> Objects.equals(privilege.name(), retrievedPrivilege.getName()))))
        );
    }

    private static Stream<Arguments> roleAndPrivileges() {
        return Stream.of(
                Arguments.of("ROLE_USER",
                        List.of(PrivilegeEnum.IMAGE_UPLOAD_PRIVILEGE, PrivilegeEnum.IMAGE_DOWNLOAD_PRIVILEGE,
                                PrivilegeEnum.IMAGE_READ_PRIVILEGE, PrivilegeEnum.IMAGE_DELETE_PRIVILEGE,
                                PrivilegeEnum.FILTER_APPLY_PRIVILEGE, PrivilegeEnum.FILTER_READ_PRIVILEGE)),
                Arguments.of("ROLE_ADMIN",
                        List.of(PrivilegeEnum.IMAGE_UPLOAD_PRIVILEGE, PrivilegeEnum.IMAGE_DOWNLOAD_PRIVILEGE,
                                PrivilegeEnum.IMAGE_READ_PRIVILEGE, PrivilegeEnum.IMAGE_DELETE_PRIVILEGE,
                                PrivilegeEnum.FILTER_APPLY_PRIVILEGE, PrivilegeEnum.FILTER_READ_PRIVILEGE,
                                PrivilegeEnum.IMAGE_FULL_ACCESS_PRIVILEGE))
        );
    }

}
