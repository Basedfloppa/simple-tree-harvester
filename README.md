# 🌲 Simple Tree Harvester

[![Modrinth](https://img.shields.io/modrinth/dt/simple-tree-harvester?logo=modrinth&label=Downloads)](https://modrinth.com/mod/simpletreeharvester)
[![Version](https://img.shields.io/modrinth/v/simple-tree-harvester?logo=modrinth)](https://modrinth.com/mod/simpletreeharvester/versions)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-blue)](https://fabricmc.net/)

A lightweight **Fabric mod** that breaks entire trees when you break a log while sneaking with an axe.  
Made because other tree harvesters don’t pass **player data** when breaking blocks — this one does.

---

## ⚙️ Config

File: 
```
/config/simple-tree-harvester.json
```

Or configure in-game through ModMenu (requires [Cloth Config](https://modrinth.com/mod/cloth-config)).  

Example:
```json
{
  "maxLogs": 128,
  "maxFoliage": 512,
  "ignoreLimits": false,
  "maxRadius": 8,
  "maxVertical": 48,
  "clumpDrops": false,
  "placeItemsInInventory": false,
  "requireFoliageAlongTrunk": true,
  "leafCheckRadius": 3,
  "minLeaves": 3,
  "breakOverworldLeaves": false,
  "breakNetherWartAndShroomlight": false,
  "breakMushroomCaps": false,
  "breakChorusFlowers": true,
  "foliageSeedNeighborRadius": 1
}
```

🧩 Requirements

* Fabric Loader
* Fabric API
* Cloth Config (optional)
* Mod Menu (optional)