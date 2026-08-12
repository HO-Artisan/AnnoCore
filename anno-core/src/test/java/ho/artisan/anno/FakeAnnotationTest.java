package ho.artisan.anno;

import ho.artisan.anno.core.FakeAnnotation;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FakeAnnotationTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Single {
        int value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Multi {
        String name();
        int[] tags();
    }

    @Single(42)
    static final class Holder {}

    @Test
    void fakeEqualsRealAnnotationWithSameValue() {
        Single real = Holder.class.getAnnotation(Single.class);
        Single fake = FakeAnnotation.builder(Single.class).value(42).build();

        assertEquals(real, fake, "fake should equal real annotation");
        assertEquals(fake, real, "equals must be symmetric");
        assertEquals(real.hashCode(), fake.hashCode(), "hashCode must match the JLS contract");
    }

    @Test
    void selfIsEqual() {
        Single fake = FakeAnnotation.builder(Single.class).value(1).build();
        assertEquals(fake, fake);
    }

    @Test
    void differentValuesAreNotEqual() {
        Single a = FakeAnnotation.builder(Single.class).value(1).build();
        Single b = FakeAnnotation.builder(Single.class).value(2).build();
        assertNotEquals(a, b);
    }

    @Test
    void equalFakesShareHashCode() {
        Single a = FakeAnnotation.builder(Single.class).value(99).build();
        Single b = FakeAnnotation.builder(Single.class).value(99).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void notEqualToNullOrOtherType() {
        Single fake = FakeAnnotation.builder(Single.class).value(1).build();
        assertNotEquals(null, fake);
        assertNotEquals("not an annotation", fake);
    }

    @Test
    void arrayMembersCompareByValue() {
        Multi a = FakeAnnotation.builder(Multi.class)
                .fake("name", "x")
                .fake("tags", new int[]{1, 2, 3})
                .build();
        Multi b = FakeAnnotation.builder(Multi.class)
                .fake("name", "x")
                .fake("tags", new int[]{1, 2, 3})
                .build();

        assertEquals(a, b, "array members must compare element-wise");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void annotationTypeReturnsRealClass() {
        Single fake = FakeAnnotation.builder(Single.class).value(1).build();
        assertEquals(Single.class, fake.annotationType());
    }
}
