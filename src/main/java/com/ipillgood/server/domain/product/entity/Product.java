package com.ipillgood.server.domain.product.entity;

import com.ipillgood.server.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product")
public class Product extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "image_key", nullable = false)
    private String imageKey;

    @Column(columnDefinition = "TEXT", name = "description", nullable = false)
    private String description;

    @Column(name = "purchase_url", nullable = false)
    private String purchaseUrl;

    @Column(name = "mfds_certified", nullable = false)
    private boolean mfdsCertified;
}
