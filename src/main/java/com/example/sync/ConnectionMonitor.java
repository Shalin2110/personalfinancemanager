package com.example.sync;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ConnectionMonitor {
    private static ConnectionMonitor instance;
    private final StringProperty statusText = new SimpleStringProperty("Checking...");
    private final BooleanProperty isOnline = new SimpleBooleanProperty(false);
    private Thread monitorThread;
    private volatile boolean running = false;

    private ConnectionMonitor() {
        startMonitoring();
    }

    public static ConnectionMonitor getInstance() {
        if (instance == null) {
            instance = new ConnectionMonitor();
        }
        return instance;
    }

    public void startMonitoring() {
        if (monitorThread != null && monitorThread.isAlive()) {
            return;
        }

        running = true;
        monitorThread = new Thread(() -> {
            while (running) {
                try {
                    boolean online = ConnectivityChecker.isOracleOnline();

                    // Update on JavaFX Application Thread
                    Platform.runLater(() -> {
                        isOnline.set(online);
                        if (online) {
                            statusText.set("Online");
                        } else {
                            statusText.set("Offline");
                        }
                    });

                    // Check every 3 seconds
                    Thread.sleep(3000);

                } catch (InterruptedException e) {
                    System.out.println("Connection monitor interrupted");
                    break;
                } catch (Exception e) {
                    System.err.println("Monitor error: " + e.getMessage());
                }
            }
        });

        monitorThread.setDaemon(true); // Don't prevent JVM shutdown
        monitorThread.setName("ConnectionMonitor");
        monitorThread.start();
    }

    public void stopMonitoring() {
        running = false;
        if (monitorThread != null) {
            monitorThread.interrupt();
        }
    }

    // Property getters for binding
    public StringProperty statusTextProperty() {
        return statusText;
    }

    public BooleanProperty isOnlineProperty() {
        return isOnline;
    }

    public String getStatusText() {
        return statusText.get();
    }

    public boolean isOnline() {
        return isOnline.get();
    }
}