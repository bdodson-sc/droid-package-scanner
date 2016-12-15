package com.snap.bjd.packageimports;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class PackageImportScanner {

    public  Map<String, Set<String>> getImportsForSourceTree(File file) throws IOException {
        if (file.isDirectory()) {
            return getImportsForPackage(file);
        } else {
            return Collections.singletonMap(file.getCanonicalPath(), getImportsForClass(file));
        }
    }

    public Set<String> getImportsForClass(File java) throws IOException {

        final Set<String> imports = new HashSet<>();

        List<String> lines = Files.readAllLines(java.toPath());
        for (String line : lines) {
            if (line.startsWith("import ")) {
                String path = line.substring(7).replace(";", "");
                int innerClassNameIndex = -1;
                int outerClassNameIndex = -1;
                for (int i = 0; i < path.length(); i++) {
                    char c = path.charAt(i);
                    if ('A' <= c && c <= 'Z') {
                        if (outerClassNameIndex == -1) {
                            outerClassNameIndex = i;
                        } else {
                            innerClassNameIndex = i;
                            break;
                        }
                    }
                }
                if (innerClassNameIndex > 0) {
                    path = path.substring(0, innerClassNameIndex - 1);
                }
                imports.add(path);
            }
        }

        return imports;
    }

    public static Set<String> getPackagesForClasses(Set<String> classpaths) {
        final Set<String> pkgs = new HashSet<>();
        for (String clz : classpaths) {
            pkgs.add(clz.substring(0, clz.lastIndexOf(".")));
        }
        return pkgs;
    }

    public Map<String, Set<String>> getImportsForPackage(File dir) throws IOException {
        Map<String, Set<String>> imparts = new LinkedHashMap<>();

        String thisPackage = dir.getCanonicalPath();
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".java")) {
                imparts.put(thisPackage, getImportsForClass(file));
            } else if (file.isDirectory()) {
                imparts.putAll(getImportsForPackage(file));
            }
        }
        return imparts;
    }
}
