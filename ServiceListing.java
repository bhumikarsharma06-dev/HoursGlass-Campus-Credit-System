package com.hourglass.model;

public class ServiceListing {
    private final long id;
    private final String title;
    private final String category;
    private final int durationHours;
    private final String mode;
    private final String description;
    private final String providerName;

    public ServiceListing(long id, String title, String category, int durationHours, String mode, String description, String providerName) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.durationHours = durationHours;
        this.mode = mode;
        this.description = description;
        this.providerName = providerName;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public int getDurationHours() { return durationHours; }
    public String getMode() { return mode; }
    public String getDescription() { return description; }
    public String getProviderName() { return providerName; }
}
