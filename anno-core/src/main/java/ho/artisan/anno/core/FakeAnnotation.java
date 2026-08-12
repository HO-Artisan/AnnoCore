package ho.artisan.anno.core;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class FakeAnnotation<A extends Annotation> implements InvocationHandler {
    private final Class<A> aClass;
    private final Map<String, Object> valueMap;

    private FakeAnnotation(Class<A> aClass, final Map<String, Object> valueMap) {
        this.aClass = aClass;
        this.valueMap = valueMap;
    }

    public static <A extends Annotation> Builder<A> builder(Class<A> aClass) {
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
            return annotationEquals(args[0]);

        else if (methodName.equals("toString"))
            return formatAnnotation();

        else if (methodName.equals("hashCode"))
            return annotationHashCode();

        else if (methodName.equals("annotationType"))
            return aClass;

        else if (valueMap.containsKey(methodName))
            return valueMap.get(methodName);

        else
            throw new IllegalStateException("Method [" + methodName + "] not found in FakeAnnotation for " + aClass.getName());
    }

    private boolean annotationEquals(Object other) {
        if (other == null || !aClass.isInstance(other))
            return false;
        for (Method member : aClass.getDeclaredMethods()) {
            Object mine = valueMap.get(member.getName());
            Object theirs;
            try {
                member.setAccessible(true);
                theirs = member.invoke(other);
            } catch (ReflectiveOperationException e) {
                return false;
            }
            if (!memberValueEquals(mine, theirs))
                return false;
        }
        return true;
    }

    private static boolean memberValueEquals(Object a, Object b) {
        if (a instanceof Object[] && b instanceof Object[])
            return Arrays.equals((Object[]) a, (Object[]) b);
        if (a instanceof boolean[] && b instanceof boolean[])
            return Arrays.equals((boolean[]) a, (boolean[]) b);
        if (a instanceof byte[] && b instanceof byte[])
            return Arrays.equals((byte[]) a, (byte[]) b);
        if (a instanceof char[] && b instanceof char[])
            return Arrays.equals((char[]) a, (char[]) b);
        if (a instanceof short[] && b instanceof short[])
            return Arrays.equals((short[]) a, (short[]) b);
        if (a instanceof int[] && b instanceof int[])
            return Arrays.equals((int[]) a, (int[]) b);
        if (a instanceof long[] && b instanceof long[])
            return Arrays.equals((long[]) a, (long[]) b);
        if (a instanceof float[] && b instanceof float[])
            return Arrays.equals((float[]) a, (float[]) b);
        if (a instanceof double[] && b instanceof double[])
            return Arrays.equals((double[]) a, (double[]) b);
        return Objects.equals(a, b);
    }

    private int annotationHashCode() {
        int result = 0;
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            result += (127 * entry.getKey().hashCode()) ^ memberValueHashCode(entry.getValue());
        }
        return result;
    }

    private static int memberValueHashCode(Object value) {
        return switch (value) {
            case null -> 0;
            case Object[] array -> Arrays.hashCode(array);
            case boolean[] array -> Arrays.hashCode(array);
            case byte[] array -> Arrays.hashCode(array);
            case char[] array -> Arrays.hashCode(array);
            case short[] array -> Arrays.hashCode(array);
            case int[] array -> Arrays.hashCode(array);
            case long[] array -> Arrays.hashCode(array);
            case float[] array -> Arrays.hashCode(array);
            case double[] array -> Arrays.hashCode(array);
            default -> value.hashCode();
        };
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

    public static class Builder<A extends Annotation> {
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
