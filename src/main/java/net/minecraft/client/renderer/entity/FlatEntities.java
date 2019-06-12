package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@Mod("flatentities")
@EventBusSubscriber
public class FlatEntities //the class is in a minecraft package so accessing RenderLivingBase's protected fields/methods is possible
{
	@SubscribeEvent
	public static void onRenderLivingPre(RenderLivingEvent.Pre<LivingEntity,EntityModel<LivingEntity>> event)
	{
		LivingEntity entity = event.getEntity();
		LivingRenderer<LivingEntity,EntityModel<LivingEntity>> renderer = event.getRenderer();
		float partialTicks = event.getPartialRenderTick();
		double x = event.getX();
		double y = event.getY();
		double z = event.getZ();

		event.setCanceled(true); //disable normal rendering

		//vanilla code
		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
		renderer.field_77045_g.field_217112_c = renderer.getSwingProgress(entity, partialTicks);
		boolean shouldSit = entity.isPassenger() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
		renderer.field_77045_g.field_217113_d = shouldSit;
		renderer.field_77045_g.isChild = entity.isChild();

		try
		{
			float f = MathHelper.func_219805_h(partialTicks, entity.prevRenderYawOffset, entity.renderYawOffset);
			float f1 = MathHelper.func_219805_h(partialTicks, entity.prevRotationYawHead, entity.rotationYawHead);
			float f2 = f1 - f;

			if(shouldSit && entity.getRidingEntity() instanceof LivingEntity)
			{
				LivingEntity livingentity = (LivingEntity)entity.getRidingEntity();
				f = MathHelper.func_219805_h(partialTicks, livingentity.prevRenderYawOffset, livingentity.renderYawOffset);
				f2 = f1 - f;
				float f3 = MathHelper.wrapDegrees(f2);

				if(f3 < -85.0F)
					f3 = -85.0F;

				if(f3 >= 85.0F)
					f3 = 85.0F;

				f = f1 - f3;

				if(f3 * f3 > 2500.0F)
					f += f3 * 0.2F;

				f2 = f1 - f;
			}

			float f7 = MathHelper.func_219799_g(partialTicks, entity.prevRotationPitch, entity.rotationPitch);
			renderer.renderLivingAt(entity, x, y, z);
			float f8 = renderer.handleRotationFloat(entity, partialTicks);
			renderer.applyRotations(entity, f8, f, partialTicks);
			float f4 = renderer.prepareScale(entity, partialTicks);
			float f5 = 0.0F;
			float f6 = 0.0F;

			prepareFlatRender(x, z, f);

			if(!entity.isPassenger() && entity.isAlive())
			{
				f5 = MathHelper.func_219799_g(partialTicks, entity.prevLimbSwingAmount, entity.limbSwingAmount);
				f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

				if(entity.isChild())
					f6 *= 3.0F;

				if(f5 > 1.0F)
					f5 = 1.0F;
			}

			GlStateManager.enableAlphaTest();
			renderer.field_77045_g.setLivingAnimations(entity, f6, f5, partialTicks);
			renderer.field_77045_g.setRotationAngles(entity, f6, f5, f8, f2, f7, f4);

			if(renderer.renderOutlines)
			{
				boolean flag1 = renderer.setScoreTeamColor(entity);
				GlStateManager.enableColorMaterial();
				GlStateManager.setupSolidRenderingTextureCombine(renderer.getTeamColor(entity));

				if(!renderer.renderMarker)
					renderer.renderModel(entity, f6, f5, f8, f2, f7, f4);

				if(!entity.isSpectator())
					renderer.renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, f4);

				GlStateManager.tearDownSolidRenderingTextureCombine();
				GlStateManager.disableColorMaterial();

				if(flag1)
					renderer.unsetScoreTeamColor();
			}
			else
			{
				boolean flag = renderer.setDoRenderBrightness(entity, partialTicks);
				renderer.renderModel(entity, f6, f5, f8, f2, f7, f4);

				if(flag)
					renderer.unsetBrightness();

				GlStateManager.depthMask(true);

				if(!entity.isSpectator())
					renderer.renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, f4);
			}

			GlStateManager.disableRescaleNormal();
		}
		catch(Exception exception) {}

		GlStateManager.activeTexture(GLX.GL_TEXTURE1);
		GlStateManager.enableTexture();
		GlStateManager.activeTexture(GLX.GL_TEXTURE0);
		GlStateManager.enableCull();
		GlStateManager.popMatrix();

		//from super call
		if(!renderer.renderOutlines)
			renderer.renderName(entity, x, y, z);
		//end vanilla code

		//call render post event
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<LivingEntity,EntityModel<LivingEntity>>(entity, renderer, partialTicks, x, y, z));
	}

	public static void prepareFlatRender(double x, double z, float f)
	{
		double angle1 = Math.atan2(z, x) / 3.141592653589793D * 180.0D;
		double angle2 = Math.floor((f - angle1) / 45.0D) * 45.0D;

		GlStateManager.rotatef((float)angle1, 0.0F, 1.0F, 0.0F);
		GlStateManager.scalef(0.02F, 1.0F, 1.0F);
		GlStateManager.rotatef((float)angle2, 0.0F, 1.0F, 0.0F);
	}
}
