package com.example.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupManager {

    public static boolean backupSQLiteDatabase() {
        try {
            Path backupDir = Paths.get("backups");
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            // Create timestamped backup filename
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Path source = Paths.get("database/personalfinanceDB.db");
            Path backup = backupDir.resolve("personalfinance_backup_" + timestamp + ".db");

            // Perform the backup
            Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Backup created: " + backup.getFileName());
            return true;

        } catch (IOException e) {
            System.err.println("❌ Backup failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean restoreSQLiteDatabase(String backupFilename) {
        try {
            Path backup = Paths.get("backups/" + backupFilename);
            Path destination = Paths.get("database/personalfinanceDB.db");

            // Ensure backup exists
            if (!Files.exists(backup)) {
                System.err.println("❌ Backup file not found: " + backupFilename);
                return false;
            }

            // Perform restore
            Files.copy(backup, destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Database restored from: " + backupFilename);
            return true;

        } catch (IOException e) {
            System.err.println("❌ Restore failed: " + e.getMessage());
            return false;
        }
    }

    public static String[] getAvailableBackups() {
        try {
            return Files.list(Paths.get("backups"))
                    .filter(path -> path.toString().endsWith(".db"))
                    .map(path -> path.getFileName().toString())
                    .toArray(String[]::new);
        } catch (IOException e) {
            System.err.println("❌ Could not list backups: " + e.getMessage());
            return new String[0];
        }
    }
}