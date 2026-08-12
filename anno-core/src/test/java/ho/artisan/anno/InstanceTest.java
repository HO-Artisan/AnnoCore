package ho.artisan.anno;

import ho.artisan.anno.core.AnnoCore;
import ho.artisan.anno.core.AnnoList;
import ho.artisan.anno.core.Instance;
import ho.artisan.anno.core.Invoker;
import ho.artisan.anno.core.Property;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InstanceTest {

    @SuppressWarnings("unused")
    static class Sample {
        private String name = "test";
        int value = 100;

        public String greet() { return "hi"; }
        void noop() {}
    }

    @Test
    void propertiesLazilyLoaded() {
        Instance inst = AnnoCore.instance(new Sample(), Sample.class);
        AnnoList<?> props = inst.properties();

        assertNotNull(props);
        assertEquals(2, props.size());
        assertTrue(props.getAll().stream().anyMatch(p -> ((Property) p).matches("name")));
        assertTrue(props.getAll().stream().anyMatch(p -> ((Property) p).matches("value")));
    }

    @Test
    void invokersLazilyLoaded() {
        Instance inst = AnnoCore.instance(new Sample(), Sample.class);
        AnnoList<?> invs = inst.invokers();

        assertNotNull(invs);
        assertTrue(invs.getAll().stream().anyMatch(i -> ((Invoker) i).matches("greet")));
        assertTrue(invs.getAll().stream().anyMatch(i -> ((Invoker) i).matches("noop")));
    }

    @Test
    void rawAndAs() {
        Sample s = new Sample();
        Instance inst = AnnoCore.instance(s, Sample.class);

        assertSame(s, inst.raw());
        assertSame(s, inst.as(Sample.class));
    }

    @Test
    void isTypeCheck() {
        Instance inst = AnnoCore.instance(new Sample(), Sample.class);

        assertTrue(inst.is(Sample.class));
        assertFalse(inst.is(String.class));
    }

    @Test
    void matchesByClassName() {
        Instance inst = AnnoCore.instance(new Sample(), Sample.class);

        assertTrue(inst.matches(Sample.class.getName()));
    }
}
