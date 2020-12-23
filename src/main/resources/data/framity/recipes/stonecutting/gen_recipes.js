const fs = require("fs");

const sources = [
    "block_frame"
];
const results = [
    // ["door_frame"],
    ["fence_frame", 1],
    ["fence_gate_frame", 1],
    ["path_frame", 1],
    ["slab_frame", 2],
    ["stairs_frame", 1],
    // ["torch_frame"],
    // ["trapdoor_frame"],
];

sources.forEach(source => {
    results.forEach(([result, count]) => {
        let name = source + "_to_" + result + ".json";

        let recipe = {
            type: "minecraft:stonecutting",
            ingredient: {
                item: "framed:" + source,
            },
            result: "framed:" + result,
            count,
        }

        fs.writeFileSync(name, JSON.stringify(recipe))
    })
})