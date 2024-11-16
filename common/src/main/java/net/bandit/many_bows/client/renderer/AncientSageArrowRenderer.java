package net.bandit.many_bows.client.renderer;

import net.bandit.many_bows.entity.AncientSageArrow;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public class AncientSageArrowRenderer extends ArrowRenderer<AncientSageArrow> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/ancient_arrow.png");

    public AncientSageArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(AncientSageArrow entity) {
        return TEXTURE;
    }
}