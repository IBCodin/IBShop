# IBShop
The IBShop is intended for player driven commodity sales on a multi-player Minecraft server.

The plugin requires **Vault** to find your economy.

The shop is a server-wide player-driven market. Players can find, buy and post to sell most stackable items.

The interface is text-based and command driven and has support for server related transaction fees and a configurable limit to number of items sold.

The shop starts empty and will only have items available when a player posts something for sale.

## Configuration

### MessagePrefix
`Default: [IBShop]`

This string will prefix any message from the plugin to a player (or the console)

### ListingFee
`Default: 5.00`

This value is the percentage fee that a player will be charged to create a listing. The percentage is applied to the total sales price for the listing. It is possible for a player to update the sales price after the listing has been created. If the listing price goes up, the player will be charged the current ListingFee for the difference in total sales price.

As an example, if player A creates a sales listing for 10 glowstone at $10 each ($100 total), they would immediately be charged $5 at the default rate.

The ListingFee must be greater than or equal to 0.00 and less than or equal to 50.00

### SalesFee
`Default: 10.00`

This value is the additional percentage fee that a player will pay to purchase an item from a sales listing.

As an example, if player A had listed the 10 glowstone at $10 each. Players looking to buy glowstone would see the price as $11 each (with the default SalesFee). The purchaser will pay the price with the SalesFee, the seller will receive their original each price.

The SalesFee must be greater than or equal to 0.00 and less than or equal to 75.00

### MaxChestCount
`Default: 10`

Players can be limited in the number of items they can sell in the shop. If this value is configured to zero, all players have no limit to the number of items they can sell. 

If they are limited, the limit is specified in single chests (27 stacks of items.)

If this number is greater than zero it looks for permission up to the number. At the default value of 10, the permission names are:
* ibshop.quantity.1
* ibshop.quantity.2
* ibshop.quantity.3
* ibshop.quantity.4
* ibshop.quantity.5
* ibshop.quantity.6
* ibshop.quantity.7
* ibshop.quantity.8
* ibshop.quantity.9
* ibshop.quantity.10

Each player will be able to list for sale items equivalent to the number of chests associated with their highest permission.

For example if Player A has the ibshop.quantity.1 and ibshop.quantity.3 permissions, they would be able to sell up to 3 chests worth of items.

## Commands

### ibshoplist
* Alias: ibslist
* Permission: ibshop.list
* Usage: `ibshoplist [partial_match] [page]`

If used without the partial_match, this will go through all of the sales listings a page at a time.

If partial_match is specified, you will only see sales listings where that is part of one of the names for the item (a page at a time).

The partial match is very literal, for example wo will match any 'wood' or 'wool' items 

### ibshopfind
* Alias: ibsfind
* Permission: ibshop.find
* Usage: `ibshopfind item` 

The item must match a specific item. It will show the detail sales listings for that item.

The detail listings will show the quantity available and the purchase price.

### ibshopbuy
* Alias: ibsbuy
* Permission: ibshop.buy
* Usage: `ibshopbuy item quantity max_each_price`

Used to purchase sales items from the listings.

The item you specifiy must match a specific item or you will see an error message and no listings. 

The purchase will go through the sales listings from lowest price to highest price, attempting to purchase a total of **quantity** items where the purchase price is no more than **max_each_price** each.

For example if there were 4 glowstone available at $9.90 and 10 at $11.00 and 20 more at $20; the command ibshopbuy glowstone 20 15 would purchase the 4 at $9.50 and the 10 and $11.00 at stop at 14 glowstone because the next listing would exceed the maximum each price.

You can ask to buy more of something than you can fit in your inventory, you will only be charged for the items that can fit in your inventory.

### ibshopsell
* Alias: ibssell
* Permission: ibshop.buy
* Usage: `ibshopsell (item|hand) quantity [each_price]`

This will create or update a sales listing. One player can only have one listing per item, though each listing could be for multiple stacks of the item.

You must specify the specific item to sell, or hold the item in your primary hand and specify the item as **hand**

You specify the maximum quantity of the item to take from your inventory to sell. If you specify more than you have, you will sell all that you have.

You can specify the **quantity** as zero and include an **each_price** if you just want to update the price on your sales listing.

If you already have some of this item for sale, you can leave off the **each_price** and the new items will be sold for the same price.

If there is a Listing Fee configured you will pay it to create your listing.

**NOTE:** this command currently requires confirmation. It will go through all of the parts of the transaction, identifying any fees you will be charged. To confirm the command, send it again. If you change any of the values, you will need to confirm those instead.

As you confirm the listing, you will be charged any applicable fees and the items will be removed from your inventory.

**NOTE 2:** The listing fee will be charged for any increase in price and will NOT be refunded should you reduce the price later. For example if you have 10 glowstone you are selling at $10 each and you raise the price to $11 each, you have raised the total price by $10 and will be charged $0.50 (at the default rate.)

