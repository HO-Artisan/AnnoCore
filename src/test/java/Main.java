import ho.artisan.anno.core.Entry;
import ho.artisan.anno.core.Instance;

public class Main {

    @Nio(4)
    public static final Entity ENTITY_0 = new Entity(101);

    @Nio(6)
    @Mio({
            @Nio(1),
            @Nio(2)
    })
    public static final Entity ENTITY_1 = new Entity(5);

    public static void main(String[] args) throws NoSuchFieldException {
        Entry entity = Entry.wrap(Main.class.getField("ENTITY_1"));

        Nio nio = entity.get(Nio.class);
        Mio mio = entity.get(Mio.class);

        Instance instance = Instance.wrap(ENTITY_0, Entity.class);

        print(instance.invokers());
        print("-----------------");
        print(instance.invokers().findByName("getI").invoke());
        instance.invokers().findByName("setI").invoke(new Object[]{114});
        print(instance.invokers().findByName("getI").invoke());
    }

    public static <T> void print(T object) {
        System.out.println(object);
    }
}