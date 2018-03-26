package io.github.ibcodin.ibshop;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

import static io.github.ibcodin.ibshop.IBShopMessages.*;
import static java.lang.Math.abs;
import static java.lang.Math.min;

public class SalesList {

    private static final int pageSize = 6;

    private final IBShop plugin;
    private final Economy economy;
    private final Settings settings;
    private final ItemLookup itemLookup;

    private final File saveFile;
    private final File oldFile;
    private final File stageFile;
    private final List<SalesItem> sales = new ArrayList<>();

//    private static final transient Pattern csvSplit = Pattern.compile("\\s*,\\s*");

    SalesList(IBShop plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
        this.settings = plugin.getSettings();
        this.itemLookup = plugin.getItemLookup();

        this.saveFile = new File(plugin.getDataFolder(), "saleslist.csv");
        this.oldFile = new File(plugin.getDataFolder(), "saleslist_old.csv");
        this.stageFile = new File(plugin.getDataFolder(), "saleslist_stage.csv");

        reload();
    }

    // Add a new or update an existing sale item
    public boolean addSalesListing(Player sender, ItemStack material, String itemName, int itemQty, double itemEach) {

        String preferredName = itemLookup.preferredName(material);

        // Search for a previous listing
        SalesItem salesItem = find(sender.getUniqueId(), preferredName);

        double upgradeFee = 0.0;
        if (salesItem != null) {
            send(sender, PRIOR_LISTING, salesItem.getQuantity(), salesItem.getPreferredName(), economy.format(salesItem.getEachPrice()));

            if (itemEach == 0.0) {
                itemEach = salesItem.getEachPrice();
            }

            if (abs(salesItem.getEachPrice() - itemEach) > 0.001) {
                send(sender, CHANGE_PRICE, economy.format(salesItem.getEachPrice()), economy.format(itemEach));

                if (itemEach > salesItem.getEachPrice()) {
                    upgradeFee = salesItem.getQuantity() * (itemEach - salesItem.getEachPrice()) * settings.getListingFee() / 100.0;
                    send(sender, HIGHER_FEE, economy.format(upgradeFee));
                }
            }
        }

        if (itemEach == 0.0) {
            send(sender, SELL_BAD_PRICE);
            return true;
        }

        // You can only sell what you have
        int invQty = getPlayerInventoryCount(sender, material);
        if (itemQty > invQty) {
            itemQty = invQty;
            material.setAmount(itemQty);
        }

        double saleTotal = itemQty * itemEach;
        send(sender, LISTING_TOTAL, itemQty, itemName, economy.format(itemEach), economy.format(saleTotal));

        double listingFee = saleTotal * settings.getListingFee() / 100.0;

        send(sender, LISTING_FEE, economy.format(listingFee));

        double totalFee = listingFee + upgradeFee;

        // Confirm you have the listing fee
        if (!economy.has(sender, totalFee)) {
            send(sender, ECON_LISTING_FAIL, economy.format(totalFee));
            return true;
        }

        // check listing space -- so they can't list too many items
        if (settings.getMaxChestCount() > 0) {
            int nStacks = getMaxStacks(sender);
            int currentStacks = getListingStacks(sender);
            int listStackSize = material.getType().getMaxStackSize();
            int listingStacks = (itemQty + listStackSize - 1) / listStackSize;

            if ((currentStacks + listingStacks) > nStacks) {
                send(sender, LISTINGS_FULL);
                return true;
            }
        }

        SalesItem testCache = new SalesItem(preferredName, itemQty, itemEach, sender);
        log(Level.INFO, "SalesItem: " + preferredName + " x " + itemQty + " @ " + itemEach);

        // Check for matching cached value
        Object fromCache = plugin.getCachedItem(sender.getUniqueId());

        if (fromCache == null) {
            // No Cached object
            log(Level.INFO, "No Cached Item");
            plugin.setCachedItem(sender.getUniqueId(), testCache);
            send(sender, RESEND_TO_CONFIRM);
            return true;
        }

        if (!(fromCache instanceof SalesItem)) {
            // Cached object not SalesItem
            log(Level.INFO, "Cached Item not SalesItem");
            plugin.setCachedItem(sender.getUniqueId(), testCache);
            send(sender, RESEND_TO_CONFIRM);
            return true;
        }

        SalesItem cachedItem = (SalesItem) fromCache;
        log(Level.INFO, "CacheItem: " + cachedItem.getPreferredName() + " x "
                + cachedItem.getQuantity() + " @ " + cachedItem.getEachPrice());

        if (!(cachedItem.getPreferredName().equals(testCache.getPreferredName())) ||
                (cachedItem.getQuantity() != testCache.getQuantity()) ||
                (cachedItem.getEachPrice() != testCache.getEachPrice())
                ) {
            log(Level.INFO, "No Match");

            plugin.setCachedItem(sender.getUniqueId(), testCache);
            send(sender, RESEND_TO_CONFIRM);
            return true;
        }

        // otherwise clear the cache that matched
        plugin.clearCachedItem(sender.getUniqueId());

        // and complete the posting

        // Debit the Listing Fee
        log(Level.INFO, "withdraw " + totalFee + " from " + sender.getName());
        EconomyResponse response = plugin.getEconomy().withdrawPlayer(sender, totalFee);
        if (!response.transactionSuccess()) {
            log(Level.WARNING, "Transaction failed");
        }

        // Remove the items
        log(Level.INFO, "remove " + material.getAmount() + " x " + preferredName);
        sender.getInventory().removeItem(material);
        log(Level.INFO, "removed");

        // Post the listing
        if (salesItem != null) {
            salesItem.addStock(itemQty);
            salesItem.setEachPrice(itemEach);
            salesItem.touchListingDate();
        } else {
            salesItem = new SalesItem(preferredName, itemQty, itemEach, sender);
            sales.add(salesItem);
        }

        save();

        double totalValue = salesItem.getQuantity() * salesItem.getEachPrice();
        send(sender, LISTING_ADDED, salesItem.getQuantity(), itemName, economy.format(salesItem.getEachPrice()), economy.format(totalValue));

        return true;
    }

