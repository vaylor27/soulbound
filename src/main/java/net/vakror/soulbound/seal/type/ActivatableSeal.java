package net.vakror.soulbound.seal.type;

import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.context.UseOnContext;
import net.vakror.soulbound.seal.AttributeModifiying;

public abstract class ActivatableSeal extends BaseSeal implements AttributeModifiying {

    public final float swingSpeed;

    public ActivatableSeal(ResourceLocation id, float swingSpeed) {
        super(id, true);
        this.swingSpeed = swingSpeed;
    }

    public abstract InteractionResult useAction(UseOnContext context);

    public abstract float getDamage();

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        if (!attributeModifiers.build().containsKey(Attributes.ATTACK_SPEED)) {
            attributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(getId() + "_swing_speed", -swingSpeed, AttributeModifier.Operation.ADDITION));
        }
        return AttributeModifiying.super.getAttributeModifiers();
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }
}
