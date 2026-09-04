package com.hourglass.model;

public class ServiceRequest {
    private final long id;
    private final String serviceTitle;
    private final String providerName;
    private final String status;

    public ServiceRequest(long id, String serviceTitle, String providerName, String status) {
        this.id = id;
        this.serviceTitle = serviceTitle;
        this.providerName = providerName;
        this.status = status;
    }

    public long getId() { return id; }
    public String getServiceTitle() { return serviceTitle; }
    public String getProviderName() { return providerName; }
    public String getStatus() { return status; }
}
