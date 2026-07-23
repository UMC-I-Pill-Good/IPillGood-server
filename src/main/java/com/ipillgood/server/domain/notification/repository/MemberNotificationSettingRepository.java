package com.ipillgood.server.domain.notification.repository;

import com.ipillgood.server.domain.notification.entity.MemberNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberNotificationSettingRepository extends JpaRepository<MemberNotificationSetting, Long> {
}
