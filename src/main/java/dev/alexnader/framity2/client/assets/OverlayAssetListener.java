package dev.alexnader.framity2.client.assets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.alexnader.framity2.client.assets.overlay.Overlay;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.profiler.Profiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static dev.alexnader.framity2.Framity2.META;

public class OverlayAssetListener implements SimpleResourceReloadListener<Collection<Identifier>> {
    private final Map<Identifier, Overlay> overlayInfoMap = new HashMap<>();

    public Optional<Overlay> getOverlayFor(final Identifier id) {
        return Optional.ofNullable(overlayInfoMap.get(id));
    }

    @Override
    public CompletableFuture<Collection<Identifier>> load(final ResourceManager resourceManager, final Profiler profiler, final Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            overlayInfoMap.clear();

            return resourceManager.findResources("framity/overlays", s -> s.endsWith(".json"));
        }, executor);
    }

    private DataResult<Unit> parseOverlayAndDependencies(final ResourceManager resourceManager, final Identifier rootOverlayId) {
        final Set<Identifier> loadedDependencies = new HashSet<>();

        if (!overlayInfoMap.containsKey(rootOverlayId)) {
            return parseOverlay(resourceManager, loadedDependencies, rootOverlayId);
        } else {
            return DataResult.success(Unit.INSTANCE);
        }
    }

    private DataResult<Unit> parseOverlay(final ResourceManager resourceManager, final Set<Identifier> loadedDependencies, final Identifier overlayId) {
        final JsonElement element;
        try {
            element = new Gson().fromJson(new BufferedReader(new InputStreamReader(resourceManager.getResource(overlayId).getInputStream())), JsonElement.class);
        } catch (final IOException e) {
            return DataResult.error("Exception while loading an overlay: " );
        }

        final DataResult<Pair<Overlay, JsonElement>> result = Overlay.PARENT_CODEC.decode(JsonOps.INSTANCE, element)
            .flatMap(pair ->
                pair.getFirst().map(parentId -> {
                    if (!loadedDependencies.add(parentId)) {
                        return DataResult.error("Circular dependency: " + pair.getFirst() + " and " + overlayId + ".");
                    } else {
                        parseOverlay(resourceManager, loadedDependencies, parentId);
                        return DataResult.success(Unit.INSTANCE);
                    }
                }).orElse(DataResult.success(Unit.INSTANCE))
            )
            .flatMap(unit -> Overlay.CODEC.decode(JsonOps.INSTANCE, element));

        result.get().mapLeft(pair -> {
            overlayInfoMap.put(overlayId, pair.getFirst());
            return Unit.INSTANCE;
        });

        return result.map(p -> Unit.INSTANCE);
    }

    @Override
    public CompletableFuture<Void> apply(final Collection<Identifier> identifiers, final ResourceManager resourceManager, final Profiler profiler, final Executor executor) {
        return CompletableFuture.runAsync(() -> {
            for (final Identifier id : identifiers) {
                final DataResult<Unit> result = parseOverlayAndDependencies(resourceManager, id);

                result.get().ifRight(partial -> META.LOGGER.warn("Error while parsing overlay \"" + id + "\" : " + partial.message()));
            }
        }, executor);
    }

    private final Identifier id = META.id("assets/overlay");

    @Override
    public Identifier getFabricId() {
        return id;
    }
}
