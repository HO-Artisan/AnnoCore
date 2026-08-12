package ho.artisan.anno;

import ho.artisan.anno.core.AnnoCore;
import ho.artisan.anno.core.Property;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PropertyTest {

    static class Box {
        String label = "hello";
        int count = 0;
    }

    @Test
    void rawReadsLiveValue() throws Exception {
        Box box = new Box();
        Property prop = AnnoCore.property(box, Box.class.getDeclaredField("label"));

        assertEquals("hello", prop.raw());

        box.label = "world";
        assertEquals("world", prop.raw(), "raw() must read the field live");
    }

    @Test
    void setValueWritesToField() throws Exception {
        Box box = new Box();
        Property prop = AnnoCore.property(box, Box.class.getDeclaredField("label"));

        prop.setValue("changed");
        assertEquals("changed", box.label);
        assertEquals("changed", prop.raw());
    }

    @Test
    void asCastsLiveValue() throws Exception {
        Box box = new Box();
        Property prop = AnnoCore.property(box, Box.class.getDeclaredField("count"));

        assertEquals(0, prop.as(Integer.class));
        box.count = 42;
        assertEquals(42, prop.as(Integer.class));
    }

    @Test
    void isChecksLiveType() throws Exception {
        Box box = new Box();
        Property prop = AnnoCore.property(box, Box.class.getDeclaredField("label"));

        assertTrue(prop.is(String.class));
        assertFalse(prop.is(Integer.class));
    }

    @Test
    void matchesByName() throws Exception {
        Box box = new Box();
        Property prop = AnnoCore.property(box, Box.class.getDeclaredField("count"));

        assertTrue(prop.matches("count"));
        assertFalse(prop.matches("label"));
    }
}
