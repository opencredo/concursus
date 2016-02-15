package com.opencredo.concourse.domain;

import com.google.common.base.Preconditions;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VersionedName {

    public static VersionedName parse(String formatted) {
        checkNotNull(formatted, "formatted must not be null");

        String[] parts = formatted.split("_", 2);

        Preconditions.checkArgument(parts.length == 2);
        return of(parts[0], parts[1]);
    }

    public static VersionedName of(String name) {
        return of(name, "0");
    }

    public static VersionedName of(String name, String version) {
        checkNotNull(name, "name must not be null");
        checkNotNull(version, "version must not be null");

        return new VersionedName(name, version);
    }

    private final String name;
    private final String version;

    private VersionedName(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getFormatted() {
        return name + "_" + version;
    }

    @Override
    public boolean equals(Object o) {
        return this == o ||
                (o instanceof VersionedName
                    && ((VersionedName) o).name.equals(name)
                    && ((VersionedName) o).version.equals(version));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }

    @Override
    public String toString() {
        return getFormatted();
    }
}
