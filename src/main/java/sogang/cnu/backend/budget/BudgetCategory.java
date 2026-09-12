package sogang.cnu.backend.budget;

import java.util.EnumSet;
import java.util.Set;

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
    EXPENSE_GPTEE,              // LLM (예전 이름: 지피티)
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
    EXPENSE_OTHER;              // 기타

    /** 보증금 원장에서 파생되는 카테고리 (참여자 신청/수료 기록이 원천) */
    public static final Set<BudgetCategory> DEPOSIT_CATEGORIES = EnumSet.of(
            INCOME_STUDY_DEPOSIT,
            EXPENSE_STUDY_DEPOSIT_REFUND
    );

    /**
     * 지출 건별 상세 내역(BudgetExpenseEntry)으로 적을 수 있는 카테고리.
     * 그 달에 내역이 하나라도 있으면 월 금액은 내역 합계로 계산되고(직접 수정 불가),
     * 내역이 없으면 지금까지처럼 월 금액을 직접 입력한다.
     */
    public static final Set<BudgetCategory> DETAIL_MANAGED_CATEGORIES = EnumSet.of(
            // 총무 시트 '수입지출 총액'의 구역들
            EXPENSE_ONLINE_COURSE,      // 인터넷 강의
            EXPENSE_MT,                 // MT
            EXPENSE_GENERAL_MEETING,    // 개총&종총
            EXPENSE_OFFICE_SNACK,       // 기타비용 (간식·소모품)
            EXPENSE_OTHER,              // 기타비용
            // 시트 '실거래'에 건별로 적히던 항목들
            EXPENSE_GPTEE,
            EXPENSE_TOKSRAP,
            EXPENSE_DISCORD,
            EXPENSE_GOOGLE_WORKSPACE,
            EXPENSE_SUBSCRIPTION_OTHER,
            EXPENSE_BOOK_SUPPORT,
            EXPENSE_SERVER_SUPPORT,
            EXPENSE_INTEGRATED_SUPPORT,
            EXPENSE_DEV_SUPPORT,
            EXPENSE_SUPPORT_OTHER
    );

    public boolean isExpense() {
        return name().startsWith("EXPENSE_");
    }
}
