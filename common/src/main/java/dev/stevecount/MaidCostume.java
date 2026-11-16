package dev.stevecount;

import java.nio.file.Files;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;

/** Cosmetic layer for the local player only. Never changes inventory, account skin or Buddy. */
public final class MaidCostume {
    private static final CostumeCursor cursor=new CostumeCursor();
    private static final Identifier SLIM=Identifier.fromNamespaceAndPath("stevecount","maid_slim");
    private static final Identifier WIDE=Identifier.fromNamespaceAndPath("stevecount","maid_wide");
    private static PlayerModel slim,wide;
    private static boolean loaded,attempted;
    private static Object world;
    static PlayerModelType smokeModel;
    public static int renderedFrames,handFrames;
    static String assetStatus="Costume not loaded yet.";
    static void tick() {
        var mc=Minecraft.getInstance();var c=SteveCount.settings;long now=System.currentTimeMillis();
        if(!attempted)reload();
        boolean sameWorld=world==mc.level;world=mc.level;
        if(c.maidUntil>0 && (!c.maidGift || CostumeClock.remaining(c.maidUntil,now)==0))restore();
        boolean armed=sameWorld && loaded && c.maidGift && c.streamerMode && !SteveCount.demo
            && mc.player!=null && mc.level!=null && SteveCount.bridge.snapshot.status().equals("Live");
        if(cursor.accept(SteveCount.bridge.costumes,now/1000d,armed))start();
    }
    static void start() {
        var mc=Minecraft.getInstance();if(mc.player==null || !loaded)return;
        var c=SteveCount.settings;c.maidUntil=CostumeClock.start(System.currentTimeMillis());c.maidWearer=mc.player.getUUID().toString();c.save();
    }
    static void restore() {var c=SteveCount.settings;c.maidUntil=0;c.maidWearer="";c.save();}
    public static boolean active() {
        var mc=Minecraft.getInstance();var c=SteveCount.settings;
        return loaded && c.maidGift && mc.player!=null && CostumeClock.wears(c.maidUntil,c.maidWearer,mc.player.getUUID().toString(),System.currentTimeMillis());
    }
    public static boolean wears(AvatarRenderState state) {var mc=Minecraft.getInstance();return !(state instanceof BuddyRenderState) && active() && state.id==mc.player.getId();}
    public static void extract(AvatarRenderState state) {
        if(!wears(state))return;
        if(Boolean.getBoolean("stevecount.costumeSmoke") && smokeModel!=null) {
            var skin=state.skin;state.skin=new net.minecraft.world.entity.player.PlayerSkin(skin.body(),skin.cape(),skin.elytra(),smokeModel,skin.secure());
        }
        state.showJacket=state.showLeftSleeve=state.showRightSleeve=state.showLeftPants=state.showRightPants=false;
        // Render-state copies only: real armor and its protection remain unchanged.
        state.headEquipment=state.chestEquipment=state.legsEquipment=state.feetEquipment=ItemStack.EMPTY;
        state.headItem.clear();
    }
    public static void submit(AvatarRenderState state,PoseStack stack,SubmitNodeCollector collector) {
        if(!wears(state))return;
        boolean isSlim=state.skin.model()==PlayerModelType.SLIM;
        collector.submitModel(model(isSlim),state,stack,RenderTypes.entityCutout(isSlim?SLIM:WIDE),state.lightCoords,OverlayTexture.NO_OVERLAY,-1,null,0);
        renderedFrames++;
    }
    public static void hand(net.minecraft.client.model.geom.ModelPart source,boolean right,PoseStack stack,SubmitNodeCollector collector,int light) {
        if(!active())return;
        boolean isSlim=Minecraft.getInstance().player.getSkin().model()==PlayerModelType.SLIM;
        var model=model(isSlim);var arm=right?model.rightArm:model.leftArm;
        arm.loadPose(source.storePose());arm.visible=true;model.leftSleeve.visible=model.rightSleeve.visible=true;
        collector.submitModelPart(arm,stack,RenderTypes.entityCutout(isSlim?SLIM:WIDE),light,OverlayTexture.NO_OVERLAY,null);handFrames++;
    }
    static void draw(net.minecraft.client.gui.GuiGraphicsExtractor g) {
        if(!active())return;
        var font=Minecraft.getInstance().font;String text=timeLabel();int x=Math.max(4,g.guiWidth()-font.width(text)-8);
        int y=Math.max(24,g.guiHeight()-48);
        g.fill(x-4,y-4,g.guiWidth()-4,y+12,0xA0101820);g.text(font,text,x,y,0xFFFFD2ED);
    }
    private static PlayerModel model(boolean isSlim) {
        if(isSlim && slim!=null)return slim;if(!isSlim && wide!=null)return wide;
        var model=new OutfitModel(isSlim);
        if(isSlim)slim=model;else wide=model;return model;
    }
    private static final class OutfitModel extends PlayerModel {
        OutfitModel(boolean slim) {super(LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(.035f),slim),64,64).bakeRoot(),slim);}
        @Override public void setupAnim(AvatarRenderState state) {
            super.setupAnim(state);
            jacket.visible=leftSleeve.visible=rightSleeve.visible=leftPants.visible=rightPants.visible=hat.visible=true;
        }
    }
    static String timeLabel() {
        var c=SteveCount.settings;long seconds=(CostumeClock.remaining(c.maidUntil,System.currentTimeMillis())+999)/1000;
        return seconds==0?"No active costume":String.format(java.util.Locale.ROOT,"Maid outfit: %02d:%02d",seconds/60,seconds%60);
    }
    static void reload() {
        attempted=true;loaded=false;
        var path=Settings.configDirectory.resolve("stevecount/maid-outfit.png");
        try {
            byte[] bytes;
            if(Files.isRegularFile(path)) {
                if(Files.size(path)>128*1024)throw new IllegalArgumentException("PNG exceeds 128 KB");
                bytes=Files.readAllBytes(path);
            } else {
                try(var input=MaidCostume.class.getResourceAsStream("/assets/stevecount/textures/entity/maid.png")) {
                    if(input==null)throw new IllegalArgumentException("Bundled maid outfit missing");
                    bytes=input.readNBytes(128*1024+1);
                }
            }
            if(!validHeader(bytes))throw new IllegalArgumentException("Expected a 64 x 64 PNG");
            try(var original=NativeImage.read(bytes)) {
                if(original.getWidth()!=64 || original.getHeight()!=64)throw new IllegalArgumentException("Expected a 64 x 64 skin overlay PNG");
                var narrow=new NativeImage(64,64,true);var broad=new NativeImage(64,64,true);
                for(int y=0;y<64;y++)for(int x=0;x<64;x++) {
                    int pixel=original.getPixel(x,y);
                    if(y<16 && x<32)pixel=0; // Always preserve the real player's face/head.
                    narrow.setPixel(x,y,pixel);broad.setPixel(x,y,pixel);
                }
                for(int[] origin:new int[][]{{40,16},{40,32},{32,48},{48,48}})expandArm(narrow,broad,origin[0],origin[1]);
                var textures=Minecraft.getInstance().getTextureManager();
                textures.register(SLIM,new DynamicTexture(()->"SteveCount approved maid overlay (slim)",narrow));
                textures.register(WIDE,new DynamicTexture(()->"SteveCount approved maid overlay (wide)",broad));
            }
            loaded=true;assetStatus="Approved outfit loaded (slim + wide).";
        }catch(Exception error) {assetStatus="Costume unavailable: "+error.getClass().getSimpleName();}
    }
    static boolean validHeader(byte[] bytes) {
        if(bytes==null || bytes.length<33 || bytes.length>128*1024)return false;
        var b=java.nio.ByteBuffer.wrap(bytes);
        return b.getLong()==0x89504e470d0a1a0aL && b.getInt()==13 && b.getInt()==0x49484452 && b.getInt()==64 && b.getInt()==64;
    }
    private static void expandArm(NativeImage source,NativeImage target,int x,int y) {
        target.fillRect(x,y,16,16,0);
        // [source x,y,width,height,target x,target width], standard Java slim/wide UVs.
        for(int[] face:new int[][]{{4,0,3,4,4,4},{7,0,3,4,8,4},{0,4,4,12,0,4},{4,4,3,12,4,4},{7,4,4,12,8,4},{11,4,3,12,12,4}})
            for(int j=0;j<face[3];j++)for(int i=0;i<face[5];i++)target.setPixel(x+face[4]+i,y+face[1]+j,source.getPixel(x+face[0]+i*face[2]/face[5],y+face[1]+j));
    }
}
