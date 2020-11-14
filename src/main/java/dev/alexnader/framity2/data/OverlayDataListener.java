package dev.alexnader.framity2.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static dev.alexnader.framity2.Framity2.META;

public class OverlayDataListener implements SimpleResourceReloadListener<Collection<Identifier>> {
    private final Map<Ingredient, Identifier> triggers = new HashMap<>();

    public Optional<Identifier> getOverlayId(ItemStack stack) {
        return triggers.entrySet().stream().filter(e -> e.getKey().test(stack)).map(Map.Entry::getValue).findFirst();
    }

    public boolean hasOverlay(ItemStack stack) {
        return getOverlayId(stack).isPresent();
    }

    @Override
    public CompletableFuture<Collection<Identifier>> load(ResourceManager resourceManager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            triggers.clear();

            return resourceManager.findResources("framity/overlays", s -> s.endsWith(".json"));
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(Collection<Identifier> identifiers, ResourceManager resourceManager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            for (Identifier id : identifiers) {
                try {
                    JsonElement element = new Gson().fromJson(new BufferedReader(new InputStreamReader(resourceManager.getResource(id).getInputStream())), JsonElement.class);

                    if (!element.isJsonObject()) {
                        throw new JsonParseException("Invalid JSON: expected an object.");
                    }

                    JsonObject obj = element.getAsJsonObject();

                    if (!obj.has("trigger")) {
                        throw new JsonParseException("Invalid JSON: expected the key `trigger`.");
                    }

                    triggers.put(Ingredient.fromJson(obj.get("trigger")), id);
                } catch (Exception e) {
                    META.LOGGER.warn("Exception while parsing overlay: " + e);
                }
            }
        }, executor);
    }

    @Override
    public Identifier getFabricId() {
        return null;
    }
}
