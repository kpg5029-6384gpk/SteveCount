package dev.stevecount;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import java.util.*;

/** Original smooth ellipsoid mesh, not a recolored Minecraft player or stacked boxes. */
public final class AstronautModel extends EntityModel<BuddyRenderState> {
    private static final boolean SMOKE=Boolean.getBoolean("stevecount.followerSmoke");
    public static int compiledMeshes;
    private static final class Holder {static final AstronautModel MODEL=new AstronautModel();}
    public static AstronautModel instance() {return Holder.MODEL;}
    private final ModelPart head,body,rightArm,leftArm,rightLeg,leftLeg;
    private static final int HOOD=0xFF59616E,TRIM=0xFF353445,GLASS=0xFF281735,WHITE=0xFFE4EFF1,BLUE=0xFF668ED0;
    private AstronautModel() {
        super(build());
        head=root.getChild("head");body=root.getChild("body");
        rightArm=root.getChild("right_arm");leftArm=root.getChild("left_arm");
        rightLeg=root.getChild("right_leg");leftLeg=root.getChild("left_leg");
    }
    private static ModelPart build() {
        var parts=new LinkedHashMap<String,ModelPart>();
        parts.put("head",part(0,0,0,
            orb(0,-6,0,7.8f,7.8f,7.1f,TRIM,24,16),
            orb(0,-6,-.55f,7.45f,7.45f,7.0f,WHITE,24,16),
            orb(0,-6,-1.25f,6.95f,6.95f,6.7f,GLASS,24,16),
            orb(-2.1f,-10.2f,-6.65f,2.5f,.65f,.5f,0xFF998DA6,16,8),
            orb(3.6f,-5.8f,-6.9f,.7f,1.1f,.28f,0xFF9D90B3,12,8),
            orb(3.8f,-3.9f,-6.4f,.32f,.4f,.2f,0xFFB1A9BC,10,6),
            orb(-7.4f,-5.5f,0,1.3f,3.9f,3.2f,TRIM,16,10),
            orb(-8.1f,-5.5f,-.1f,.65f,3.4f,2.75f,WHITE,16,10),
            orb(7.4f,-5.5f,0,1.3f,3.9f,3.2f,TRIM,16,10),
            orb(8.1f,-5.5f,-.1f,.65f,3.4f,2.75f,WHITE,16,10)));
        parts.put("body",part(0,0,0,
            orb(0,6,0,4.6f,6.3f,2.8f,HOOD,20,12),
            orb(0,.7f,.5f,4.8f,2.0f,3.0f,TRIM,20,10),
            orb(0,1.1f,.3f,4.4f,1.5f,2.9f,HOOD,20,10),
            orb(0,9,-2.5f,3.55f,1.9f,.6f,TRIM,16,8),
            orb(0,8.8f,-2.8f,3.2f,1.55f,.45f,0xFF7B838C,16,8),
            orb(-1.1f,4.2f,-2.75f,.13f,2.8f,.14f,WHITE,8,8),
            orb(1.1f,4.2f,-2.75f,.13f,2.8f,.14f,WHITE,8,8)));
        parts.put("right_arm",arm(-5.4f));parts.put("left_arm",arm(5.4f));
        parts.put("right_leg",leg(-2.2f));parts.put("left_leg",leg(2.2f));
        return new ModelPart(List.of(),parts);
    }
    private static ModelPart arm(float x) {
        return part(x,2,0,
            orb(0,4,0,2.15f,5.1f,2.25f,HOOD,16,10),
            orb(0,8.2f,-.05f,2.25f,.85f,2.3f,TRIM,16,8),
            orb(0,10.15f,-.15f,1.75f,2.1f,1.55f,WHITE,16,10),
            orb(-1.55f,10,-.7f,.65f,1.15f,.65f,WHITE,12,8),
            orb(-.8f,12,-.35f,.4f,.8f,.6f,WHITE,10,6),
            orb(.1f,12.2f,-.35f,.4f,.9f,.6f,WHITE,10,6),
            orb(.95f,11.9f,-.3f,.4f,.7f,.55f,WHITE,10,6));
    }
    private static ModelPart leg(float x) {
        return part(x,12,0,
            orb(0,5,0,2.45f,6.0f,2.3f,BLUE,16,12),
            orb(0,9.1f,0,2.45f,.8f,2.35f,0xFF4776BB,16,8),
            orb(0,11.55f,-.8f,2.75f,.65f,3.4f,WHITE,16,8),
            orb(0,10.7f,-.8f,2.7f,1.4f,3.35f,0xFF36224D,16,10));
    }
    private static ModelPart part(float x,float y,float z,ModelPart.Cube... cubes) {
        var p=new ModelPart(List.of(cubes),Map.of());
        p.setInitialPose(PartPose.offset(x,y,z));p.loadPose(p.getInitialPose());return p;
    }
    private static RoundMesh orb(float x,float y,float z,float rx,float ry,float rz,int color,int slices,int rings) {
        return new RoundMesh(x,y,z,rx,ry,rz,color,slices,rings);
    }
    @Override public void setupAnim(BuddyRenderState s) {
        super.setupAnim(s);
        head.yRot=s.yRot*(float)Math.PI/180;head.xRot=s.xRot*(float)Math.PI/180;
        float step=(float)Math.sin(s.walkAnimationPos*.6662f)*s.walkAnimationSpeed;
        rightLeg.xRot=step;leftLeg.xRot=-step;rightArm.xRot=-step*.8f;leftArm.xRot=step*.8f;
        if(s.headRide) {rightLeg.xRot=leftLeg.xRot=-1.4f;rightLeg.yRot=.3f;leftLeg.yRot=-.3f;}
        float beat=(float)Math.sin(s.emoteTime*9);
        switch(s.emote) {
            case "Wave" -> {rightArm.xRot=-2.5f;rightArm.zRot=-.4f+beat*.4f;}
            case "Cheer" -> {rightArm.xRot=leftArm.xRot=-2.7f+beat*.15f;rightArm.zRot=-.45f;leftArm.zRot=.45f;}
            case "Dance" -> {rightArm.xRot=beat*1.1f;leftArm.xRot=-beat*1.1f;rightArm.zRot=-.45f;leftArm.zRot=.45f;if(!s.headRide){rightLeg.xRot=-beat*.6f;leftLeg.xRot=beat*.6f;}}
        }
        if(s.swingAnimation>0)rightArm.xRot=-2.2f*(float)Math.sin(s.swingAnimation*Math.PI);
    }
    /** Cached vertices with smooth ellipsoid normals. Native lighting and overlay are retained. */
    static final class RoundMesh extends ModelPart.Cube {
        final float[] vertices;
        final int color;
        RoundMesh(float cx,float cy,float cz,float rx,float ry,float rz,int color,int slices,int rings) {
            super(0,0,cx-rx,cy-ry,cz-rz,rx*2,ry*2,rz*2,0,0,0,false,1,1,Set.of());
            this.color=color;vertices=new float[slices*rings*4*6];int n=0;
            for(int v=0;v<rings;v++)for(int u=0;u<slices;u++) {
                for(int corner=0;corner<4;corner++) {
                    double a=(u+(corner==1||corner==2?1:0))*Math.PI*2/slices;
                    double b=(v+(corner>=2?1:0))*Math.PI/rings;
                    float x=(float)(Math.sin(b)*Math.cos(a)),y=(float)Math.cos(b),z=(float)(Math.sin(b)*Math.sin(a));
                    vertices[n++]=(cx+x*rx)/16;vertices[n++]=(cy+y*ry)/16;vertices[n++]=(cz+z*rz)/16;
                    float nx=x/rx,ny=y/ry,nz=z/rz,len=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);
                    vertices[n++]=nx/len;vertices[n++]=ny/len;vertices[n++]=nz/len;
                }
            }
        }
        @Override public void compile(PoseStack.Pose pose,VertexConsumer consumer,int light,int overlay,int tint) {
            if(SMOKE)compiledMeshes++;
            int r=((color>>16)&255)*((tint>>16)&255)/255,g=((color>>8)&255)*((tint>>8)&255)/255,b=(color&255)*(tint&255)/255;
            for(int i=0;i<vertices.length;i+=6)consumer.addVertex(pose,vertices[i],vertices[i+1],vertices[i+2])
                .setColor(r,g,b,255).setUv(.5f,.5f).setOverlay(overlay).setLight(light)
                .setNormal(pose,vertices[i+3],vertices[i+4],vertices[i+5]);
        }
    }
}
