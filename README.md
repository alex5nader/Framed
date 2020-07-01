# Framity

Framity is a [Minecraft](https://www.minecraft.net/) mod for the [Fabric](https://fabricmc.net/use/) mod loader.

Framity adds several types of block frame with various shapes. Each
frame can have its texture changed to that of any full-size block in
the game. While flowers, slabs, and similar items, are able to be
placed into the world, they are not able to be placed into a frame.

<p align="center">
    <a href="https://www.curseforge.com/minecraft/mc-mods/fabric-api">
        <img title="Requires Fabric API" height="50" src="https://i.imgur.com/Ol1Tcf8.png">
    </a>
</p>

<p align="center">
    <a title="Fabric Language Kotlin"
       href="https://minecraft.curseforge.com/projects/fabric-language-kotlin"
       target="_blank"
       rel="noopener noreferrer"
       >
        <img style="display: block; margin-left: auto; margin-right: auto;"
             src="https://i.imgur.com/c1DH9VL.png"
             alt=""
             width="171"
             height="50"
             />
    </a>
</p>

## Download

Framity is not currently available, but when it is, it will be available through
its [CurseForge page](https://www.curseforge.com/minecraft/mc-mods/framity).

## Planned Features

The features below are in no particular order, and are not guaranteed to be added.

- Overlay textures
    - ✔ Grass (via wheat seeds)
    - ✔ Snow (via snowball)
    - ✔ Mycelium (via red/brown mushroom)
    - ✔ Path (via dead bush)
    - ✔ Hay (via wheat)
    - ✔ Vines (via vine)
- Frame blocks
    - ✔ Full block
    - ✔ Slab
    - ✔ Stairs
    - ✔ Fence
    - ✔ Fence gate
    - Slope
    - Bed
    - Flower pot
    - Door
    - ✔ Trapdoor
    - Torch
    - Pressure plate
    - Lever
    - Button
    - Path
- Other
    - Independent sides → Each side of the frame can have a different texture  
    - ✔ Glowstone dust → Frame gains light level of 15
    - ✔ Redstone dust → Frame outputs redstone signal of 15
    - Ice shard (via cotton) → Frame is slippery
    
## Thanks
Thanks to:
- The members of the [Fabric Discord](https://discord.gg/v6v4pMv) for their
incredible help and support throughout the creation of Framity.
- [Grondag](https://www.curseforge.com/members/grondagthebarbarian)
for immensely helping me understand the fabric rendering API.
- [Lil Tater Reloaded](https://www.curseforge.com/minecraft/mc-mods/lil-tater-reloaded)
for acting as an example of how to read assets/data.
- [Mineshopper](https://www.curseforge.com/members/mineshopper) for letting
me use assets from the original
[Carpenter's Blocks](https://www.curseforge.com/minecraft/mc-mods/carpenters-blocks).

Without them, this project would never have become more than a dream.

## License

- All assets from and inspired by Carpenter's Blocks are licensed under the [LGPL v2.1](LICENSE_LGPL.md). This includes the following paths:
  - `src/main/resources/assets/framity/textures/block/solid_frame.png`
  - `src/main/resources/assets/framity/textures/block/hollow_frame.ase`
  - `src/main/resources/assets/framity/textures/block/hollow_frame.png`
  - `src/main/resources/assets/framity/textures/item/hammer.png`
  - `src/main/resources/assets/framity/icon.png`
- All other files are licensed under the [MIT License](LICENSE_MIT.md).
