package net.vakror.soulbound.seal.function.amplify;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class HasteAmplifyFunction extends AmplifyFunction{
    private final double miningSpeedIncrease;

    public HasteAmplifyFunction(double miningSpeedIncrease) {
        this.miningSpeedIncrease = miningSpeedIncrease;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = new ImmutableMultimap.Builder<>();
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.randomUUID().toString(), miningSpeedIncrease, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }
}
