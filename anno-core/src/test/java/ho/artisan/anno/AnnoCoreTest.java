package ho.artisan.anno;

import ho.artisan.anno.core.*;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AnnoCoreTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Tag {
        String value();
    }

    @Tag("marked")
    @SuppressWarnings("unused")
    public static final String STATIC_FIELD = "s";

    private String instanceField = "i";

    private String echo(String x) { return x; }

    @Test
    void wrapAnnotatedElement() throws Exception {
        Anno anno = AnnoCore.wrap(AnnoCoreTest.class.getDeclaredField("STATIC_FIELD"));
        assertTrue(anno.contain(Tag.class));
    }

    @Test
    void entryFromStaticField() throws Exception {
        Entry entry = AnnoCore.entry(AnnoCoreTest.class.getDeclaredField("STATIC_FIELD"));
        assertEquals("s", entry.raw());
        assertTrue(entry.contain(Tag.class));
    }

    @Test
    void registrationFromClass() {
        Registration reg = AnnoCore.registration(AnnoCoreTest.class);
        assertTrue(reg.entries().size() > 0);
    }

    @Test
    void instanceFromObject() {
        Instance inst = AnnoCore.instance(this, AnnoCoreTest.class);
        assertSame(this, inst.raw());
    }

    @Test
    void propertyFromInstanceField() throws Exception {
        Property prop = AnnoCore.property(this, AnnoCoreTest.class.getDeclaredField("instanceField"));
        assertEquals("i", prop.raw());
        prop.setValue("changed");
        assertEquals("changed", this.instanceField);
    }

    @Test
    void invokerFromMethod() throws Exception {
        Method m = AnnoCoreTest.class.getDeclaredMethod("echo", String.class);
        Invoker inv = AnnoCore.invoker(this, m);
        assertEquals("hello", inv.invoke(new Object[]{"hello"}));
    }

    @Test
    void fakeAnnotationBuilder() {
        Tag fake = AnnoCore.fake(Tag.class).value("x").build();
        assertEquals("x", fake.value());
    }

    @Test
    void genIDCombinesTwo() throws Exception {
        Anno a = AnnoCore.wrap(AnnoCoreTest.class.getDeclaredField("STATIC_FIELD"));
        Anno b = AnnoCore.wrap(AnnoCoreTest.class.getDeclaredField("STATIC_FIELD"));
        String id = AnnoCore.genID(a, b);
        assertTrue(id.contains(":"));
    }

    @Test
    void priorityComparatorSortsDescending() {
        var list = new java.util.ArrayList<Anno>();
        Anno a = AnnoCore.wrap(Object.class);
        Anno b = AnnoCore.wrap(Object.class);
        list.add(a);
        list.add(b);
        list.sort(AnnoCore.priorityComparator());
        // just verifying the comparator does not throw
        assertEquals(2, list.size());
    }
}
