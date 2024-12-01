# World Wide Trade Shop ![Static Badge](https://img.shields.io/badge/Minecraft-%20v1.21.1-green) ![Static Badge](https://img.shields.io/badge/Plugin-blue) ![Static Badge](https://img.shields.io/badge/Easy%20to%20use-red) 

World Wide Trade Shop is a plugin for Minecraft servers that will allow players on your server to buy or sell items to players on other servers that also have this plugin installed (or any other variant that also makes requests to the api).

This plugin will provide players with an inventory-based user interface that will allow them to view items for sale or list their owns.

## Installing Plugin

To install World Wide Trade Shop on your Minecraft server you just have to copy the "wwtshop-version.jar" file to your "/plugins" folder. Once you run the server with the plugin inside for the first time, it will generate a wwtshop/ folder where you can find the configuration file for the plugin.

## Commands
- **/wwt help**: To obtain a text with information about how to use the plugin.
 
  <img width="844" alt="{A8E1B32F-1A4A-41D6-AA4C-3680CC8BDA88}" src="https://github.com/user-attachments/assets/d7df53f9-5b21-4685-911f-a77003656602">

- **/wwt sell**: Opens the sell interface.

  <img width="468" alt="{A0AACFD6-DA4A-453B-9352-625EA9313E38}" src="https://github.com/user-attachments/assets/4071e061-7671-4dc2-8b66-f2cf8d449c99">

    In the photograph, you can see the selling interface, which has a slot in the center where you can place the item you want to sell (in this case, diamonds). You can check the item's price on the emerald, and you can increase or decrease this price using the up or down arrows. At the bottom right, you can cancel the process, while the totem at the bottom         left allows you to confirm the sale. Once you confirm, the item will disappear from your inventory, and the selling interface will close.

- **/wwt buy**: On build.

## Permissions
On build...

## For developers
If for some reason you don't like this GUI plugin or would simply prefer some other kind of model, but still want to keep the overall trading system, I invite you to fork this repository to keep the API requests module and create your own system. This way, we will have multiples interfaces that endpoint on the same API.

You can find API repository [here](https://github.com/RedRiotTank/wwtapi).

## Warranty and License ![Static Badge](https://img.shields.io/badge/License-GNU%20v3.0-green)

This project uses the GNU General Public License v3.0, if you have any questions about it you can consult the LICENSE file, I remember that this software runs without any type of guarantee and could contain bad practices or even errors.
