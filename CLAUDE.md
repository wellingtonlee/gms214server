# CLAUDE.md — Project Context for AI Agents

## What is this?

SwordieMS — a MapleStory GMS v214 server emulator. Written in Java 17 with Maven. ~645 Java source files, thousands of Python NPC/quest scripts.

## Build & Run

```bash
# Build (produces fat JAR)
mvn clean package -DskipTests

# Run (--enable-preview is REQUIRED)
java --enable-preview -jar bin/maplestory-2.13.1-jar-with-dependencies.jar

# Run tests
mvn test
```

Output JAR: `bin/maplestory-2.13.1-jar-with-dependencies.jar`

## Architecture

### Entry Point
`net.swordie.ms.Server` — `src/main/java/net/swordie/ms/Server.java`

### Key Packages (under `src/main/java/net/swordie/ms/`)

| Package | Purpose |
|---------|---------|
| `handlers/` | Packet handlers — process client→server messages (login, movement, combat, items, etc.) |
| `client/` | Character, Account, Inventory, Skills — player state models |
| `connection/` | Netty networking (`netty/`), Hibernate ORM (`db/`), packet crypto (`crypto/`), packet definitions (`packet/`) |
| `connection/api/` | REST-like API server |
| `life/` | Mobs, NPCs, Pets, Summons, Reactors, Drop logic |
| `world/` | World, Channel, Field (maps), Shops, Guilds, Parties, Auction |
| `loaders/` | WZ/dat data loaders — parse game data files into memory |
| `scripts/` | Jython/Nashorn script engine integration |
| `constants/` | Game constants (items, skills, jobs, game mechanics) |
| `enums/` | Enumerations for stats, chat types, inventory types, etc. |
| `util/` | Utilities, containers, Position, Rect, FileTime |

### Database
- **MySQL 8** via **Hibernate 5.6 ORM**
- Config: `src/main/java/hibernate.cfg.xml`
- Manager: `net.swordie.ms.connection.db.DatabaseManager`
- Schema init scripts in `sql/` (run numbered files in order: 1, 2, 3, ... 10, 99)
- Default DB: `MS214`, user: `MSServer`

### Networking (Netty 4.1)
- **Login Server**: port 8484 — authentication, character selection
- **API Server**: port 8483 — external REST API
- **Channel Servers**: port 8584+ (8484 + 100*worldId + channelId)
- **Chat Server**: port configurable (currently disabled)

### Scripts
- Location: `scripts/` directory (not under `src/`)
- Subdirs: `npc/`, `quest/`, `portal/`, `reactor/`, `field/`, `item/`
- Language: Python 2.7 (via Jython) — some JavaScript (via Nashorn)
- Scripts are hot-loadable at runtime

## Key Configuration Files

| File | What it configures |
|------|-------------------|
| `src/main/java/net/swordie/ms/ServerConstants.java` | Ports, paths, version, IP, crypto settings |
| `src/main/java/net/swordie/ms/ServerConfig.java` | World ID, server name, user limit, max characters |
| `src/main/java/hibernate.cfg.xml` | Database URL, credentials, Hibernate settings |
| `src/main/java/log4j.properties` | Logging configuration |
| `properties/CP_207-1.properties` | Outgoing packet opcodes (server→client) |
| `properties/LP_207-1.properties` | Incoming packet opcodes (client→server) |
| `pom.xml` | Maven build config, dependencies |

## Conventions

- **Java 17 preview features** are used (records, sealed classes) — `--enable-preview` required at both compile and runtime
- **Hibernate entities** use JPA annotations (`@Entity`, `@Table`, `@Column`)
- **Packet handlers** use custom `@Handler` annotations with opcode enums
- **Handler registration** is automatic via reflection at startup
- **All game data** comes from WZ files (extracted MapleStory client data) → compiled to `.dat` cache on first run
- **NPC interactions** are scripted in Python, not hardcoded in Java

## Directory Layout (project root)

```
gms214server/
├── src/main/java/          # Java source code
├── src/test/java/          # Tests (JUnit 5)
├── scripts/                # Python/JS game scripts (NPC, quest, portal, etc.)
├── resources/              # Static game data (Data.wz, fonts, shops)
├── properties/             # Packet opcode definitions
├── sql/                    # Database initialization scripts
├── wz/                     # Extracted WZ data (NOT in repo — must provide)
├── dat/                    # Binary cache (auto-generated on first run)
├── bin/                    # Build output (fat JAR)
├── pom.xml                 # Maven build
├── Dockerfile              # Container build
├── docker-compose.yml      # Full stack (server + MySQL)
└── .github/workflows/      # CI pipeline
```

## Common Pitfalls

1. **`--enable-preview` is mandatory** — forgetting it at runtime causes `UnsupportedClassVersionError`
2. **Maven mirror**: `pom.xml` uses `maven.aliyun.com` — may be unreachable outside China. Swap to `https://repo1.maven.org/maven2` if needed
3. **WZ data not in repo** — must be extracted from a GMS v214 client using WZ-Dumper or HaRepacker and placed in `wz/`
4. **First startup is slow** — WZ→dat compilation is a one-time cost
5. **hibernate.cfg.xml is under src/main/java/** (not resources/) — this is intentional, it gets included via the Maven resource config
6. **Packet opcodes** are v207 format (properties files) even though the server is v214 — the opcode mapping handles the translation
7. **Database must exist before starting** — run SQL scripts in numbered order

## Known Incomplete Areas

- Many class skills have incorrect behavior (not all audited)
- Mob drop data is incomplete (level-based fallback used)
- Many quests/NPCs are unscripted
- V Matrix has display issues
- Boss skill effects are partial
- Rune system non-functional
