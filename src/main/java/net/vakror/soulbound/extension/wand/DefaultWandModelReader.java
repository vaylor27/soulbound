package net.vakror.soulbound.extension.wand;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.vakror.soulbound.model.wand.api.AbstractQuadsProcessor;
import net.vakror.soulbound.model.wand.api.AbstractWandLayer;
import net.vakror.soulbound.model.wand.api.IWandModelReader;
import net.vakror.soulbound.model.wand.api.LayerRenderData;

import java.util.ArrayList;
import java.util.List;

public class DefaultWandModelReader implements IWandModelReader {
    @Override
    public List<AbstractWandLayer> getLayers(JsonObject model) {
        List<AbstractWandLayer> layers = new ArrayList<>();
        ResourceLocation wandLocation = new ResourceLocation("");

        if (model.has("wand")) {
            wandLocation = new ResourceLocation(model.get("wand").getAsString());
        }
        layers.add(new DefaultWandTextureRenderLayer(wandLocation));

        return layers;
    }

    @Override
    public List<AbstractQuadsProcessor> getQuadsProcessors(JsonObject object, LayerRenderData data) {
        return List.of(new RemoveDuplicateQuadsProcessor());
    }
}
