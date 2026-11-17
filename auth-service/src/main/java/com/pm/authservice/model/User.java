package com.pm.authservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name="users")
public class User {
    @id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
}
