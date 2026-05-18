/*package com.example.freeze;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class StorageManager {
    // Adding the 'L' to ensure it handles the large number correctly
    private static final long MAX_STORAGE_BYTES = 1024L * 1024 * 1024;
    private static final long ONE_MONTH = 30L * 24 * 60 * 60 * 1000;

    public void autoCleanJunk(File internalDir) {
        if (internalDir == null || !internalDir.exists()) return;

        File[] files = internalDir.listFiles();
        if (files == null) return;

        // Sort files by date (Oldest first)
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        long currentFolderFiles = 0;
        for (File file : files) {
            if (file.isFile()) {
                currentFolderFiles += file.length();
            }
        }

        // Only start cleaning if the TOTAL size is too big
        if (currentFolderFiles > MAX_STORAGE_BYTES) {
            // We "Pass" the files array to the next method so it can see them
            performCleanup(files);
        }
    }

    // Added (File[] files) here so this method has access to the list
    private void performCleanup(File[] files) {
        long currentTime = System.currentTimeMillis();

        for (File file : files) {
            long fileAge = currentTime - file.lastModified();

            // The "if" must be INSIDE the for-loop so it checks every file
            if (fileAge > ONE_MONTH) {
                if (file.delete()) {
                    System.out.println("Cleaned up old junk: " + file.getName());
                }
            }
        }
    }
}*/