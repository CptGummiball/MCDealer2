
# MCDealer 2

**MCDealer 2** is the comprehensive rework of the original [MCDealer](https://github.com/CptGummiball/MC-Dealer) plugin, redesigned from the ground up to be more efficient and streamlined. It removes the Python dependency, resulting in better performance and significantly reduced server load compared to the previous version.


## Features

- **Integrated Web Server**: The plugin provides an optional internal website that displays player and admin shops. This website is served from the `/web` folder of the plugin and can be enabled via `config.yml`.
- **REST API**: An API that outputs shop data in JSON format via the endpoint `/api/shopdata`. This API is always active, even if the internal website is disabled, allowing you to build your own web solutions based on the shop data.
- **Reduced Server Load**: By removing Python dependencies, the plugin is more resource-efficient, significantly lowering the load on your server compared to the previous version.
- **Multi-Language Support**: The plugin supports multiple languages for both plugin messages and the provided website, allowing for a customizable experience for users around the world.


## Installation

1. Download the latest version of **MCDealer 2** (release is yet to be announced).
2. Place the downloaded `.jar` file into your `plugins` folder (for Bukkit, Spigot, or Paper).
3. Restart your server or reload the plugin (`/reload` or `/restart`) to activate it.

    
## Configuration
After installation, you can adjust the web server settings via the `config.yml`:
```yaml
# MCDealer 2 Config
language: en-US #en-US or de-DE (ONLY INTERNAL LANGUAGE)
data:
  interval: 10m #(s = seconds, m = minute, h = hour)
web:
  port: 8089
  use-internal-website: true
debug-mode: true
# Don't change anything below!
config-version: 3
````
## Web Server
- The internal web server is optional and displays the shops directly in a browser. By setting `use-internal-website` to `true`, the website will be served on the `port` specified in `config.yml`.
- If you want to build your own website, you can simply use the provided API.
## API
The API is always active, regardless of whether the internal website is used. It provides shop data at the following endpoint:

- Endpoint: `/api/shopdata`

## Commands

The MCDealer2 plugin provides the following commands for managing shops and settings:

- **/mcdealer [subcommand]**: Manage your shops and settings.

    - **Subcommands**:
        - **reload**: Reloads the plugin configuration. (Requires permission: `mcdealer.admin`)
        - **hideshop**: Hides your shop in the Shop Overview. (Requires permission: `mcdealer.hideshop`)
        - **showshop**: Shows your previously hidden shop in the Shop Overview. (Requires permission: `mcdealer.showshop`)

## Work in Progress
This plugin is still under development. The release date has not been set. Features will continue to be added, optimized, and tested. Feedback and suggestions for improvement are always welcome!
## Support, Issues, and Ideas
For questions, issues, or ideas for new features, please feel free to open a new issue on the GitHub page. Your feedback is valuable for improving the plugin!
## Authors

- [@cptgummiball](https://www.github.com/cptgummiball)
- [@wolf128058](https://www.github.com/wolf128058)


## Mentions

First of all the one and only @Bestem0r and his Plugin [VillagerMarket](https://github.com/Bestem0r/VillagerMarket)

And special thank you to the following individuals for their contributions and support:

## LICENSE
This plugin is released under the MIT License. See the [LICENSE](LICENSE) file for details.