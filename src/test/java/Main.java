import ho.artisan.anno.core.Entry;
import ho.artisan.anno.core.FakeAnnotation;

public class Main {

    @Nio(4)
    public static final Entity ENTITY_0 = new Entity(101);

    @Nio(1)
    @Mio({
            @Nio(1),
            @Nio(2)
    })
    public static final Entity ENTITY_1 = new Entity(5);

    public static void main(String[] args) throws NoSuchFieldException {
        Entry entity = Entry.wrap(Main.class.getField("ENTITY_1"));

        Nio nio = entity.get(Nio.class);
        Mio mio = entity.get(Mio.class);

        Nio fake1 = FakeAnnotation.builder(Nio.class).fake("value", 1).build();
        Nio fake2 = FakeAnnotation.builder(Nio.class).value(2).build();

        Mio fake3 = FakeAnnotation.builder(Mio.class).value(new Nio[]{fake1, fake2}).build();

        print(mio);
        print(fake3);

        print(fake3.equals(mio));
    }

    public static <T> void print(T object) {
        System.out.println(object);
    }
}