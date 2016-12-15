package com.snap.bjd.packageimports;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {

    static final String SNAPCHAT_ROOT = "/Users/benjamin.dodson/snap/android/snapchat/";

    static final String MAIN_SRC_DIR = "app/src/";


    static final String PATH_APP_SHARED = "/app/src/main/java/com/snapchat/android/app/shared";
    static final String PATH_APP_MODELS = "/app/src/main/java/com/snapchat/android/model";

    public static void main(String[] args) throws IOException {
        printPackageDependencies();
        //printPackageDependencies(PATH_APP_SHARED);
    }

    static void printPackageDependencies() throws IOException {

        final File root = new File(SNAPCHAT_ROOT);

        File output = new File("packages.txt");
	    Map<String, Set<String>> sourceTree =
                new PackageImportScanner().getImportsForSourceTree(new File(root, MAIN_SRC_DIR));

        Set<String> migrated = PackageProviderScanner.getExistingImports(root);

        FileWriter writer = new FileWriter(output);
        for (Map.Entry<String, Set<String>> pkg : sourceTree.entrySet()) {

            String path = pkg.getKey().replace(root.getAbsolutePath(), "");
            if (path.startsWith("test/")) {
                continue;
            }

            writer.append(path).append(":\n");
            int snapCount = 0;
            Set<String> pkgs = PackageImportScanner.getPackagesForClasses(pkg.getValue());
            List<String> sorted = new ArrayList<>(pkgs);
            Collections.sort(sorted);
            for (String im : sorted) {
                if (isSnapchatPackage(im) && !migrated.contains(im)) {
                    snapCount++;
                    writer.append("    ").append(im).append("\n");
                }
            }
            writer.append("(- " + snapCount + " -)\n");
            writer.append("\n");
        }
        writer.flush();
        writer.close();
    }

    static void printPackageDependencies(String base) throws IOException {
        final File root = new File(SNAPCHAT_ROOT);

        File output = new File("packages.txt");
        Map<String, Set<String>> sourceTree =
                new PackageImportScanner().getImportsForSourceTree(new File(root, MAIN_SRC_DIR));

        Set<String> migrated = PackageProviderScanner.getExistingImports(root);

        FileWriter writer = new FileWriter(output);
        writer.append(base).append(":\n");
        String basePkg = pathAsPackage(base);
        int snapCount = 0;

        Set<String> imports = new HashSet<>();
        for (Map.Entry<String, Set<String>> pkg : sourceTree.entrySet()) {

            String path = pkg.getKey().replace(root.getAbsolutePath(), "");

            if (!path.startsWith(base)) {
                continue;
            }

            imports.addAll(PackageImportScanner.getPackagesForClasses(pkg.getValue()));
        }

        List<String> sorted = new ArrayList<>(imports);
        Collections.sort(sorted);
        for (String im : sorted) {
            if (im.startsWith(basePkg)) {
                continue;
            }

            if (isSnapchatPackage(im) && !migrated.contains(im)) {
                snapCount++;
                writer.append("    ").append(im).append("\n");
            }
        }

        writer.append("(- " + snapCount + " -)\n");
        writer.append("\n");
        writer.flush();
        writer.close();
    }

    private static String pathAsPackage(String path) {
        // TODO hack-attack...
        return path.replace("/app/src/main/java/", "").replace("/", ".");
    }

    static boolean isSnapchatPackage(String pkg) {
        return pkg.startsWith("com.snap");
    }

    static boolean isJavaPackage(String pkg) {
        return pkg.startsWith("java.");
    }

    static boolean isAndroidPackage(String pkg) {
        return pkg.startsWith("android.");
    }

    static boolean isExternalPackage(String pkg) {
        return !isSnapchatPackage(pkg) && !isJavaPackage(pkg) && !isAndroidPackage(pkg);
    }
}
