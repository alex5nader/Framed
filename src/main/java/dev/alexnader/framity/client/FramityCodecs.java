package dev.alexnader.framity.client;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alexnader.framity.client.assets.overlay.Offsetter;
import dev.alexnader.framity.client.assets.overlay.OffsetterRegistry;
import dev.alexnader.framity.util.Identifiable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class FramityCodecs {
    public final Codec<Direction> DIRECTION = Codec.STRING.flatXmap(
        name -> {
            Direction result = Direction.byName(name);
            if (result != null) {
                return DataResult.success(result);
            } else {
                return DataResult.error("Invalid direction: " + name);
            }
        },
        d -> DataResult.success(d.toString())
    );

    public final Codec<Offsetter> OFFSETTER = Identifier.CODEC
        .flatXmap(
            id -> {
                final @Nullable Identifiable<Codec<Offsetter>> codec = OffsetterRegistry.get(id);
                if (codec != null) {
                    return DataResult.success(codec);
                } else {
                    return DataResult.error("Invalid offsetter type: " + id);
                }
            },
            codec -> DataResult.success(codec.id())
        )
        .dispatch(
            o -> OffsetterRegistry.get(o.getId()),
            codec -> RecordCodecBuilder.create(inst -> inst.group(
                codec.value().fieldOf("value").forGetter(c -> c)
            ).apply(inst, c -> c))
        );

    public <V> Codec<Map<Direction, V>> sidedMapOf(final Codec<V> valueCodec) {
        final Codec<Pair<List<Direction>, V>> itemCodec = RecordCodecBuilder.create(inst -> inst.group(
            DIRECTION.listOf().fieldOf("sides").forGetter(Pair::getFirst),
            valueCodec.fieldOf("value").forGetter(Pair::getSecond)
        ).apply(inst, Pair::new));

        return itemCodec.listOf().xmap(
            pairs -> pairs.stream().flatMap(pair -> pair.getFirst().stream().map(dir -> new Pair<>(dir, pair.getSecond()))).collect(Pair.toMap()),
            map -> map.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue))
                .entrySet().stream()
                .map(e -> new Pair<>(e.getValue().stream().map(Map.Entry::getKey).collect(Collectors.toList()), e.getKey()))
                .collect(Collectors.toList())
        );
    }
}
