package ho.artisan.anno.exception;

import java.lang.annotation.Annotation;

public class AnnotationInvalidException extends AnnotationException {
    private final String invalidProperty;


    public AnnotationInvalidException(Class<? extends Annotation> annoClass, String reason) {
        super(reason, annoClass, "put");
        this.invalidProperty = null;
    }

    public AnnotationInvalidException(Class<? extends Annotation> annoClass, String invalidProperty, String reason) {
        super("Property [" + invalidProperty + "] is invalid: " + reason, annoClass, "put");
        this.invalidProperty = invalidProperty;
    }

    public String getInvalidProperty() {
        return invalidProperty;
    }
}
