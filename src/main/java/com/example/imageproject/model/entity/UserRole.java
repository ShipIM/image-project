package com.example.imageproject.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "_user_role")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserRole {

    @Id
    private Long id;

    @Column("user_id")
    private Long user;

    @Column("role_id")
    private Long role;

}
