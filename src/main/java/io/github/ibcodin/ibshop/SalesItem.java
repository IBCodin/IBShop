package io.github.ibcodin.ibshop;

import org.bukkit.OfflinePlayer;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

public class SalesItem {

    public static final Comparator<SalesItem> PRICE_ORDER = Comparator.comparingDouble(o -> o.eachPrice);
    private static final Pattern csvSplit = Pattern.compile("\\s*,\\s*");
    private static final SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String preferredName;
    private int quantity;
    private double eachPrice;
    private UUID sellingPlayerId;
    private Date lastListing;
    private Date lastSale;

    public SalesItem(String preferredName,
                     int quantity,
                     double eachPrice,
                     OfflinePlayer seller) {
        this.preferredName = preferredName;
        this.quantity = quantity;
        this.eachPrice = eachPrice;
        this.sellingPlayerId = seller.getUniqueId();
        this.lastListing = new Date();
        this.lastSale = null;
    }

    public int addStock(int quantity) {
        assert(quantity >= 0);
        this.quantity += quantity;
        return this.quantity;
    }

    public int removeStock(int quantity) {
        assert(quantity >= 0);
        assert(quantity <= this.quantity);
        this.quantity -= quantity;
        return this.quantity;
    }

    public void touchListingDate() {
        this.lastListing = new Date();
    }

    public void touchSaleDate() {
        this.lastSale = new Date();
    }

    public static SalesItem reparseCSV(final String line) {
        String[] fields = csvSplit.split(line);

        if (fields.length < 5) {
            System.out.println("SalesItem.reparseCSV: only found " + fields.length + " fields");
            return null;
        }

        return new SalesItem(
                fields[0],
                fields[1],
                fields[2],
                fields[3],
                fields[4],
                (fields.length > 5) ? fields[5] : ""
        );
    }

    public String toCSV() {
        return MessageFormat.format("{0},{1},{2},{3},{4},{5}\n",
                        preferredName,
                        quantity,
                        eachPrice,
                        sellingPlayerId,
                        dateSdf.format(lastListing),
                        ((lastSale == null) ? "" : dateSdf.format(lastSale))
        );
    }

    public String getPreferredName() {
        return preferredName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getEachPrice() {
        return eachPrice;
    }

    public void setEachPrice(double eachPrice) {
        this.eachPrice = eachPrice;
    }

    public UUID getSellingPlayerId() {
        return sellingPlayerId;
    }

    public Date getLastListing() {
        return lastListing;
    }

    public Date getLastSale() {
        return lastSale;
    }

    protected SalesItem(String preferredName,
                        String quantity,
                        String eachPrice,
                        String sellerId,
                        String lastListing,
                        String lastSale) {
        this.preferredName = preferredName;
        this.quantity = Integer.parseInt(quantity);
        this.eachPrice = Double.parseDouble(eachPrice);
        this.sellingPlayerId = UUID.fromString(sellerId);
        try {
            this.lastListing = dateSdf.parse(lastListing);
        } catch (ParseException ee) {
            this.lastListing = new Date();
        }
        if (lastSale.isEmpty()) {
            this.lastSale = null;
        } else {
            try {
                this.lastSale = dateSdf.parse(lastSale);
            } catch (ParseException ee) {
                this.lastSale = null;
            }
        }
    }

}