    // Purchase from existing sale items
    // sender
    public boolean buyItems(Player sender, ItemStack findMat, String itemName, int itemQty, double itemEach) {

        log(Level.INFO, sender.getName() + " buying " + itemQty + " " + itemName + " at up to " + itemEach + "each");

        String preferredName = itemLookup.preferredName(findMat);
        List<SalesItem> matches = findSales(preferredName);

        // ensure the player has enough to pay for the entire order in case it is satisfied
        double maxPay = itemQty * itemEach;
        if (!economy.has(sender, maxPay)) {
            send(sender, ECON_BUY_FAIL, economy.format(maxPay));
            return true;
        }

        for (SalesItem item : matches) {
            if (itemQty < 1)
                break;

            // Buy what we can/need from this item
            int buyQty = min(item.getQuantity(), itemQty);
            double eachBuyPrice = item.getEachPrice() + (item.getEachPrice() * (settings.getSalesFee() / 100.0));

            if (eachBuyPrice > itemEach)
                break;

            // Build the stack for the available/requested items
            ItemStack requested = itemLookup.get(preferredName, buyQty);
            // Try to put the requested items in the player inventory
            Map<Integer, ItemStack> nofit = sender.getInventory().addItem(requested);

            // Adjust the Qty for what fit
            if (!nofit.isEmpty()) {
                buyQty -= nofit.get(0).getAmount();
            }

            // Take the money
            double itemPay = buyQty * eachBuyPrice;
            double itemSell = buyQty * item.getEachPrice();
            economy.withdrawPlayer(sender, itemPay);

            // Pay the money
            OfflinePlayer offlineSeller = plugin.getServer().getOfflinePlayer(item.getSellingPlayerId());
            if (offlineSeller != null) {
                economy.depositPlayer(offlineSeller, itemSell);

                // Notify seller if online
                Player onlineSeller = plugin.getServer().getPlayer(item.getSellingPlayerId());
                if (onlineSeller != null) {
                    send(onlineSeller, ITEMS_SOLD,
                            economy.format(itemSell),
                            buyQty,
                            preferredName,
                            economy.format(item.getEachPrice())
                    );
                }
            }

            send(sender, ITEMS_BOUGHT,
                    economy.format(itemPay),
                    buyQty,
                    itemName,
                    economy.format(eachBuyPrice)
            );

            // Update the listing
            item.touchSaleDate();
            if (item.removeStock(buyQty) == 0) {
                sales.remove(item);
            }
            save();

            itemQty -= buyQty;
        }

        return true;
    }

    public boolean showSalesDetailsByItemName(CommandSender sender, String preferredName, int page) {
        List<SalesItem> items = findSales(preferredName);

        if (items.isEmpty()) {
            send(sender, NO_LISTINGS);
            return true;
        }

        int nPages = (items.size() + (pageSize - 1)) / pageSize;
        int pageFirst = (page - 1) * pageSize;
        int pageLast = (page * pageSize);
        pageLast = (pageLast > items.size()) ? items.size() : pageLast;

        if (pageFirst < items.size()) {
            for (SalesItem item : items.subList(pageFirst, pageLast)) {
                double eachPrice = item.getEachPrice() + (item.getEachPrice() * (settings.getSalesFee() / 100.0));
                String eachStr = economy.format(eachPrice);
                send(sender, FOUND_LISTING, item.getQuantity(), eachStr);
            }
            send(sender, PAGE_OF, page, nPages);
        } else {
            send(sender, NO_MORE_LISTINGS);
        }

        return true;
    }

