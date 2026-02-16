package sogang.cnu.backend.application;



public enum ApplicationStatus {
    APPLIED,        // 지원 완료
    IN_PROGRESS,   // 진행 중
    PASSED,        // 통과
    REJECTED,      // 탈락
    WAITING,       // 대기
    CANCELED,      // 지원자 취소
    HOLD           // 보류
}