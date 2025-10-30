# 🏆 Awards Plugin

Et avanceret awards system til Minecraft servere, der belønner spillere baseret på deres spilletid med penge og items.

## 📋 Features

### 🎯 **Core Funktionalitet**
- **Spilletids tracking** - Automatisk registrering af spilletid
- **Sekventielle awards** - Spillere skal unlock awards i rækkefølge
- **Fleksible belønninger** - Penge, items eller begge dele
- **JSON-baseret progress** - Effektiv data lagring
- **Real-time progress** - Se præcis hvor lang tid der er tilbage

### 💾 **Database Support**
- **SQLite** (standard) - Ingen setup påkrævet
- **MySQL** - For større servere med ekstern database
- **JSON progress tracking** - Ubegrænset antal awards

### 🎁 **Award Typer**
- `"money"` - Kun penge via Vault/Economy
- `"items"` - Kun Minecraft items
- `"both"` - Både penge OG items i samme award

### 🖱️ **Hover Funktionalitet**
- **Item preview** - Hover over awards for at se items
- **Konfigurerbar** - Slå hover til/fra via config
- **Smart parsing** - Automatisk item navne konvertering

## 🚀 Installation

1. **Download** plugin JAR fil
2. **Placer** i `plugins/` mappen
3. **Installer dependencies:**
   - [Vault](https://www.spigotmc.org/resources/vault.34315/)
   - Et economy plugin (f.eks. EssentialsX)
4. **Genstart** serveren
5. **Tilføj licens nøgle** i `config.yml`:
   ```yaml
   Core:
     license: "din-licens-nøgle-her"
   ```
6. **Konfigurer** `config.yml` efter behov

## ⚙️ Konfiguration

### Licens og Core Setup
```yaml
Core:
  prefix: "&8[&6Awards&8]"
  license: "din-licens-nøgle-her"  # PÅKRÆVET
```

### Database Setup
```yaml
database:
  type: "mysql"  # eller "sqlite"
  mysql:
    host: "localhost"
    port: 3306
    database: "awards"
    username: "username"
    password: "password"
    useSSL: false
```

### Award Eksempel
```yaml
awards:
  1:
    time: 60              # Minutter
    type: "money"         # money, items eller both
    money: 5000
    items: []
    message: "Du har spillet i 1 time og modtager $5.000"
  
  5:
    time: 600
    type: "both"          # Giver både penge OG items
    money: 25000
    items:
      - "diamond 10"
      - "emerald 5"
    message: "Du har spillet i 10 timer og modtager $25.000 + items"
```

### Hover Indstillinger
```yaml
hover:
  enabled: true           # Aktiver hover funktionalitet
  show_items: true        # Vis items i hover tekst
```

## 🎮 Kommandoer

| Kommando | Beskrivelse | Permission |
|----------|-------------|------------|
| `/awards` | Vis dine awards og progress | - |
| `/playtime [spiller]` | Se spilletid | - |
| `/notifications [toggle]` | Administrer notifikationer | - |
| `/resetplaytime [spiller]` | Nulstil spilletid og awards | `awards.reset` |
| `/awardssettings toggle [hover\|items]` | Ændre hover indstillinger | `awards.admin` |

## 🔐 Permissions

- `awards.admin` - Tillader at ændre indstillinger (default: op)

## 📊 Sådan Fungerer Det

### Sekventiel Progression
1. Spillere starter med Award 1
2. Kan kun unlock næste award når forrige er unlocked
3. Forhindrer "spring over" til store belønninger
4. Skaber meningsfuld progression

### Progress Visning
```
Award 1: ✓ Unlocked ($5000)
Award 2: ✓ Unlocked ($10000)
Award 3: 2t 30m tilbage ($15000 + Items)  ← Hover for items
Award 4: 🔒 Låst (unlock award 3 først) ($20000)
```

### JSON Data Format
```json
{
  "1": {"isUnlocked": true, "timeLeft": 0},
  "2": {"isUnlocked": true, "timeLeft": 0},
  "3": {"isUnlocked": false, "timeLeft": 150}
}
```

## 🎯 Award Progression Eksempel

| Award | Tid | Type | Belønning | Beskrivelse |
|-------|-----|------|-----------|-------------|
| 1 | 1 time | money | $5,000 | Starter belønning |
| 2 | 2 timer | money | $10,000 | Tidlig progression |
| 3 | 3 timer | both | $15,000 + items | Første kombinerede |
| 5 | 10 timer | both | $25,000 + diamanter | Betydelig milestone |
| 10 | 7 dage | both | $150,000 + items | Ugentlig dedication |
| 16 | 365 dage | both | $5,000,000 + elytra | Årlig legende |

## 🛠️ Tekniske Detaljer

### Dependencies
- **Paper/Spigot** 1.21+
- **Vault** (economy integration)
- **LicenseGate** (licens verificering)
- **Gson** (JSON håndtering)
- **MySQL Connector** (hvis MySQL bruges)

## 🐛 Troubleshooting

### Plugin loader ikke
- **Licens fejl**: Tjek at korrekt licens nøgle er indsat i config.yml
- **IP verificering**: Plugin verificerer server IP - kontakt support hvis problemer
- Tjek at Vault er installeret
- Verificer at economy plugin er aktivt
- Se server logs for fejl beskeder

### Database forbindelse fejler
- MySQL: Tjek host, port, credentials
- SQLite: Tjek file permissions i plugin folder

### Awards gives ikke
- Verificer economy plugin integration
- Tjek at items er gyldige Material navne
- Se console for warnings

## 📝 Changelog

### v1.0.0
- ✅ Grundlæggende awards system
- ✅ SQLite og MySQL support
- ✅ Sekventiel progression
- ✅ Hover funktionalitet
- ✅ JSON-baseret progress tracking
- ✅ "Both" award type support
- ✅ LicenseGate licens system
- ✅ IP-baseret server verificering