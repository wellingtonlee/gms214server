# SwordieMS — MapleStory GMS v214 Server Emulator

A server emulator for MapleStory Global (GMS) version 214, based on the [SwordieMS source](https://forum.ragezone.com/f427/java-v214-swordie-source-1209447/).

## Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| **Java JDK** | 17+ | Must support `--enable-preview` |
| **Apache Maven** | 3.6+ | Build tool |
| **MySQL** | 8.0+ | MariaDB 10.5+ also works |
| **MapleStory v214 Client** | GMS v214.1 | For extracting WZ data files |

## Quick Start

```bash
# 1. Clone the repository
git clone <repo-url> && cd gms214server

# 2. Set up the database
mysql -u root -p < sql/"1 - InitTables_characters.sql"

# 3. Extract WZ data (see WZ Data section below)

# 4. Build the server
mvn clean package -DskipTests

# 5. Run the server
java --enable-preview -jar bin/maplestory-2.13.1-jar-with-dependencies.jar
```

## Detailed Deployment Guide

### Step 1: Install Java 17

The server requires JDK 17 with preview features enabled.

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
java -version   # verify: openjdk version "17.x.x"
```

**Windows:**
Download and install [Adoptium Temurin JDK 17](https://adoptium.net/).

**macOS:**
```bash
brew install openjdk@17
```

### Step 2: Install Maven

**Ubuntu/Debian:**
```bash
sudo apt install maven
mvn -version   # verify: Apache Maven 3.6+
```

**Windows:**
Download from [maven.apache.org](https://maven.apache.org/download.cgi) and add `bin/` to your PATH.

**macOS:**
```bash
brew install maven
```

### Step 3: Set Up MySQL Database

1. **Install MySQL 8.0+** and start the service:

   ```bash
   # Ubuntu/Debian
   sudo apt install mysql-server
   sudo systemctl start mysql
   ```

2. **Create the database and user** matching `hibernate.cfg.xml`:

   ```sql
   mysql -u root -p
   ```

   ```sql
   CREATE DATABASE MS214 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'MSServer'@'127.0.0.1' IDENTIFIED BY 'yellowhammer';
   GRANT ALL PRIVILEGES ON MS214.* TO 'MSServer'@'127.0.0.1';
   FLUSH PRIVILEGES;
   ```

3. **Run the SQL initialization scripts** in order:

   ```bash
   mysql -u MSServer -p MS214 < sql/"1 - InitTables_characters.sql"
   mysql -u MSServer -p MS214 < sql/"2 - InitTables_drops.sql"
   mysql -u MSServer -p MS214 < sql/"3 - InitTables_cashshop.sql"
   mysql -u MSServer -p MS214 < sql/"4 - drops.sql"
   mysql -u MSServer -p MS214 < sql/"5 - InitTable_equip_drops.sql"
   mysql -u MSServer -p MS214 < sql/"6 - beautyalbum.sql"
   mysql -u MSServer -p MS214 < sql/"7 - charactercard.sql"
   mysql -u MSServer -p MS214 < sql/"8 - InitTable_npc.sql"
   mysql -u MSServer -p MS214 < sql/"9 - InitTables_MonsterCollection.sql"
   mysql -u MSServer -p MS214 < sql/"10 - InitTables_shops.sql"
   mysql -u MSServer -p MS214 < sql/"99 - cashshopfix.sql"
   ```

   > **Order matters.** The numbered prefixes indicate the correct execution sequence. Additional scripts (`hairequips.sql`, `unseenequips.sql`) are optional cosmetic data.

### Step 4: Obtain and Extract WZ Data

The server requires extracted WZ files placed in a `wz/` directory at the project root.

1. **Download the v214 client** using [DepotDownloader](https://github.com/SteamRE/DepotDownloader):

   ```bash
   dotnet run -app 216150 -depot 216151 -manifest 976750626611673486
   ```

2. **Extract the WZ files** using one of these tools:

   | Tool | Pros | Cons |
   |------|------|------|
   | [WZ-Dumper](https://github.com/Xterminatorz/WZ-Dumper) (recommended) | Simple, smaller output | Lossy extraction |
   | [HaRepacker](https://github.com/lastbattle/Harepacker-resurrected) | Lossless extraction | Larger output |

3. **Place the extracted folders** in the project root:

   ```
   gms214server/
   ├── wz/               <-- extracted WZ data goes here
   │   ├── Base.wz/
   │   ├── Character.wz/
   │   ├── Effect.wz/
   │   ├── Etc.wz/
   │   ├── Item.wz/
   │   ├── Map.wz/
   │   ├── Mob.wz/
   │   ├── Morph.wz/
   │   ├── Npc.wz/
   │   ├── Quest.wz/
   │   ├── Reactor.wz/
   │   ├── Skill.wz/
   │   ├── Sound.wz/
   │   ├── String.wz/
   │   └── UI.wz/
   ├── scripts/
   ├── resources/
   ├── sql/
   └── ...
   ```

   On first startup, the server will automatically generate binary `.dat` cache files from the WZ data into a `dat/` directory. This initial load takes significantly longer than subsequent starts.

### Step 5: Configure the Server

#### Database Connection (`src/main/java/hibernate.cfg.xml`)

Update these values if your database setup differs from the defaults:

| Property | Default | Description |
|----------|---------|-------------|
| `hibernate.connection.url` | `jdbc:mysql://127.0.0.1:3306/MS214` | Database URL |
| `hibernate.connection.username` | `MSServer` | Database user |
| `hibernate.connection.password` | `yellowhammer` | Database password |

#### Server Constants (`src/main/java/net/swordie/ms/ServerConstants.java`)

| Constant | Default | Description |
|----------|---------|-------------|
| `LOGIN_PORT` | `8484` | Login server port |
| `API_PORT` | `8483` | REST API port |
| `CHANNEL_IP` | `54.68.160.34` | IP clients connect to (change for LAN/localhost) |
| `LOCAL_HOST_SERVER` | `false` | Set `true` for local-only testing |
| `VERSION` | `214` | Client version — do not change |

#### World Config (`src/main/java/net/swordie/ms/ServerConfig.java`)

| Constant | Default | Description |
|----------|---------|-------------|
| `WORLD_ID` | `Bera` | World selection |
| `SERVER_NAME` | `MS` | Display name |
| `USER_LIMIT` | `20` | Max concurrent users |
| `MAX_CHARACTERS` | `30` | Max characters per account |

**For localhost play**, set `CHANNEL_IP` to `{127, 0, 0, 1}` and `LOCAL_HOST_SERVER` to `true`.

### Step 6: Build

```bash
mvn clean package -DskipTests
```

This produces a fat JAR at `bin/maplestory-2.13.1-jar-with-dependencies.jar` containing all dependencies.

If the build fails, verify:
- JDK 17 is active: `java -version`
- Maven can reach the repository at `maven.aliyun.com` (or change `<repositories>` in `pom.xml` to Maven Central)

### Step 7: Run the Server

```bash
java --enable-preview -jar bin/maplestory-2.13.1-jar-with-dependencies.jar
```

The `--enable-preview` flag is **required** — the codebase uses Java 17 preview features.

**Recommended JVM flags for production:**

```bash
java --enable-preview \
  -Xms512m -Xmx2g \
  -XX:+UseG1GC \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=../heapdumps \
  -jar bin/maplestory-2.13.1-jar-with-dependencies.jar
```

**Startup sequence** (visible in logs):
1. Hibernate/database initialization
2. WZ/dat data loading (slow on first run)
3. String data, world map, handlers
4. Skill data, NPC data
5. VCore, Cash Shop, Monster Collection
6. Channel acceptors start
7. `"Finished loading server"` — server is ready

### Step 8: Connect with the Client

The client must be the exact v214.1 GMS client. You need to redirect the client to your server's IP using a localhost redirect or a custom launcher.

**Default ports the server listens on:**

| Service | Port |
|---------|------|
| Login Server | 8484 |
| Channel Servers | 8585+ (one per channel) |
| Chat Server | 8586+ |
| API Server | 8483 |

## Project Structure

```
gms214server/
├── src/main/java/net/swordie/ms/
│   ├── Server.java              # Entry point
│   ├── ServerConstants.java     # Ports, paths, version
│   ├── ServerConfig.java        # World settings
│   ├── handlers/                # Packet handlers (login, item, user, etc.)
│   ├── client/                  # Character, account, inventory models
│   ├── connection/              # Netty networking, database (Hibernate)
│   ├── constants/               # Game/item/skill constants
│   ├── enums/                   # Enumerations
│   ├── life/                    # Mobs, NPCs, summons
│   ├── loaders/                 # WZ/dat data loaders
│   ├── scripts/                 # Script engine integration
│   ├── util/                    # Utilities
│   └── world/                   # World, channel, field, shop logic
├── scripts/                     # Python/JS NPC, quest, portal, reactor scripts
├── resources/                   # Static game data (shops, Data.wz)
├── sql/                         # Database initialization scripts
├── properties/                  # Login/channel properties
├── wz/                          # Extracted WZ data (not in repo)
├── dat/                         # Generated binary cache (auto-created)
├── pom.xml                      # Maven build configuration
└── hibernate.cfg.xml            # At src/main/java/hibernate.cfg.xml
```

## Key Technologies

| Component | Technology |
|-----------|-----------|
| Language | Java 17 (preview features) |
| Build | Maven 3 with Assembly plugin |
| Networking | Netty 4.1 |
| Database | MySQL 8.0 via Hibernate 5.6 ORM |
| Scripting | Jython 2.7 (Python NPCs) + Nashorn 15 (JavaScript) |
| Auth | BCrypt (jbcrypt 0.4) |
| Logging | Log4j 1.2 |

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `ClassNotFoundException: javax.xml.bind` | Ensure JDK 17 is used (JAXB deps are bundled) |
| `Access denied for user 'MSServer'` | Verify MySQL user, password, and grants match `hibernate.cfg.xml` |
| `Dat files cannot be found` | Normal on first run — WZ data will be parsed into `.dat` files automatically |
| `java.net.BindException: Address already in use` | Another process is using port 8484 — kill it or change `LOGIN_PORT` |
| Build fails on dependencies | The POM uses `maven.aliyun.com` mirror; change to `https://repo1.maven.org/maven2` if unreachable |
| `UnsupportedClassVersionError` | Wrong Java version — must be JDK 17+ |
| Client can't connect | Ensure `CHANNEL_IP` matches your server's IP; use `127.0.0.1` for localhost |
| Slow first startup | Expected — dat file generation from WZ data is a one-time cost |

## Known Issues

1. Some class skill behaviors are incorrect (not all classes have been audited).
2. Mob-specific drop data is incomplete — level-based drops are used as fallback.
3. Many quest and NPC interactions are unscripted.
4. V Matrix has display issues.
5. Boss skill effects are partially implemented (damage skills now work, but visual/mechanical effects may be missing).
6. Rune interactions on maps are not functional.
7. Some maps may softlock due to missing quest scripts.

## License

MIT License — see [LICENSE.md](LICENSE.md) for details.
