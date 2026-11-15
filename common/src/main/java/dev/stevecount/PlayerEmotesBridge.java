package dev.stevecount;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Optional adapter for Player Emotes 2.0.3. No copied animations, networking or player playback. */
public final class PlayerEmotesBridge {
    record Choice(String id,String name,Object animation,double length,boolean once) {}
    private static Api api;
    private static boolean checked;
    private static String status="Install Player Emotes 2.0.3 for Minecraft 26.3.";
    private static Choice active;
    public static int appliedFrames;
    static String status() {available();return status;}
    static boolean available() {
        if(!checked) {
            checked=true;
            try {api=new Api();status="Solo animations from installed Player Emotes.";}
            catch(ClassNotFoundException e) {api=null;}
            catch(ReflectiveOperationException | LinkageError | RuntimeException e) {fail(e);}
        }
        return api!=null;
    }
    private static void fail(Throwable error) {
        api=null;active=null;checked=true;
        status="Player Emotes adapter unavailable; use version 2.0.3 ("+error.getClass().getSimpleName()+").";
        com.mojang.logging.LogUtils.getLogger().warn("SteveCount disabled optional Player Emotes adapter: {}",error.toString());
    }
    static List<Choice> choices() {
        if(!available())return List.of();
        try {
            var result=new ArrayList<Choice>();
            for(Object emote:(List<?>)api.all.invoke(null)) {
                if(api.partner.invoke(emote)!=null || api.prop.invoke(emote)!=null || !((List<?>)api.props.invoke(emote)).isEmpty())continue;
                Object id=api.id.invoke(emote);
                Object animation=null;
                for(Object animationId:(List<?>)api.animations.invoke(emote)) {
                    animation=api.getAnimation.invoke(null,animationId);
                    if(animation!=null)break;
                }
                if(animation==null || !((Map<?,?>)api.animationProps.invoke(animation)).isEmpty())continue;
                double length=((Number)api.length.invoke(animation)).doubleValue();
                if(!Double.isFinite(length) || length<=0 || length>600)continue;
                String name=((Component)api.name.invoke(emote)).getString();
                result.add(new Choice(id.toString(),name,animation,length,api.loop.invoke(animation).toString().equals("ONCE")));
            }
            result.sort(Comparator.comparing(Choice::name,String.CASE_INSENSITIVE_ORDER).thenComparing(Choice::id));
            status=result.size()+" solo emotes. Props and partner emotes excluded.";
            return List.copyOf(result);
        }catch(ReflectiveOperationException | LinkageError | RuntimeException e) {fail(e);return List.of();}
    }
    static void select(Choice choice) {active=choice;}
    static void stop() {active=null;}
    static int duration(String id) {
        if(active==null || !id.equals("PlayerEmotes:"+active.id()))return 0;
        return active.once()?(int)Math.ceil(active.length()*1000):Integer.MAX_VALUE;
    }
    static Object frame(String id,float seconds) {
        if(api==null || active==null || !id.equals("PlayerEmotes:"+active.id()))return null;
        try {
            // Five-argument public constructor uses Player Emotes' own local query context.
            return api.frame.newInstance(active.animation(),(double)Math.max(0,seconds),1f,false,false);
        }catch(ReflectiveOperationException | LinkageError | RuntimeException e) {fail(e);return null;}
    }
    /** Runs only for our render-state marker. Ordinary players are never assigned a frame. */
    public static boolean posePlayer(HumanoidModel<?> model,BuddyRenderState state) {
        if(state.astronaut || state.playerEmoteFrame==null || api==null)return false;
        try {
            api.poseModel.invoke(null,model,state.playerEmoteFrame);
            applyRoot(model.root(),state);
            seat(state,model.rightLeg,model.leftLeg);
            if(state.headRide)for(ModelPart leg:new ModelPart[]{model.rightLeg,model.leftLeg}) {
                Object bend=api.activeBend.invoke(null,leg);if(bend!=null)api.bendActive.setBoolean(bend,false);
            }
            appliedFrames++;
            return true;
        }catch(ReflectiveOperationException | LinkageError | RuntimeException e) {model.resetPose();fail(e);return false;}
    }
    static void poseAstronaut(ModelPart root,ModelPart[] parts,BuddyRenderState state) {
        if(state.playerEmoteFrame==null || api==null)return;
        try {
            api.sample.invoke(state.playerEmoteFrame,api.pose);
            var values=(float[][])api.parts.get(api.solver);
            for(int i=0;i<parts.length;i++) {
                ModelPart p=parts[i];float[] v=values[api.modelIndices[i]];
                v[0]=p.x;v[1]=p.y;v[2]=p.z;v[3]=p.xRot;v[4]=p.yRot;v[5]=p.zRot;
                v[6]=p.xScale;v[7]=p.yScale;v[8]=p.zScale;
            }
            api.solve.invoke(api.solver,api.pose,false,1f);
            for(int i=0;i<parts.length;i++) {
                ModelPart p=parts[i];float[] v=values[api.modelIndices[i]];
                p.x=v[0];p.y=v[1];p.z=v[2];p.xRot=v[3];p.yRot=v[4];p.zRot=v[5];
                p.xScale=v[6];p.yScale=v[7];p.zScale=v[8];
            }
            applyRoot(root,state);
            seat(state,parts[4],parts[5]);
            appliedFrames++;
        }catch(ReflectiveOperationException | LinkageError | RuntimeException e) {root.getAllParts().forEach(ModelPart::resetPose);fail(e);}
    }
    private static void seat(BuddyRenderState state,ModelPart right,ModelPart left) {
        if(state.headRide) {
            right.resetPose();left.resetPose();right.xRot=left.xRot=-1.4f;right.yRot=.3f;left.yRot=-.3f;
        }
    }
    private static void applyRoot(ModelPart root,BuddyRenderState state) throws ReflectiveOperationException {
        if(state.headRide)return; // Stay seated: no imported flips, jumps or root motion on the player's head.
        api.sample.invoke(state.playerEmoteFrame,api.pose);
        float rx=-api.value(api.rotation,0),ry=-api.value(api.rotation,1),rz=api.value(api.rotation,2);
        float sx=api.value(api.scale,0),sy=api.value(api.scale,1),sz=api.value(api.scale,2);
        Vector3f pivot=new Vector3f(0,12*sy,0).rotate(new Quaternionf().rotationZYX(rz,ry,rx));
        root.x=-api.value(api.position,0)*16-pivot.x;
        root.y=-api.value(api.position,1)*16+12-pivot.y;
        root.z=api.value(api.position,2)*16-pivot.z;
        root.xRot=rx;root.yRot=ry;root.zRot=rz;
        root.xScale=sx;root.yScale=sy;root.zScale=sz;
    }
    private static final class Api {
        final Method all,animations,id,name,partner,prop,props,getAnimation,length,loop,animationProps,sample,poseModel,solve,rotation,position,scale,activeBend;
        final Constructor<?> frame;
        final Object pose,solver,body;
        final Field parts,bendActive;
        final int[] modelIndices;
        Api() throws ReflectiveOperationException {
            var registry=Class.forName("com.yamikhal.playeremotes.client.emote.EmoteRegistry");
            var emote=Class.forName("com.yamikhal.playeremotes.client.emote.Emote");
            var animation=Class.forName("com.yamikhal.playeremotes.anim.EmoteAnimation");
            var poseClass=Class.forName("com.yamikhal.playeremotes.anim.Pose");
            var solverClass=Class.forName("com.yamikhal.playeremotes.anim.PoseSolver");
            var part=Class.forName("com.yamikhal.playeremotes.anim.Part");
            var frameClass=Class.forName("com.yamikhal.playeremotes.client.animation.EmotePlayback$Frame");
            all=registry.getMethod("all");animations=emote.getMethod("animations");
            id=emote.getMethod("id");name=emote.getMethod("name");partner=emote.getMethod("partner");prop=emote.getMethod("prop");props=emote.getMethod("props");
            getAnimation=Class.forName("com.yamikhal.playeremotes.client.animation.AnimationRegistry").getMethod("get",Identifier.class);
            length=animation.getMethod("length");loop=animation.getMethod("loop");animationProps=animation.getMethod("props");
            frame=frameClass.getConstructor(animation,double.class,float.class,boolean.class,boolean.class);
            sample=frameClass.getMethod("sample",poseClass);
            poseModel=Class.forName("com.yamikhal.playeremotes.client.animation.EmoteRenderer").getMethod("poseModel",HumanoidModel.class,frameClass);
            activeBend=Class.forName("com.yamikhal.playeremotes.client.animation.EmoteRenderer").getMethod("activeBend",ModelPart.class);
            bendActive=Class.forName("com.yamikhal.playeremotes.anim.Bend").getField("active");
            pose=poseClass.getConstructor().newInstance();solver=solverClass.getConstructor().newInstance();
            solve=solverClass.getMethod("solve",poseClass,boolean.class,float.class);parts=solverClass.getField("parts");
            body=part.getField("BODY").get(null);
            Object[] modelParts=(Object[])part.getField("MODEL_PARTS").get(null);
            modelIndices=Arrays.stream(modelParts).mapToInt(p->((Enum<?>)p).ordinal()).toArray();
            rotation=poseClass.getMethod("rotation",part,int.class);position=poseClass.getMethod("position",part,int.class);scale=poseClass.getMethod("scale",part,int.class);
        }
        float value(Method method,int axis) throws ReflectiveOperationException {
            double value=((Number)method.invoke(pose,body,axis)).doubleValue();
            return Double.isFinite(value)?(float)value:method==scale?1:0;
        }
    }
}
