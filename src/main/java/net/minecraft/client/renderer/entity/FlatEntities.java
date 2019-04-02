package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid="flatentities", name="Flat Entities", version="1.0")
@EventBusSubscriber
public class FlatEntities
{
	@SubscribeEvent
	public static void onRenderLivingPre(RenderLivingEvent.Pre<EntityLivingBase> event)
	{
		event.setCanceled(true);
		method_4054(event.getRenderer(), event.getEntity(), event.getX(), event.getY(), event.getZ(), 0.0F, event.getPartialRenderTick());
	}

	public static float method_17821(float float_1, float float_2, float float_3) {
		return float_2 + float_1 * MathHelper.wrapDegrees(float_3 - float_2);
	}

	public static float lerp(float float_1, float float_2, float float_3) {
		return float_2 + float_1 * (float_3 - float_2);
	}

	protected static void method_20283(double double_1, double double_2, EntityLivingBase livingEntity_1, float float_1) {
		double double_3 = Math.atan2(double_2, double_1) / 3.141592653589793D * 180.0D;
		double double_4 = float_1 - double_3;
		double double_5 = Math.floor(double_4 / 45.0D) * 45.0D;
		GlStateManager.rotate((float)double_3, 0.0F, 1.0F, 0.0F);
		GlStateManager.scale(0.02F, 1.0F, 1.0F);
		GlStateManager.rotate((float)double_5, 0.0F, 1.0F, 0.0F);
	}

	public static void method_4054(RenderLivingBase<EntityLivingBase> renderer, EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
		renderer.mainModel.swingProgress = renderer.getSwingProgress(entity, partialTicks);
		renderer.mainModel.isRiding = entity.isBeingRidden();
		renderer.mainModel.isChild = entity.isChild();

		try {
			float float_3 = method_17821(partialTicks, entity.prevRenderYawOffset, entity.renderYawOffset);
			float float_4 = method_17821(partialTicks, entity.prevRotationYawHead, entity.rotationYawHead);
			float float_5 = float_4 - float_3;
			float float_8;
			if (entity.isBeingRidden() && entity.getRidingEntity() instanceof EntityLivingBase) {
				EntityLivingBase livingEntity_2 = (EntityLivingBase)entity.getRidingEntity();
				float_3 = method_17821(partialTicks, livingEntity_2.prevRenderYawOffset, livingEntity_2.renderYawOffset);
				float_5 = float_4 - float_3;
				float_8 = MathHelper.wrapDegrees(float_5);
				if (float_8 < -85.0F) {
					float_8 = -85.0F;
				}

				if (float_8 >= 85.0F) {
					float_8 = 85.0F;
				}

				float_3 = float_4 - float_8;
				if (float_8 * float_8 > 2500.0F) {
					float_3 += float_8 * 0.2F;
				}

				float_5 = float_4 - float_3;
			}

			float float_7 = lerp(partialTicks, entity.prevCameraPitch, entity.cameraPitch);
			GlStateManager.translate((float)x, (float)y, (float)z);
			float_8 = renderer.handleRotationFloat(entity, partialTicks);
			renderer.applyRotations(entity, float_8, 0.0F, partialTicks);
			float float_9 = renderer.prepareScale(entity, partialTicks);
			method_20283(x, z, entity, float_3);
			float float_10 = 0.0F;
			float float_11 = 0.0F;
			if (!entity.isBeingRidden() && !entity.isDead && entity.getHealth() > 0.0F) {
				float_10 = lerp(partialTicks, entity.prevLimbSwingAmount, entity.limbSwingAmount);
				float_11 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);
				if (entity.isChild()) {
					float_11 *= 3.0F;
				}

				if (float_10 > 1.0F) {
					float_10 = 1.0F;
				}
			}

			GlStateManager.enableAlpha();
			renderer.mainModel.setLivingAnimations(entity, float_11, float_10, partialTicks);
			renderer.mainModel.setRotationAngles(float_11, float_10, float_8, float_5, float_7, float_9, entity);
			boolean boolean_1;
			if (renderer.renderOutlines) {
				boolean_1 = renderer.setScoreTeamColor(entity);
				GlStateManager.enableColorMaterial();
				GlStateManager.enableOutlineMode(renderer.getTeamColor(entity));
				if (!renderer.renderOutlines) {
					renderer.renderModel(entity, float_11, float_10, float_8, float_5, float_7, float_9);
				}

				if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator()) {
					renderer.renderLayers(entity, float_11, float_10, partialTicks, float_8, float_5, float_7, float_9);
				}

				GlStateManager.disableOutlineMode();
				GlStateManager.disableColorMaterial();
				if (boolean_1) {
					renderer.unsetScoreTeamColor();
				}
			} else {
				boolean_1 = renderer.setDoRenderBrightness(entity, partialTicks);
				renderer.renderModel(entity, float_11, float_10, float_8, float_5, float_7, float_9);
				if (boolean_1) {
					renderer.unsetBrightness();
				}

				GlStateManager.depthMask(true);
				if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator()) {
					renderer.renderLayers(entity, float_11, float_10, partialTicks, float_8, float_5, float_7, float_9);
				}
			}

			GlStateManager.disableRescaleNormal();
		} catch (Exception var19) {
		}

		GlStateManager.bindTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.enableTexture2D();
		GlStateManager.bindTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.enableCull();
		GlStateManager.popMatrix();

		if (!renderer.renderOutlines) {
			renderer.renderName(entity, x, y, z);
		}
	}
}
