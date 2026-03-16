package sogang.cnu.backend.quarter;

public enum Season {
    WINTER(0), SPRING(1), SUMMER(2), FALL(3), PRECEDENCE(-1);

    private final int order;

    Season(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}