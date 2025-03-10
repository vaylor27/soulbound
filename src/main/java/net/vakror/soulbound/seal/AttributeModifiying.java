package net.vakror.soulbound.seal;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public interface AttributeModifiying {
    ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeModifiers = new ImmutableMultimap.Builder<>();

    default Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        return attributeModifiers.build();
    }
}
