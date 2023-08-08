package net.vakror.soulbound.model.wand;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.vakror.soulbound.model.models.ActiveSealModels;
import net.vakror.soulbound.model.models.WandModels;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/* Used in read json of meals */
public enum WandModelLoader implements IGeometryLoader<WandModel> {
	INSTANCE;

	public static final List<ResourceLocation> textures = new ArrayList<ResourceLocation>();


	@Override
	public WandModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
		List<TypedTextures> typedTexturesList = new ArrayList<>();
		ResourceLocation wandLocation = new ResourceLocation("");
		int defaultTint = 0;

		if (modelContents.has("wand")) {
			JsonObject wandJsonObject = modelContents.getAsJsonObject("wand");
			wandLocation = new ResourceLocation(wandJsonObject.get("wand").getAsString());
			WandModelLoader.textures.add(wandLocation);
			defaultTint = wandJsonObject.get("default_tint") == null ? 0: wandJsonObject.get("default_tint").getAsInt();

			TypedTextures typedTextures = new TypedTextures(wandJsonObject);
			typedTexturesList.add(typedTextures);

			for (Entry<String, ResourceLocation> entry : typedTextures.getTextures().entrySet()) {
				WandModelLoader.textures.add(entry.getValue());
			}
		}

		return new WandModel(wandLocation, ImmutableList.copyOf(typedTexturesList), defaultTint);
	}

	public static class TypedTextures {
		private final ImmutableMap<String, ResourceLocation> textures;

		private TypedTextures(JsonObject wandObject) {

			Map<String, ResourceLocation> map = new HashMap<>();
			map.put("wand", new ResourceLocation(wandObject.get("wand").getAsString()));
			this.textures = ImmutableMap.copyOf(map);
		}

		public ImmutableMap<String, ResourceLocation> getTextures() {
			return textures;
		}

		@Nullable
		public TextureAtlasSprite getSprite(String name, Function<Material, TextureAtlasSprite> spriteGetter) {
			ResourceLocation location = this.textures.get(name);
			assert location != null;
			@SuppressWarnings("deprecation")
			Material material = new Material(TextureAtlas.LOCATION_BLOCKS, location);
			TextureAtlasSprite sprite = spriteGetter.apply(material);
			if (sprite != null && !sprite.getName().equals(MissingTextureAtlasSprite.getLocation())) {
				return sprite;
			}
			return null;
		}

		@Nullable
		public TextureAtlasSprite getSprite(String name, Function<Material, TextureAtlasSprite> spriteGetter, boolean isWandModel) {
			ResourceLocation location = isWandModel ? WandModels.MODELS.get(name): ActiveSealModels.MODELS.get(name);
			if (location != null) {
				@SuppressWarnings("deprecation")
				Material material = new Material(TextureAtlas.LOCATION_BLOCKS, location);
				TextureAtlasSprite sprite = spriteGetter.apply(material);
				if (sprite != null && !sprite.getName().equals(MissingTextureAtlasSprite.getLocation())) {
					return sprite;
				}
			}
			return null;
		}
	}
}