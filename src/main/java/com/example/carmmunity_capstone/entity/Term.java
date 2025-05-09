package com.example.carmmunity_capstone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "Term")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Term {

    @Id
    @Column(name = "Term_Code", length = 10)
    private String termCode;

    @Column(name = "Term_Content", nullable = false, columnDefinition = "TEXT")
    private String termContent;
}