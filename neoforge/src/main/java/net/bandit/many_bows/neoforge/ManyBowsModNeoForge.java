package net.bandit.many_bows.neoforge;

import net.bandit.many_bows.client.renderer.*;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import net.bandit.many_bows.ManyBowsMod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@Mod(ManyBowsMod.MOD_ID)
@EventBusSubscriber(modid = ManyBowsMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ManyBowsModNeoForge {
    public ManyBowsModNeoForge(IEventBus modEventBus) {
        // Run our common setup.
        ManyBowsMod.init();
        modEventBus.addListener(this::onClientSetup);
//        EnvExecutor.runInEnv(Dist.CLIENT, () -> ManyBowsMod::initClient);
    }
    private void onClientSetup(final FMLClientSetupEvent event) {
        // This is where the client-specific code should be registered
        ManyBowsMod.initClient();
    }

    @SubscribeEvent
    public static void onModelRegister(ModelEvent.RegisterAdditional event) {
        // Register any models or textures here
    }
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.FROSTBITE_ARROW.get(), FrostbiteArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.VENOM_ARROW.get(), VenomArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FLAME_ARROW.get(), FlameArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CURSED_FLAME_ARROW.get(), CursedFlameArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.TIDAL_ARROW.get(), TidalArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WIND_PROJECTILE.get(), WindArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.LIGHTNING_ARROW.get(), LightningArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DRAGONS_BREATH_ARROW.get(), DragonsBreathArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SONIC_BOOM_PROJECTILE.get(), SonicBoomProjectileRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ICICLE_JAVELIN.get(), IcicleJavelinRenderer::new);
        event.registerEntityRenderer(EntityRegistry.HUNTER_ARROW.get(), HunterArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SENTINEL_ARROW.get(), SentinelArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.HUNTER_XP_ARROW.get(), HunterXPArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ANCIENT_SAGE_ARROW.get(), AncientSageArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SHULKER_BLAST_PROJECTILE.get(), ShulkerBlastArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.VITALITY_ARROW.get(), VitalityArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ASTRAL_ARROW.get(), AstralArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SPECTRAL_ARROW.get(), SpectralArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.AURORA_ARROW.get(), AuroraArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.RIFT_ENTITY.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.RADIANT_ARROW.get(), RadianceArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DUSK_REAPER_ARROW.get(), DuskArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WEBSTRING_ARROW.get(), WebstringArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.IRONCLAD_ARROW.get(), IroncladArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.TORCHBEARER_ARROW.get(), TorchbearerArrowRenderer::new);
    }
}
