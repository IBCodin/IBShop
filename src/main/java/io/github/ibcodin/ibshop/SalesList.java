package io.github.ibcodin.ibshop;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static io.github.ibcodin.ibshop.MessageLookup.IBShopMessages.*;
import static java.lang.Math.abs;
import static java.lang.Math.min;

public class SalesList {

    private static final int pageSize = 6;

    private final IBShop plugin;
    private final Economy economy;
    private final Settings settings;
    private final ItemLookup itemLookup;
    private final MessageLookup messageLookup;

    private final File saveFile;
    private final File oldFile;
    private final File stageFile;
    private final List<ItemForSale> sales = new ArrayList<>();

//    private static final transient Pattern csvSplit = Pattern.compile("\\s*,\\s*");

    SalesList(IBShop plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
        this.settings = plugin.getSettings();
        this.itemLookup = plugin.getItemLookup();
        this.messageLookup = plugin.getMessageLookup();

        this.saveFile = new File(plugin.getDataFolder(), "saleslist.csv");
        this.oldFile = new File(plugin.getDataFolder(), "saleslist_old.csv");
        this.stageFile = new File(plugin.getDataFolder(), "saleslist_stage.csv");

        reload();
    }

    // Add a new or update an existing sale item
    public boolean addSalesListing(Player sender, ItemStack material, String itemName, int itemQty, double itemEach) {

        String preferredName = itemLookup.preferredName(material);

        // Search for a previous listing
        ItemForSale itemForSale = find(sender.getUniqueId(), preferredName);

        double upgradeFee = 0.0;
        if (itemForSale != null) {
            sendMessage(sender, MSG_PRIOR_LISTING, itemForSale.quantity, itemForSale.preferredName, economy.format(itemForSale.eachPrice));

            if (itemEach == 0.0) {
                itemEach = itemForSale.eachPrice;
            }

            if (abs(itemForSale.eachPrice - itemEach) > 0.001) {
                sendMessage(sender, MSG_CHANGE_PRICE, economy.format(itemForSale.eachPrice), economy.format(itemEach));

                if (itemEach > itemForSale.eachPrice) {
                    upgradeFee = itemForSale.quantity * (itemEach - itemForSale.eachPrice) * settings.getListingFee() / 100.0;
                    sendMessage(sender, MSG_HIGHER_FEE, economy.format(upgradeFee));
                }
            }
        }

        if (itemEach == 0.0) {
            sendMessage(sender, MSG_SELL_BAD_PRICE);
            return true;
        }

        // You can only sell what you have
        int invQty = getPlayerInventoryCount(sender, material);
        if (itemQty > invQty) {
            itemQty = invQty;
            material.setAmount(itemQty);
        }

        double saleTotal = itemQty * itemEach;
        sendMessage(sender, MSG_LISTING_TOTAL, itemQty, itemName, economy.format(itemEach), economy.format(saleTotal));

        double listingFee = saleTotal * settings.getListingFee() / 100.0;

        sendMessage(sender, MSG_LISTING_FEE, economy.format(listingFee));

        double totalFee = listingFee + upgradeFee;

        // Confirm you have the listing fee
        if (!economy.has(sender, totalFee)) {
            sendMessage(sender, MSG_ECON_LISTING_FAIL, economy.format(totalFee));
            return true;
        }

        // check listing space -- so they can't list too many items
        if (settings.getMaxChestCount() > 0) {
            int nStacks = getMaxStacks(sender);
            int currentStacks = getListingStacks(sender);
            int listStackSize = material.getType().getMaxStackSize();
            int listingStacks = (itemQty + listStackSize - 1) / listStackSize;

            if ((currentStacks + listingStacks) > nStacks) {
                sendMessage(sender, MSG_LISTINGS_FULL);
                return true;
            }
        }

        ItemForSale testCache = new ItemForSale(preferredName, itemQty, itemEach, sender);
        log(Level.INFO, "SalesItem: " + preferredName + " x " + itemQty + " @ " + itemEach);

        // Check for matching cached value
        Object fromCache = plugin.getCachedItem(sender.getUniqueId());

        if (fromCache == null) {
            // No Cached object
            log(Level.INFO, "No Cached Item");
            plugin.setCachedItem(sender.getUniqueId(), testCache);
            sendMessage(sender, MSG_RESEND_TO_CONFIRM);
            return true;
        }

        if (!(fromCache instanceof ItemForSale)) {
            // Cached object not ItemForSale
            log(Level.INFO, "Cached Item not ItemForSale");
            plugin.setCachedItem(sender.getUniqueId(), testCache);
            sendMessage(sender, MSG_RESEND_TO_CONFIRM);
            return true;
        }

        ItemForSale cachedItem = (ItemForSale) fromCache;
        log(Level.INFO, "CacheItem: " + cachedItem.preferredName + " x "
                + cachedItem.quantity + " @ " + cachedItem.eachPrice);

        if (!(cachedItem.preferredName.equals(testCache.preferredName)) ||
                (cachedItem.quantity != testCache.quantity) ||
                (cachedItem.eachPrice != testCache.eachPrice)
                ) {
            log(Level.INFO, "No Match");

            plugin.setCachedItem(sender.getUniqueId(), testCache);
            sendMessage(sender, MSG_RESEND_TO_CONFIRM);
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
        if (itemForSale != null) {
            itemForSale.quantity += itemQty;
            itemForSale.eachPrice = itemEach;
            itemForSale.lastListing = new Date();
        } else {
            itemForSale = new ItemForSale(preferredName, itemQty, itemEach, sender);
            sales.add(itemForSale);
        }

        save();

        double totalValue = itemForSale.quantity * itemForSale.eachPrice;
        sendMessage(sender, MSG_LISTING_ADDED, itemForSale.quantity, itemName, economy.format(itemForSale.eachPrice), economy.format(totalValue));

        return true;
    }

    // Purchase from existing sale items
    // sender
    public boolean buyItems(Player sender, ItemStack findMat, String itemName, int itemQty, double itemEach) {

        log(Level.INFO, sender.getName() + " buying " + itemQty + " " + itemName + " at up to " + itemEach + "each");

        String preferredName = itemLookup.preferredName(findMat);
        List<ItemForSale> matches = findSales(preferredName);

        // ensure the player has enough to pay for the entire order in case it is satisfied
        double maxPay = itemQty * itemEach;
        if (!economy.has(sender, maxPay)) {
            sendMessage(sender, MSG_ECON_BUY_FAIL, economy.format(maxPay));
            return true;
        }

        for (ItemForSale item : matches) {
            if (itemQty < 1)
                break;

            // Buy what we can/need from this item
            int buyQty = min(item.quantity, itemQty);
            double eachBuyPrice = item.eachPrice + (item.eachPrice * (settings.getSalesFee() / 100.0));

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
            double itemSell = buyQty * item.eachPrice;
            economy.withdrawPlayer(sender, itemPay);

            // Pay the money
            OfflinePlayer offlineSeller = plugin.getServer().getOfflinePlayer(item.sellingPlayerId);
            if (offlineSeller != null) {
                economy.depositPlayer(offlineSeller, itemSell);

                // Notify seller if online
                Player onlineSeller = plugin.getServer().getPlayer(item.sellingPlayerId);
                if (onlineSeller != null) {
                    sendMessage(onlineSeller, MSG_ITEMS_SOLD,
                            economy.format(itemSell),
                            buyQty,
                            preferredName,
                            economy.format(item.eachPrice)
                    );
                }
            }

            sendMessage(sender, MSG_ITEMS_BOUGHT,
                    economy.format(itemPay),
                    buyQty,
                    itemName,
                    economy.format(eachBuyPrice)
            );

            // Update the listing
            if (buyQty == item.quantity) {
                sales.remove(item);
            } else {
                item.quantity -= buyQty;
            }

            save();

            itemQty -= buyQty;
        }

        return true;
    }

    public boolean showSalesDetailsByItemName(CommandSender sender, String preferredName, int page) {
        List<ItemForSale> items = findSales(preferredName);

        if (items.isEmpty()) {
            sendMessage(sender, MSG_NO_LISTINGS);
            return true;
        }

        int nPages = (items.size() + (pageSize - 1)) / pageSize;
        int pageFirst = (page - 1) * pageSize;
        int pageLast = (page * pageSize);
        pageLast = (pageLast > items.size()) ? items.size() : pageLast;

        if (pageFirst < items.size()) {
            for (ItemForSale item : items.subList(pageFirst, pageLast)) {
                double eachPrice = item.eachPrice + (item.eachPrice * (settings.getSalesFee() / 100.0));
                String eachStr = economy.format(eachPrice);
                sendMessage(sender, MSG_FOUND_LISTING, item.quantity, eachStr);
            }
            sendMessage(sender, MSG_PAGE_OF, page, nPages);
        } else {
            sendMessage(sender, MSG_NO_MORE_LISTINGS);
        }

        return true;
    }

    public boolean showSenderItemsForSale(Player sender, int page) {
        List<ItemForSale> items = findStock(sender.getUniqueId());

        int nPages = (items.size() + (pageSize - 1)) / pageSize;
        int pageFirst = (page - 1) * pageSize;
        int pageLast = (page * pageSize);
        pageLast = (pageLast > items.size()) ? items.size() : pageLast;

        if (pageFirst < items.size()) {
            for (ItemForSale item : items.subList(pageFirst, pageLast)) {
                sendMessage(sender, MSG_STOCK_LISTING,
                        item.quantity,
                        item.preferredName,
                        economy.format(item.eachPrice)
                );
            }
            sendMessage(sender, MSG_PAGE_OF, page, nPages);
        } else {
            sendMessage(sender, MSG_NO_MORE_LISTINGS);
        }

        return true;
    }

    public List<String> getStockNames(Player sender) {
        List<ItemForSale> items = findStock(sender.getUniqueId());

        List<String> returnList = new ArrayList<>();
        for (ItemForSale item : items) {
            returnList.add(item.preferredName);
        }

        Collections.sort(returnList);
        return returnList;
    }

    public boolean cancelSale(Player sender, ItemStack material, String itemName, int itemQty) {
        String preferredName = itemLookup.preferredName(material);

        // Search for a previous listing
        ItemForSale itemForSale = find(sender.getUniqueId(), preferredName);

        if (itemForSale == null) {
            sendMessage(sender, MSG_CANCEL_NO_ITEMS, itemName);
            return true;
        }

        // skip-Does the player have that many items on sale?
        if (itemForSale.quantity < itemQty) {
            itemQty = itemForSale.quantity;
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
        if (itemQty == itemForSale.quantity) {
            sales.remove(itemForSale);
        } else {
            itemForSale.quantity -= itemQty;
        }

        sendMessage(sender, MSG_ITEMS_CANCELLED, itemQty, itemName);

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
        for (ItemSummary item : salesSummary()) {
            returnList.add(item.preferredName);
        }
        return returnList;
    }

    private boolean showSalesSummary(final CommandSender sender, final int page, final List<ItemSummary> items) {
        if (items.isEmpty()) {
            sendMessage(sender, MSG_NO_LISTINGS);
            return true;
        }

        int nPages = (items.size() + (pageSize - 1)) / pageSize;
        int pageFirst = (page - 1) * pageSize;
        int pageLast = (page * pageSize);
        pageLast = (pageLast > items.size()) ? items.size() : pageLast;

        if (pageFirst < items.size()) {
            for (ItemSummary item : items.subList(pageFirst, pageLast)) {
                sendMessage(sender, MSG_SALES_SUMMARY, item.quantity, item.preferredName);
            }
            sendMessage(sender, MSG_PAGE_OF, page, nPages);
        } else {
            sendMessage(sender, MSG_NO_MORE_LISTINGS);
        }

        return true;
    }


    private List<ItemSummary> salesSummaryFiltered(final List<String> prefNames) {
        final List<ItemSummary> result = new ArrayList<>();
        for (ItemSummary sum : salesSummary()) {
            if (prefNames.contains(sum.preferredName)) {
                result.add(sum);
            }
        }
        return result;
    }

    private List<ItemSummary> salesSummary() {
        final List<ItemSummary> summary = new ArrayList<>();
        final Map<String, ItemSummary> map = new HashMap<>();

        for (ItemForSale item : this.sales) {
            if (map.containsKey(item.preferredName)) {
                map.get(item.preferredName).add(item);
            } else {
                ItemSummary itemSum = new ItemSummary(item);
                map.put(item.preferredName, itemSum);
                summary.add(itemSum);
            }
        }

        return summary;
    }


    private List<ItemForSale> findSales(String preferredName) {
        List<ItemForSale> matches = new ArrayList<>();
        for (ItemForSale item : sales) {
            if (item == null) {
                log(Level.SEVERE, "SalesList.findSales: Null Sales Listing found");
                continue;
            }
            if (item.preferredName == null) {
                log(Level.WARNING, "Invalid Listing: " + item.dump());
                continue;
            }
            if (item.preferredName.equals(preferredName)) {
                matches.add(item);
            }
        }

        matches.sort(ItemForSale.PRICE_ORDER);

        return matches;
    }

    private List<ItemForSale> findStock(UUID sellerId) {
        List<ItemForSale> stock = new ArrayList<>();
        for (ItemForSale item : sales) {
            if (item == null) {
                log(Level.SEVERE, "SalesList.findStock: Null Sales Listing found");
                continue;
            }
            if (item.sellingPlayerId.equals((sellerId))) {
                stock.add(item);
            }
        }
        return stock;
    }

    private ItemForSale find(UUID sellerId, String preferredName) {
        for (ItemForSale itemForSale : sales) {
            if (itemForSale.sellingPlayerId.equals(sellerId) && itemForSale.preferredName.equals(preferredName)) {
                return itemForSale;
            }
        }
        return null;
    }

    private int getListingStacks(Player sender) {
        int stacks = 0;
        List<ItemForSale> stock = this.findStock(sender.getUniqueId());
        for (ItemForSale item : stock) {
            ItemStack mat = itemLookup.get(item.preferredName);
            int stackSize = mat.getType().getMaxStackSize();
            stacks += (item.quantity + stackSize - 1) / stackSize;
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

                    ItemForSale item = ItemForSale.reparse(line);
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
        if (stageFile.exists()) stageFile.delete();

        try {
            stageFile.createNewFile();

            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(stageFile), "utf-8"))) {
                for (ItemForSale sale : sales) {
                    writer.write(sale.dump());
                }
            }
        } catch (Exception ee) {
            plugin.getLogger().log(Level.SEVERE, "Failed to write sales stage file", ee);
            return;
        }

        // Remove any OLD file
        if (oldFile.exists()) oldFile.delete();

        // Rename file to OLD
        saveFile.renameTo(oldFile);

        // Rename STAGE to file
        stageFile.renameTo(saveFile);

    }

