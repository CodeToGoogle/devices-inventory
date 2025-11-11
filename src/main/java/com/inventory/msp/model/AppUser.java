package com.inventory.msp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;          // encrypted

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String agencyName;        // only for agency accounts
}
