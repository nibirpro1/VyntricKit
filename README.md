# VyntricKit Plugin

Ekta simple kit plugin — op ra player-der kit dite parbe, ar GUI diye notun/existing kit edit korte parbe.

## Command list

- `/kit set <playername> <kitname>` — oi player-ke oi kit-er item gula দিবে
- `/kit add <kitname>` — 54-slot GUI khulbe. Item boshao, GUI close (X চাপো) korle automatic save hoye jabe.
  - Kit ta age theke thakle, GUI te existing item gula already dekhabe — edit kore abar close korle update hoye jabe.
- `/kit remove <kitname>` — oi kit ta completely delete kore dibe (kits.yml theke o mucbe)

Both command-e **Op** thakte hobe.

## Tab-complete

`/kit ` likhe Tab chapleï `set`, `add`, `remove` show hobe. Tarpor:
- `set` er por Tab chaple online player-der naam show hobe, ar tar por kit-er naam
- `add` / `remove` er por Tab chaple existing kit-er naam show hobe

## Default kit

Plugin first-time enable hole ekta default kit toiri hoy, naam **VyntricUhc**:

- Bookshelf x64
- Apple x128
- Anvil x64
- Enchanting Table x64
- Grindstone x16

## Build kora (jar banano)

Ei plugin ta Minecraft **1.21** theke shuru kore server-er latest version porjonto (1.21.1, 1.21.4, ইত্যাদি) support kore. Build korte hole tomar computer-e Java 21+ ar Maven install thakte hobe, ar internet connection lagbe (Paper API download korar jonno).

```bash
cd VyntricKit
mvn clean package
```

Build shesh hole jar file paba: `target/VyntricKit.jar`

Oi jar file ta tomar server-er `plugins/` folder-e felo, server restart/reload dao — plugin chalu hoye jabe.

## Kit data kothay save hoy

`plugins/VyntricKit/kits.yml` file-e shob kit-er data save hoy (item, amount, enchant sob thakbe, jehetu Bukkit-er built-in ItemStack serialization use kora hoyeche).

## Notun kit banate chaile

```
/kit add MyNewKit
```

GUI khulbe, item boshao, close koro — `MyNewKit` naam-e save hoye jabe automatic. Tarpor:

```
/kit set PlayerName MyNewKit
```