    public boolean showSenderItemsForSale(Player sender, int page) {
        List<SalesItem> items = findStock(sender.getUniqueId());

        int nPages = (items.size() + (pageSize - 1)) / pageSize;
        int pageFirst = (page - 1) * pageSize;
        int pageLast = (page * pageSize);
        pageLast = (pageLast > items.size()) ? items.size() : pageLast;

        if (pageFirst < items.size()) {
            for (SalesItem item : items.subList(pageFirst, pageLast)) {
                send(sender, STOCK_LISTING,
                        item.getQuantity(),
                        item.getPreferredName(),
                        economy.format(item.getEachPrice())
                );
            }
            send(sender, PAGE_OF, page, nPages);
        } else {
            send(sender, NO_MORE_LISTINGS);
        }

        return true;
    }

    public List<String> getStockNames(Player sender) {
        List<SalesItem> items = findStock(sender.getUniqueId());

        List<String> returnList = new ArrayList<>();
        for (SalesItem item : items) {
            returnList.add(item.getPreferredName());
        }

        Collections.sort(returnList);
        return returnList;
    }

    public boolean cancelSale(Player sender, ItemStack material, String itemName, int itemQty) {
        String preferredName = itemLookup.preferredName(material);

        // Search for a previous listing
        SalesItem salesItem = find(sender.getUniqueId(), preferredName);

        if (salesItem == null) {
            send(sender, CANCEL_NO_ITEMS, itemName);
            return true;
        }

        // skip-Does the player have that many items on sale?
        if (salesItem.getQuantity() < itemQty) {
            itemQty = salesItem.getQuantity();
        }

        // Build the stack for the available/requested items
        ItemStack requested = itemLookup.get(preferredName, itemQty);
        // Try to put the requested items in the player inventory
        Map<Integer, ItemStack> nofit = sender.getInventory().addItem(requested);

        // Adjust the Qty for what fit
        if (!nofit.isEmpty()) {
            itemQty -= nofit.get(0).getAmount();
        }

        // Update the listing
        if (salesItem.removeStock(itemQty) == 0) {
            sales.remove(salesItem);
        }

        send(sender, ITEMS_CANCELLED, itemQty, itemName);

        save();

        return true;
    }

    public boolean showAllSalesSummary(CommandSender sender, int page) {
        return showSalesSummary(sender, page, salesSummary());
    }

    public boolean showSelectedSalesSummary(CommandSender sender, List<String> preferredNames, int page) {
        return showSalesSummary(sender, page, salesSummaryFiltered(preferredNames));
    }

    public List<String> getSalesItemNames() {
        List<String> returnList = new ArrayList<>();
        for (SalesSummary item : salesSummary()) {
            returnList.add(item.getPreferredName());
        }
        return returnList;
    }

    private boolean showSalesSummary(final CommandSender sender, final int page, final List<SalesSummary> items) {
        if (items.isEmpty()) {
            send(sender, NO_LISTINGS);
            return true;
        }

        int nPages = (items.size() + (pageSize - 1)) / pageSize;
        int pageFirst = (page - 1) * pageSize;
        int pageLast = (page * pageSize);
        pageLast = (pageLast > items.size()) ? items.size() : pageLast;

        if (pageFirst < items.size()) {
            for (SalesSummary item : items.subList(pageFirst, pageLast)) {
                send(sender, SALES_SUMMARY, item.getQuantity(), item.getPreferredName());
            }
            send(sender, PAGE_OF, page, nPages);
        } else {
            send(sender, NO_MORE_LISTINGS);
        }

        return true;
    }


    private List<SalesSummary> salesSummaryFiltered(final List<String> prefNames) {
        final List<SalesSummary> result = new ArrayList<>();
        for (SalesSummary sum : salesSummary()) {
            if (prefNames.contains(sum.getPreferredName())) {
                result.add(sum);
            }
        }
        return result;
    }

    private List<SalesSummary> salesSummary() {
        final List<SalesSummary> summary = new ArrayList<>();
        final Map<String, SalesSummary> map = new HashMap<>();

        for (SalesItem item : this.sales) {
            if (map.containsKey(item.getPreferredName())) {
                map.get(item.getPreferredName()).add(item);
            } else {
                SalesSummary itemSum = new SalesSummary(item);
                map.put(item.getPreferredName(), itemSum);
                summary.add(itemSum);
            }
        }

        return summary;
    }


