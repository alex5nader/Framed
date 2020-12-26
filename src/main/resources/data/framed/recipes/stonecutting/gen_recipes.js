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
    ["pressure_plate_frame",1],
    // ["torch_frame"],
    // ["trapdoor_frame"],
];

for (const source of sources) {
    for (const [result, count] of results) {
        let name = source + "_to_" + result + ".json";

        let recipe = {
            type: "minecraft:stonecutting",
            ingredient: {
                item: "framed:" + source,
            },
            result: "framed:" + result,
            count,
        }

        await Deno.writeTextFile(name, JSON.stringify(recipe))
    }
}