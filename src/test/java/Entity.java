public class Entity {
    private int i;

    public Entity(int i) {
        this.i = i;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "i=" + getI() +
                '}';
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }
}
