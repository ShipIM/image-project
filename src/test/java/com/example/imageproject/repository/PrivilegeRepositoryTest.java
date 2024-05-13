import com.example.imageapi.api.repository.PrivilegeRepository;
import com.example.imageproject.config.BaseTest;
import com.example.imageproject.model.enumeration.PrivilegeEnum;
import com.example.imageproject.model.enumeration.RoleEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class PrivilegeRepositoryTest extends BaseTest {

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @ParameterizedTest
    @MethodSource("roleAndPrivileges")
    public void findPrivilegesByRoleId_PrivilegesExist(RoleEnum role, List<PrivilegeEnum> privileges) {
        var retrievedPrivileges = privilegeRepository.findPrivilegesByRole(role);

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
                Arguments.of(RoleEnum.ROLE_USER,
                        List.of(PrivilegeEnum.IMAGE_UPLOAD_PRIVILEGE, PrivilegeEnum.IMAGE_DOWNLOAD_PRIVILEGE,
                                PrivilegeEnum.IMAGE_READ_PRIVILEGE, PrivilegeEnum.IMAGE_DELETE_PRIVILEGE,
                                PrivilegeEnum.FILTER_APPLY_PRIVILEGE, PrivilegeEnum.FILTER_READ_PRIVILEGE)),
                Arguments.of(RoleEnum.ROLE_ADMIN,
                        List.of(PrivilegeEnum.IMAGE_UPLOAD_PRIVILEGE, PrivilegeEnum.IMAGE_DOWNLOAD_PRIVILEGE,
                                PrivilegeEnum.IMAGE_READ_PRIVILEGE, PrivilegeEnum.IMAGE_DELETE_PRIVILEGE,
                                PrivilegeEnum.FILTER_APPLY_PRIVILEGE, PrivilegeEnum.FILTER_READ_PRIVILEGE,
                                PrivilegeEnum.IMAGE_FULL_ACCESS_PRIVILEGE))
        );
    }

}
