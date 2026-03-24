package sogang.cnu.backend.budget;

/**
 * 예산 항목 카테고리
 * INCOME: 수입
 * EXPENSE_*: 지출 세부 카테고리
 */
public enum BudgetCategory {
    // 수입
    INCOME_CARRYOVER,       // 전월 이월금
    INCOME_MEMBERSHIP,      // 월별 학회비
    INCOME_STUDY_DEPOSIT,   // 스터디 보증금
    INCOME_OTHER,           // 기타 수입 (통장이자 등)

    // 지출 - 지원비
    EXPENSE_BOOK_SUPPORT,       // 도서지원비
    EXPENSE_SERVER_SUPPORT,     // 서버지원비
    EXPENSE_INTEGRATED_SUPPORT, // 통합 지원비
    EXPENSE_DEV_SUPPORT,        // 개발 지원비 (학회원 지원비 > 개발 지원비)
    EXPENSE_SUPPORT_OTHER,      // 학회원 지원비 > 기타

    // 지출 - 프로젝트
    EXPENSE_PROJECT_SUPPORT,    // 프로젝트 지원비
    EXPENSE_MENTOR_FEE,         // 멘토 보수

    // 지출 - 정기결제
    EXPENSE_GPTEE,              // 지피티
    EXPENSE_TOKSRAP,            // 톡서랍
    EXPENSE_DISCORD,            // 디스코드
    EXPENSE_GOOGLE_WORKSPACE,   // 구글 워크스페이스
    EXPENSE_SUBSCRIPTION_OTHER, // 정기결제 기타

    // 지출 - 스터디
    EXPENSE_LECTURE_FEE,        // 강의형 스터디 강의비
    EXPENSE_STUDY_DEPOSIT_REFUND, // 스터디 보증금 환급
    EXPENSE_ONLINE_COURSE,      // 인강 구매비
    EXPENSE_MENTOR_REWARD,      // 멘토 보수 (스터디)
    EXPENSE_STUDY_OTHER,        // 스터디 기타

    // 지출 - 기타
    EXPENSE_MT,                 // 엠티
    EXPENSE_GENERAL_MEETING,    // 개총/종총
    EXPENSE_OFFICE_SNACK,       // 관리자실 간식비
    EXPENSE_TAX_REFUND,         // 15% 환급비
    EXPENSE_OTHER               // 기타
}
