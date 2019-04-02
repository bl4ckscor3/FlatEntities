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
public class FlatEntities //the class is in a minecraft package so accessing RenderLivingBase's protected fields/methods is possible
{
	@SubscribeEvent
	public static void onRenderLivingPre(RenderLivingEvent.Pre<EntityLivingBase> event)
	{
		EntityLivingBase entity = event.getEntity();
		RenderLivingBase<EntityLivingBase> renderer = event.getRenderer();
		float partialTicks = event.getPartialRenderTick();
		double x = event.getX();
		double y = event.getY();
		double z = event.getZ();

		event.setCanceled(true); //disable normal rendering

		//vanilla code
		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
		renderer.mainModel.swingProgress = renderer.getSwingProgress(entity, partialTicks);
		boolean shouldSit = entity.isRiding() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
		renderer.mainModel.isRiding = shouldSit;
		renderer.mainModel.isChild = entity.isChild();

		try
		{
			float f = renderer.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
			float f1 = renderer.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
			float f2 = f1 - f;

			if(shouldSit && entity.getRidingEntity() instanceof EntityLivingBase)
			{
				EntityLivingBase entitylivingbase = (EntityLivingBase)entity.getRidingEntity();
				f = renderer.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
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

			float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
			renderer.renderLivingAt(entity, x, y, z);
			float f8 = renderer.handleRotationFloat(entity, partialTicks);
			renderer.applyRotations(entity, f8, f, partialTicks);
			float f4 = renderer.prepareScale(entity, partialTicks);
			float f5 = 0.0F;
			float f6 = 0.0F;

			prepareFlatRender(x, z, f);

			if(!entity.isRiding())
			{
				f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
				f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

				if(entity.isChild())
					f6 *= 3.0F;

				if(f5 > 1.0F)
					f5 = 1.0F;
				f2 = f1 - f; // Forge: Fix MC-1207

			}

			GlStateManager.enableAlpha();
			renderer.mainModel.setLivingAnimations(entity, f6, f5, partialTicks);
			renderer.mainModel.setRotationAngles(f6, f5, f8, f2, f7, f4, entity);

			if(renderer.renderOutlines)
			{
				boolean flag1 = renderer.setScoreTeamColor(entity);
				GlStateManager.enableColorMaterial();
				GlStateManager.enableOutlineMode(renderer.getTeamColor(entity));

				if(!renderer.renderMarker)
					renderer.renderModel(entity, f6, f5, f8, f2, f7, f4);

				if(!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator())
					renderer.renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, f4);

				GlStateManager.disableOutlineMode();
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

				if(!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator())
					renderer.renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, f4);
			}

			GlStateManager.disableRescaleNormal();
		}
		catch (Exception exception) {}

		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.enableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.enableCull();
		GlStateManager.popMatrix();

		//from super call
		if(!renderer.renderOutlines)
			renderer.renderName(entity, x, y, z);
		//end vanilla code

		//call render post event
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<EntityLivingBase>(entity, renderer, partialTicks, x, y, z));
	}

	public static void prepareFlatRender(double x, double z, float f)
	{
		double angle1 = Math.atan2(z, x) / 3.141592653589793D * 180.0D;
		double angle2 = Math.floor((f - angle1) / 45.0D) * 45.0D;

		GlStateManager.rotate((float)angle1, 0.0F, 1.0F, 0.0F);
		GlStateManager.scale(0.02F, 1.0F, 1.0F);
		GlStateManager.rotate((float)angle2, 0.0F, 1.0F, 0.0F);
	}
}
