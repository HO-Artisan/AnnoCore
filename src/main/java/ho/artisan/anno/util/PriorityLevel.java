package ho.artisan.anno.util;

public enum PriorityLevel {
    LOW(1), MEDIUM(5), HIGH(10);
    private final int value;
    PriorityLevel(int value) { this.value = value; }
    public int value() { return value; }
}
