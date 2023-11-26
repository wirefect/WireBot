/*
 * ServerWrecker
 *
 * Copyright (C) 2023 ServerWrecker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package net.pistonmaster.serverwrecker.util;

import lombok.Getter;
import net.lenni0451.reflect.Methods;
import net.lenni0451.reflect.exceptions.MethodInvocationException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SWContextClassLoader extends ClassLoader {
    @Getter
    private final List<ClassLoader> childClassLoaders = new ArrayList<>();
    private final Method findLoadedClassMethod = Objects.requireNonNull(Methods.getDeclaredMethod(ClassLoader.class, "loadClass", String.class, boolean.class));
    private final ClassLoader platformClassLoader = ClassLoader.getSystemClassLoader().getParent();
    private final ClassLoader parent;

    public SWContextClassLoader() {
        this(ClassLoader.getSystemClassLoader());
    }

    private SWContextClassLoader(ClassLoader parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    return Methods.invoke(platformClassLoader, findLoadedClassMethod, name, resolve);
                } catch (MethodInvocationException ignored) {
                }

                var classData = loadClassData(parent, name);
                if (classData == null) {
                    // Check if child class loaders can load the class
                    for (ClassLoader childClassLoader : childClassLoaders) {
                        try {
                            var pluginClass = (Class<?>) Methods.invoke(childClassLoader, findLoadedClassMethod, name, resolve);
                            if (pluginClass != null) {
                                return pluginClass;
                            }
                        } catch (MethodInvocationException ignored) {
                        }
                    }

                    throw new ClassNotFoundException(name);
                }

                c = defineClass(name, classData, 0, classData.length);
            }

            if (resolve) {
                resolveClass(c);
            }

            return c;
        }
    }

    private byte[] loadClassData(ClassLoader classLoader, String className) {
        var classPath = className.replace('.', '/') + ".class";

        try (InputStream inputStream = classLoader.getResourceAsStream(classPath)) {
            if (inputStream == null) {
                return null;
            }

            return inputStream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }
}