    private List<SalesItem> findSales(String preferredName) {
        List<SalesItem> matches = new ArrayList<>();
        for (SalesItem item : sales) {
            if (item == null) {
                log(Level.SEVERE, "SalesList.findSales: Null Sales Listing found");
                continue;
            }
            if (item.getPreferredName() == null) {
                log(Level.WARNING, "Invalid Listing: " + item.toCSV());
                continue;
            }
            if (item.getPreferredName().equals(preferredName)) {
                matches.add(item);
            }
        }

        matches.sort(SalesItem.PRICE_ORDER);

        return matches;
    }

    private List<SalesItem> findStock(UUID sellerId) {
        List<SalesItem> stock = new ArrayList<>();
        for (SalesItem item : sales) {
            if (item == null) {
                log(Level.SEVERE, "SalesList.findStock: Null Sales Listing found");
                continue;
            }
            if (item.getSellingPlayerId().equals((sellerId))) {
                stock.add(item);
            }
        }
        return stock;
    }

    private SalesItem find(UUID sellerId, String preferredName) {
        for (SalesItem salesItem : sales) {
            if (salesItem.getSellingPlayerId().equals(sellerId) && salesItem.getPreferredName().equals(preferredName)) {
                return salesItem;
            }
        }
        return null;
    }

    private int getListingStacks(Player sender) {
        int stacks = 0;
        List<SalesItem> stock = this.findStock(sender.getUniqueId());
        for (SalesItem item : stock) {
            ItemStack mat = itemLookup.get(item.getPreferredName());
            int stackSize = mat.getType().getMaxStackSize();
            stacks += (item.getQuantity() + stackSize - 1) / stackSize;
        }
        return stacks;
    }

    private int getPlayerInventoryCount(Player player, ItemStack material) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null &&
                    stack.getType().equals(material.getType()) &&
                    stack.getDurability() == material.getDurability() &&
                    !stack.hasItemMeta()) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    private int getMaxStacks(Player player) {
        int numChest = 0;
        for (int ii = 1; ii <= settings.getMaxChestCount(); ii++) {
            if (player.hasPermission(String.format("ibshop.quantity.%d", ii))) {
                numChest = ii;
            }
        }
        return 27 * numChest;
    }

    public void reload() {
        log(Level.INFO, "Loading sales list");

        if (!saveFile.exists()) {
            log(Level.WARNING, "No sales listing file: saleslist.csv");
            return;
        }

        sales.clear();

        try {
            try (BufferedReader br = new BufferedReader(new FileReader(saveFile))) {
                for (String line; (line = br.readLine()) != null; ) {
                    if (line.length() > 0 && line.charAt(0) == '#')
                        continue;

                    SalesItem item = SalesItem.reparseCSV(line);
                    if (item == null) {
                        log(Level.SEVERE, "Failed to load sale: " + line);
                        continue;
                    }
                    sales.add(item);
                }
            }
            log(Level.INFO, "Loaded " + sales.size() + " items");
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    private void save() {
        log(Level.INFO, "SalesList.save");

        // Create new STAGE file
        if (stageFile.exists()) {
            if (! stageFile.delete())
            {
                log(Level.SEVERE, "Failed to delete old stage file " + stageFile.getName());
            }
        }

        try {
            if (! stageFile.createNewFile()) {
                log(Level.SEVERE, "Failed to create stage file " + stageFile.getName());
            }

            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(stageFile), "utf-8"))) {
                for (SalesItem sale : sales) {
                    writer.write(sale.toCSV());
                }
            }
        } catch (Exception ee) {
            plugin.log(Level.SEVERE, "Failed to write sales stage file", ee);
            return;
        }

        // Remove any OLD file
        if (oldFile.exists()) {
            if (! oldFile.delete()) {
                log(Level.SEVERE, "Failed to remove old file " + oldFile.getName());
            }
        }

        // Rename file to OLD
        if (! saveFile.renameTo(oldFile)) {
            log(Level.SEVERE, "Failed to rename sales file " + saveFile.getName() + " to " + oldFile.getName());
        }

        // Rename STAGE to file
        if (! stageFile.renameTo(saveFile)) {
            log(Level.SEVERE, "Failed to rename stage file " + stageFile.getName() + " to " + saveFile.getName());
        }
    }

    public void send(CommandSender sender, IBShopMessages msg, Object... args) {
        plugin.send(sender, msg, args);
    }

    protected void log(Level level, String message) {
        plugin.log(level, message);
    }

}

