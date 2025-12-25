package ho.artisan.anno.exception;

import java.lang.annotation.Annotation;

public class AnnotationNotFoundException extends AnnotationException {

    public AnnotationNotFoundException(Class<? extends Annotation> annoClass) {
        super("Annotation instance not found in current context", annoClass, "get");
    }

    public AnnotationNotFoundException(Class<? extends Annotation> annoClass, String elementId) {
        super("Annotation not found on element [" + elementId + "]", annoClass, "get");
    }
}
