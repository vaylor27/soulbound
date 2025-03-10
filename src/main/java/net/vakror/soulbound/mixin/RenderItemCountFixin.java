package net.vakror.soulbound.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.vakror.soulbound.util.ItemCountRenderHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@OnlyIn(Dist.CLIENT)
@Mixin(GuiGraphics.class)
public abstract class RenderItemCountFixin {

	@Shadow @Final private PoseStack pose;


	@Redirect (method = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
			at = @At (value = "INVOKE", target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;"))
	private String render(int i) {
		return ItemCountRenderHandler.getInstance().toConsiseString(i);
	}

	@Redirect (method = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
			at = @At (value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I"))
	private int width(Font renderer, String text) {
		return (int) (renderer.width(text) * ItemCountRenderHandler.getInstance().scale(text));
	}

	@Inject (method = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
			at = @At (value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", shift = At.Shift.AFTER),
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void rescaleText(Font textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci, String string) {
		float f = ItemCountRenderHandler.getInstance().scale(string);
		if (f != 1f) {
			this.pose.translate(x * (1 - f), y * (1 - f) + (1 - f) * 16, 0);
			this.pose.scale(f, f, f);
		}
	}
}