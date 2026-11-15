package dev.stevecount.mixin;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Constructor;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererPool;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Player Emotes 2.0.3 supplies legacy renderer instances; NeoForge .102 expects pooled factories. */
@Mixin(value=PictureInPictureRendererPool.class,remap=false)
abstract class PlayerEmotesPreviewCompatMixin {
    @ModifyVariable(method="createPools",at=@At("HEAD"),argsOnly=true)
    private static List<?> stevecount$previewFactories(List<?> registrations) {
        ArrayList<Object> converted=null;
        for(int i=0;i<registrations.size();i++) {
            Object entry=registrations.get(i);
            if(entry instanceof PictureInPictureRenderer<?> renderer && entry.getClass().getName().equals("com.yamikhal.playeremotes.client.preview.PreviewPictures$Renderer")) {
                if(converted==null)converted=new ArrayList<>(registrations);
                converted.set(i,stevecount$registration(renderer));
            }
        }
        if(converted!=null)com.mojang.logging.LogUtils.getLogger().info("SteveCount adapted legacy Player Emotes preview renderers to NeoForge pooled registrations");
        return converted==null?registrations:converted;
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    private static PictureInPictureRendererRegistration<?> stevecount$registration(PictureInPictureRenderer<?> renderer) {
        try {
            Constructor<?> constructor=renderer.getClass().getDeclaredConstructor(Class.class);
            if(!constructor.trySetAccessible())throw new IllegalAccessException("Preview renderer constructor is inaccessible");
            Class<?> state=renderer.getRenderStateClass();
            return new PictureInPictureRendererRegistration(state,()->{
                try {return (PictureInPictureRenderer)constructor.newInstance(state);}
                catch(ReflectiveOperationException e) {throw new IllegalStateException("Cannot create Player Emotes preview renderer",e);}
            });
        }catch(ReflectiveOperationException e) {throw new IllegalStateException("Cannot adapt Player Emotes preview registration",e);}
    }
}
