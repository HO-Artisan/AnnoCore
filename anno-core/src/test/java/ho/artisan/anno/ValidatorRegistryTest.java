package ho.artisan.anno;

import ho.artisan.anno.core.Anno;
import ho.artisan.anno.core.AnnoCore;
import ho.artisan.anno.core.FakeAnnotation;
import ho.artisan.anno.core.validator.AnnotationValidator;
import ho.artisan.anno.exception.AnnotationInvalidException;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorRegistryTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Even {
        int value();
    }

    static class EvenValidator implements AnnotationValidator<Even> {
        @Override
        public boolean validate(Even anno) {
            return anno.value() % 2 == 0;
        }

        @Override
        public Class<Even> supportAnnotationType() {
            return Even.class;
        }
    }

    @Test
    void validatorPassesWhenValid() {
        AnnoCore.registerValidator(new EvenValidator());

        Anno anno = AnnoCore.wrap(Object.class);
        anno.put(FakeAnnotation.builder(Even.class).value(2).build());

        assertDoesNotThrow(() -> AnnoCore.validate(anno));
    }

    @Test
    void validatorThrowsWhenInvalid() {
        AnnoCore.registerValidator(new EvenValidator());

        Anno anno = AnnoCore.wrap(Object.class);
        anno.put(FakeAnnotation.builder(Even.class).value(3).build());

        assertThrows(AnnotationInvalidException.class, () -> AnnoCore.validate(anno));
    }

    @Test
    void unregisteredAnnotationIsSkipped() {
        Anno anno = AnnoCore.wrap(Object.class);
        // @ID and @Priority are auto-added but no validators registered for them
        // (assuming EvenValidator was registered in another test)
        assertDoesNotThrow(() -> AnnoCore.validate(anno));
    }
}
