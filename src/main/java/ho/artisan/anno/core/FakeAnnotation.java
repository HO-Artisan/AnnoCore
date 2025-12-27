package ho.artisan.anno.core;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public final class FakeAnnotation<A> implements InvocationHandler {
    private final Class<A> aClass;
    private final Map<String, Object> valueMap;

    private FakeAnnotation(Class<A> aClass, final Map<String, Object> valueMap) {
        this.aClass = aClass;
        this.valueMap = valueMap;
    }

    public static <A> Builder<A> builder(Class<A> aClass) {
        return new Builder<>(aClass);
    }

    public A fake() {
        return aClass.cast(Proxy.newProxyInstance(
                aClass.getClassLoader(),
                new Class[]{Annotation.class, aClass},
                this
        ));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();

        if (methodName.equals("equals"))
            return false;
        
        else if (methodName.equals("toString"))
            return formatAnnotation();

        else if (methodName.equals("hashCode"))
            return valueMap.hashCode();

        else if (methodName.equals("annotationType"))
            return aClass;

        else if (valueMap.containsKey(methodName))
            return valueMap.get(methodName);

        else
            throw new IllegalStateException("Method [" + methodName + "] not found in FakeAnnotation for " + aClass.getName());
    }

    private String formatAnnotation() {
        StringBuilder sb = new StringBuilder();
        sb.append('@').append(aClass.getName()).append('(');
        boolean first = true;
        boolean value = valueMap.size() == 1 && valueMap.containsKey("value");
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            if (!value)
                sb.append(entry.getKey()).append("=");
            sb.append(formatValue(entry.getValue()));
        }
        sb.append(')');
        return sb.toString();
    }

    private String formatValue(Object value) {
        switch (value) {
            case null -> {
                return "null";
            }
            case String s -> {
                return "'" + value + "'";
            }
            case Annotation annotation -> {
                return value.toString();
            }
            case Object[] array -> {
                StringBuilder arraySb = new StringBuilder("{");
                for (int i = 0; i < array.length; i++) {
                    if (i > 0) arraySb.append(", ");
                    arraySb.append(formatValue(array[i]));
                }
                arraySb.append("}");
                return arraySb.toString();
            }
            default -> {}
        }
        return value.toString();
    }

    public static class Builder<A> {
        private final Class<A> aClass;
        private final Map<String, Object> valueMap;

        private Builder(Class<A> aClass) {
            this.aClass = aClass;
            this.valueMap = new HashMap<>();
        }

        public <T> Builder<A> fake(@NotNull String key, @NotNull T value) {
            valueMap.put(key, value);
            return this;
        }

        public <T> Builder<A> value(T value) {
            return fake("value", value);
        }

        public A build() {
            return new FakeAnnotation<>(aClass, valueMap).fake();
        }
    }
}
