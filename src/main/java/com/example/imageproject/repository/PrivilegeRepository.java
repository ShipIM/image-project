package com.example.imageproject.repository;

import com.example.imageproject.model.entity.Privilege;
import com.example.imageproject.model.enumeration.RoleEnum;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrivilegeRepository extends CrudRepository<Privilege, Long> {

    @Query("with recursive role_hierarchy as ( " +
            "select id, name " +
            "from role " +
            "where name = :role " +
            "union all " +
            "select rr.second_role_id as id, r.name " +
            "from role_hierarchy rh " +
            "join role_role rr on rh.id = rr.first_role_id " +
            "join role r on rr.second_role_id = r.id) " +
            "select p.id, p.name " +
            "from role_hierarchy rh " +
            "join role_privilege rp on rh.id = rp.role_id " +
            "join privilege p on rp.privilege_id = p.id")
    List<Privilege> findPrivilegesByRole(RoleEnum role);

}
