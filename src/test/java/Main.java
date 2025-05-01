import ho.artisan.anno.core.FakeAnnotation;
import ho.artisan.anno.core.Registration;
import ho.artisan.anno.core.annotation.ID;
import ho.artisan.anno.core.annotation.Priority;

@ID("main")
public class Main {

    @ID("iron")
    @Nio(4)
    public static final Entity ENTITY_0 = new Entity(101);

    @ID("gold")
    @Priority(1)
    @Nio(1)
    @Mio({
            @Nio(1),
            @Nio(2)
    })
    public static final Entity ENTITY_1 = new Entity( 101);

    public static void main(String[] args) {
        Registration registration = Registration.wrap(Main.class);
        Nio nio_true = registration.entries().getFirst().get(Nio.class);
        Nio nio_fake = FakeAnnotation.builder(Nio.class).value(1).build();

    }

    public static <T> void print(T object) {
        System.out.println(object.toString());
    }
}