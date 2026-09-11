package sogang.cnu.backend.budget;

import java.util.List;

/**
 * 가계부 엑셀(.xlsx) 양식의 항목 순서/라벨 정의.
 * 총무가 쓰던 구글 시트의 '예산안' 탭 행 구성을 그대로 따른다.
 * 내보내기(BudgetExportService)와 추후 업로드 파서가 같은 정의를 공유하도록 한 곳에 모아둔다.
 */
public final class BudgetSheetLayout {

    private BudgetSheetLayout() {}

    public record CategoryRow(String label, BudgetCategory category) {}

    public record Group(String label, List<CategoryRow> rows) {}

    public static final Group INCOME = new Group("수입", List.of(
            new CategoryRow("1. 전월 이월금", BudgetCategory.INCOME_CARRYOVER),
            new CategoryRow("2. 월별 학회비", BudgetCategory.INCOME_MEMBERSHIP),
            new CategoryRow("3. 스터디 보증금", BudgetCategory.INCOME_STUDY_DEPOSIT),
            new CategoryRow("4. 기타", BudgetCategory.INCOME_OTHER)
    ));

    public static final List<Group> EXPENSE_GROUPS = List.of(
            new Group("1. 지원비", List.of(
                    new CategoryRow("1-1. 도서지원비", BudgetCategory.EXPENSE_BOOK_SUPPORT),
                    new CategoryRow("1-2. 서버지원비", BudgetCategory.EXPENSE_SERVER_SUPPORT),
                    new CategoryRow("1-3. 통합 지원비", BudgetCategory.EXPENSE_INTEGRATED_SUPPORT),
                    new CategoryRow("1-4. 개발 지원비", BudgetCategory.EXPENSE_DEV_SUPPORT),
                    new CategoryRow("1-5. 기타", BudgetCategory.EXPENSE_SUPPORT_OTHER)
            )),
            new Group("2. 프로젝트", List.of(
                    new CategoryRow("2-1. 프로젝트 지원비", BudgetCategory.EXPENSE_PROJECT_SUPPORT),
                    new CategoryRow("2-2. 멘토 보수", BudgetCategory.EXPENSE_MENTOR_FEE)
            )),
            new Group("3. 정기결제", List.of(
                    new CategoryRow("3-1. 지피티", BudgetCategory.EXPENSE_GPTEE),
                    new CategoryRow("3-2. 톡서랍", BudgetCategory.EXPENSE_TOKSRAP),
                    new CategoryRow("3-3. 디스코드", BudgetCategory.EXPENSE_DISCORD),
                    new CategoryRow("3-4. 구글 워크스페이스", BudgetCategory.EXPENSE_GOOGLE_WORKSPACE),
                    new CategoryRow("3-5. 기타", BudgetCategory.EXPENSE_SUBSCRIPTION_OTHER)
            )),
            new Group("4. 스터디", List.of(
                    new CategoryRow("4-1. 강의형 스터디 강의비", BudgetCategory.EXPENSE_LECTURE_FEE),
                    new CategoryRow("4-2. 스터디 보증금 환급", BudgetCategory.EXPENSE_STUDY_DEPOSIT_REFUND),
                    new CategoryRow("4-3. 인강 구매비", BudgetCategory.EXPENSE_ONLINE_COURSE),
                    new CategoryRow("4-4. 멘토 보수", BudgetCategory.EXPENSE_MENTOR_REWARD),
                    new CategoryRow("4-5. 기타", BudgetCategory.EXPENSE_STUDY_OTHER)
            )),
            new Group("5. 기타", List.of(
                    new CategoryRow("5-1. 엠티", BudgetCategory.EXPENSE_MT),
                    new CategoryRow("5-2. 개총/종총", BudgetCategory.EXPENSE_GENERAL_MEETING),
                    new CategoryRow("5-3. 관리자실 간식비", BudgetCategory.EXPENSE_OFFICE_SNACK),
                    new CategoryRow("5-4. 15% 환급비", BudgetCategory.EXPENSE_TAX_REFUND),
                    new CategoryRow("5-5. 기타", BudgetCategory.EXPENSE_OTHER)
            ))
    );
}
