package diergo.junit5.dataprovider;

import org.junit.jupiter.api.function.Executable;

import java.lang.invoke.MethodHandle;

public final class DataProvided {
    private final String group;
    private final String name;
    private final MethodHandle handle;
    private final Object[] args;

    DataProvided(String group, String name, MethodHandle handle, Object... args) {
        this.group = group;
        this.name = name;
        this.handle = handle;
        this.args = args;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public Executable asExecutable() {
        return () -> handle.invokeWithArguments(args);
    }
}