### ibshopstock
* Alias: ibsstock
* Permission: ibshop.stock
* Usage: `ibshopstock [page]`

This is used to view the sales listings you have created.

The list will be shown page by page.

### ibshopcancel
* Alias: ibscancel
* Permission: ibshop.cancel
* Usage: `ibshopcancel item quantity`

This is used to cancel part or all of a sales listing you created and to return the items.

The item must match something you are selling.

Up to quantity items will be canceled from your sale and returned to your inventory. Items that do not fit in your inventory will not be canceled.

If you cancel all of the items in a sales listing the listing will be removed.

### ibshopconfig
* Alias: ibsconfig 
* Permission: ibshop.config
* Usage: `ibshopconfig [config_item] [value]`

Used to set the configuration items listed above under configuration while the server is running. Values will begin to have an immediate effect.

For example, if the SalesFee is raised from 10.00 to 20.00 an item listing that was $11.00 each will be $12.00 each when re-found.

### ibshopreload
* Alias: ibsreload
* Permission: ibshop.reload
* Usage: `ibshopreload`

This will cause the plugin to immediately reload all of it's configuration from the files. 



### ibshop
* Alias: ibs
* Permission: ibshop.use
* Usage: `ibshop [sub_command [arguments]]`

This is a general interface to the other ibshop commands. When typed alone, it will provide help for all of the commands that the sender is authorized to use.

This implementation allows all four of these commands to perform the same function:
* ibshopsell dirt 10 10
* ibssell dirt 10 10
* ibshop sell dirt 10 10
* ibs sell dirt 10 10

The first two are handled by the specific ibshopsell command and the last two are handled by the ibshop command by redirecting the remaining arguments to ibshopsell.

All of the ibshop* command are supported through this interface

## Permissions

### ibshop.use
`Default: true`

Basic access to the plugin and ibshop command 

### ibshop.list
`Default: true`

Access to the ibshoplist command, allowing the user to see things for sale

### ibshop.find
`Default: true`

Access to the ibshopfind command, allowing the user to see detail sales listings

### ibshop.buy
`Default: true`

Access to the ibshopbuy command, allowing the user to buy things from the shop

### ibshop.sell
`Default: true`

Access to the ibshopsell command, allowing the user to sell things at the shop

### ibshop.stock
`Default: true`

Access to the ibshopstock command, allowing the user to see what they have for sale

### ibshop.cancel
`Default: true`

Access to the ibshopcancel command, allowing the user to withdraw something from the shop

### ibshop.config
`Default: op`

Access to the ibshopconfig command, allowing the user to change plugin configuration

**Recommended for admins only**

### ibshop.reload
`Default: op`

Access to the ibshopreload command, allowing the user to reload plugin configuration

**Recommended for admins only**


### ibshop.quantity.1
`Default: true`

Allows the user to sell items at the shop equivalent to one chest full of items.

**NOTE:** if the user has a permission with a higher number (that is less than or equal to the configuration value MaxChestCount) the plugin will restrict sales to the highest number found.

**NOTE 2:** if the configuration value MaxChestCount is zero, there is no limit to the number of items that can be sold at the shop.

### ibshop.quantity.2
`Default: false`

Allows the user to sell items at the shop equivalent to two chests full of items

_See notes for ibshop.quantity.1_


### ibshop.general

Grants all of the non-admin permissions, but does not grant any quantity permissions

### ibshop.admin

Grants all of the admin permission, none of the general permissions


## Configuration Files

### blacklist.yml
This is a listing of Minecraft material names that should not be sold by the store.

_The default file will be created if this file does not exist._

### config.yml
Basic plugin configuration, see Configuration items above

_The default file will be created if this file does not exist._

### items.csv
This file was borrowed almost directly from EssentialsX. It contains the alias names for materials.

_The default file will be created if this file does not exist._

### messages.yml
Most of the messages sent by the plugin come from this file. 

**NOTE:** currently, most of the help text for the commands does not come from this file. It's a project for when I have more time to make the conversion.

You can use this file to customize the messages shown to the user.

_The default file will be created if this file does not exist._

### saleslist.csv, saleslist_old.csv, saleslist_stage.csv

These files are used to maintain the sales listings. The file is updated for every sale or purchase transaction. The saleslist.csv file is where the sales are loaded from when the plugin is enabled or the reload command is executed. 

The following sequence is used to write the data to the files:
1) Remove any old saleslist_stage
2) Write the new data to saleslist_stage
3) Remove any old saleslist_old file
4) Rename the current saleslist file to saleslist_old
5) Rename the saleslist_stage to saleslist

The intent of the sequence is to always have at least one full copy of the data on disk.

The default for this file is an empty file

If this file is deleted, you will loose all of the sales listings and items in the shop.
