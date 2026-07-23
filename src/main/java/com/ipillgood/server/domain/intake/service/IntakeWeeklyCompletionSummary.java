package com.ipillgood.server.domain.intake.service;

// 특정 기간(7일)의 복용 완료/미완료 일수 집계. 복용 예정일이 없는 날은 완료·미완료 어느 쪽에도 포함되지 않는다.
public record IntakeWeeklyCompletionSummary(int completedDays, int missedDays) {
}
