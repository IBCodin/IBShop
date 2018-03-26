package io.github.ibcodin.ibshop;

import java.text.MessageFormat;

public class SalesSummary {
    private String preferredName;
    private int quantity;

    public SalesSummary(SalesItem item) {
        this.preferredName = item.getPreferredName();
        this.quantity = item.getQuantity();
    }

    public void add(SalesItem item) {
        assert (item.getPreferredName().equals(this.preferredName));
        this.quantity += item.getQuantity();
    }

    public String getPreferredName() { return preferredName; }

    public int getQuantity() { return quantity; }

    public String toCSV() { return MessageFormat.format("{0},{1}\n", preferredName, quantity); }
}
