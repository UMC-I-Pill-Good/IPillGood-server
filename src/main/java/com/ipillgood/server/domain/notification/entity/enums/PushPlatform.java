package com.ipillgood.server.domain.notification.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PushPlatform {
    WEB("웹");

    private final String label;
}
