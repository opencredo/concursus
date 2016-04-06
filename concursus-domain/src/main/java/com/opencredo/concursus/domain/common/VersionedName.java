package com.opencredo.concursus.domain.common;

import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A name/version combination.
 */
public final class VersionedName implements Serializable {

    /**
     * Create a {@link VersionedName} with the supplied name and version "0".
     * @param name The name of the {@link VersionedName}.
     * @return The constructed {@link VersionedName}.
     */
    public static VersionedName of(String name) {
        return of(name, "0");
    }


    /**
     * Create a {@link VersionedName} with the supplied name and version.
     * @param name The name of the {@link VersionedName}.
     * @param version The version of the {@link VersionedName}.
     * @return The constructed {@link VersionedName}.
     */
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

    /**
     * Get the "name" part of the {@link VersionedName}.
     * @return The "name" part of the {@link VersionedName}.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the "version" part of the {@link VersionedName}.
     * @return The "version" part of the {@link VersionedName}.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the {@link VersionedName} formatted as a single string.
     * @return The name and version concatenated with an underscore separator.
     */
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
