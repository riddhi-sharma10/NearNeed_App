package com.example.nearneed;

public class PaymentMethod {
    private String id;
    private String name;
    private String type; // "CARD", "WALLET", "BANK_TRANSFER"
    private String displayDetail;
    private boolean isDefault;
    private String iconName;

    public PaymentMethod(String id, String name, String type, String displayDetail, boolean isDefault, String iconName) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.displayDetail = displayDetail;
        this.isDefault = isDefault;
        this.iconName = iconName;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDisplayDetail() { return displayDetail; }
    public boolean isDefault() { return isDefault; }
    public String getIconName() { return iconName; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setDisplayDetail(String displayDetail) { this.displayDetail = displayDetail; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
    public void setIconName(String iconName) { this.iconName = iconName; }
}
