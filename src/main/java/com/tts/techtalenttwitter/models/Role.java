package com.tts.techtalenttwitter.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author Wes Lanning
 * @version 2019-11-25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "role_id")
    private Long id;

    @Enumerated
    @Column(columnDefinition = "integer")
    private RoleType role;

    public enum RoleType {
        USER,
        ADMIN,
        GUEST
    }
}


