package com.snap.bjd.packageimports;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class PackageProviderScanner {

    public static final String[] PACKAGED_MODULES = {
            "sc-framework",
            "sc-core",
    };

    static final List<String> MIGRATED_PACKAGES = Arrays.asList(
            "com.snapchat.soju.android",
            "com.snapchat.soju.android.gallery",
            "com.snapchat.soju.android.identity",
            "com.snapchat.android"
    );

    public static Set<String> getExistingImports(File root) throws IOException {

        Set<String> migrated = new HashSet<>();
        for (String module : PACKAGED_MODULES) {
            migrated.addAll(
                    new PackageProviderScanner().getPackagesForDirectory(new File(root, module))
            );
        }
        migrated.addAll(MIGRATED_PACKAGES);

        return migrated;
    }

    public Set<String> getPackagesForSourceTree(File file) throws IOException {
        if (file.isDirectory()) {
            return getPackagesForDirectory(file);
        } else {
            return Collections.singleton(getPackageForClass(file));
        }
    }

    public String getPackageForClass(File java) throws IOException {
        List<String> lines = Files.readAllLines(java.toPath());
        for (String line : lines) {
            if (line.startsWith("package ")) {
                String path = line.substring(8).replace(";", "");
                return path;
            }
        }
        throw new IOException("no package for " + java);
    }

    public Set<String> getPackagesForDirectory(File dir) throws IOException {
        Set<String> pkgs = new HashSet<>();

        String thisPackage = dir.getCanonicalPath();
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".java")) {
                pkgs.add(getPackageForClass(file));
            } else if (file.isDirectory()) {
                pkgs.addAll(getPackagesForDirectory(file));
            }
        }
        return pkgs;
    }
}
