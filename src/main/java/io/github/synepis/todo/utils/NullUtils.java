package io.github.synepis.todo.utils;

public class NullUtils {
    public static<T> T firstNonNull(T first, T second) {
        return first != null ? first : second;
    }
}
