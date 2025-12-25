package ho.artisan.anno.exception;

import java.lang.annotation.Annotation;

public class AnnotationException extends RuntimeException {
    private final Class<? extends Annotation> annoClass;
    private final String operation;

    public AnnotationException(String message, Class<? extends Annotation> annoClass, String operation) {
        super(message);
        this.annoClass = annoClass;
        this.operation = operation;
    }

    public AnnotationException(String message, Throwable cause, Class<? extends Annotation> annoClass, String operation) {
        super(message, cause);
        this.annoClass = annoClass;
        this.operation = operation;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Annotation operation [").append(operation).append("] failed");
        if (annoClass != null) {
            sb.append(" for annotation [").append(annoClass.getName()).append("]");
        }
        sb.append(": ").append(super.getMessage());
        return sb.toString();
    }

    public Class<? extends Annotation> getAnnoClass() {
        return annoClass;
    }

    public String getOperation() {
        return operation;
    }
}
