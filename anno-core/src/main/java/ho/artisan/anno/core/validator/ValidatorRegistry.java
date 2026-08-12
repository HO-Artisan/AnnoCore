package ho.artisan.anno.core.validator;

import ho.artisan.anno.core.Anno;
import ho.artisan.anno.exception.AnnotationInvalidException;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public final class ValidatorRegistry {
    private static final Map<Class<? extends Annotation>, AnnotationValidator<?>> validators = new ConcurrentHashMap<>();
    private static volatile boolean serviceLoaded;

    private ValidatorRegistry() {}

    public static <A extends Annotation> void register(AnnotationValidator<A> validator) {
        validators.put(validator.supportAnnotationType(), validator);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void validate(Anno anno) throws AnnotationInvalidException {
        ensureServiceLoaded();
        for (Annotation annotation : anno.annotations()) {
            AnnotationValidator validator = (AnnotationValidator) validators.get(annotation.annotationType());
            if (validator != null && !validator.validate(annotation)) {
                throw new AnnotationInvalidException(annotation.annotationType(), "validation failed");
            }
        }
    }

    private static void ensureServiceLoaded() {
        if (serviceLoaded) return;
        synchronized (ValidatorRegistry.class) {
            if (serviceLoaded) return;
            for (AnnotationValidator<?> validator : ServiceLoader.load(AnnotationValidator.class)) {
                register(validator);
            }
            serviceLoaded = true;
        }
    }
}