    public void sendMessage(CommandSender sender, MessageLookup.IBShopMessages msg, Object... args) {
        messageLookup.sendMessage(sender, msg, args);
    }

    protected void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

    static public class ItemForSale {
        public static final Comparator<ItemForSale> PRICE_ORDER =
                Comparator.comparingDouble(o -> o.eachPrice);
        private static final transient Pattern csvSplit = Pattern.compile("\\s*,\\s*");
        private static final transient SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String preferredName;
        int quantity;
        double eachPrice;
        UUID sellingPlayerId;
        Date lastListing;
        Date lastSale;

        public ItemForSale(String preferredName,
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

        protected ItemForSale(String preferredName,
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

        public static ItemForSale reparse(final String line) {
            String[] fields = csvSplit.split(line);

            if (fields.length < 5) {
                System.out.println("ItemForSale.reparse: only found " + fields.length + " fields");
                return null;
            }

            return new ItemForSale(
                    fields[0],
                    fields[1],
                    fields[2],
                    fields[3],
                    fields[4],
                    (fields.length > 5) ? fields[5] : ""
            );
        }

        public String dump() {
            return new MessageFormat("{0},{1},{2},{3},{4},{5}\n")
                    .format(new Object[]{
                            preferredName,
                            quantity,
                            eachPrice,
                            sellingPlayerId,
                            dateSdf.format(lastListing),
                            ((lastSale == null) ? "" : dateSdf.format(lastSale))
                    });
        }
    }

    static class ItemSummary {
        String preferredName;
        int quantity;

        public ItemSummary(ItemForSale item) {
            this.preferredName = item.preferredName;
            this.quantity = item.quantity;
        }

        public void add(ItemForSale item) {
            assert (item.preferredName.equals(this.preferredName));
            this.quantity += item.quantity;
        }

        public String dump() {
            return new MessageFormat("{0},{1}\n")
                    .format(new Object[]{preferredName, quantity});
        }
    }
}
