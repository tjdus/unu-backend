# CNU&U Backend

서강대학교 학회 운영·활동 관리 서비스 **CNU&U**의 백엔드입니다.
학회원 관리, 활동 모집·신청, 출석, 공지, 예산 관리 API를 제공합니다.

## 기술 스택

- Java 21, Spring Boot 3.5
- Spring Data JPA, PostgreSQL
- Spring Security (JWT, `@PreAuthorize` 권한 제어)
- Apache POI (예산안 엑셀 내보내기·업로드)



## 주요 도메인

| 패키지 | 내용 |
|---|---|
| `activity`, `activity_participant` | 활동 개설, 모집, 신청, 수료 |
| `budget` | 월별 예산안(예상·실제), 스터디 보증금 원장, 지출 건별 내역, 엑셀 내보내기·업로드 |
| `applicant_notification` | 운영진·담당자용 신청자 알림 |
