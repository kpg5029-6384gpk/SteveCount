package dev.stevecount;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/** Explicit opt-in single-player gameplay action. Never sends attack packets to servers. */
final class GiftPunch {
    static final GiftCursor cursor=new GiftCursor();
    static final PendingPunch pending=new PendingPunch();
    static long animationStart;
    private static Object level;
    private static int readyTicks;
    static void tick() {
        var mc=Minecraft.getInstance();
        if(level!=mc.level) {level=mc.level;readyTicks=0;animationStart=0;pending.clear();}
        boolean eligible=mc.player!=null && mc.player.isAlive() && mc.getSingleplayerServer()!=null
            && SteveCount.settings.giftPunch && SteveCount.settings.streamerMode
            && SteveCount.settings.showCharacter() && !SteveCount.demo
            && SteveCount.bridge.snapshot.status().equals("Live");
        readyTicks=eligible?readyTicks+1:0;
        String previousSession=cursor.session;
        long now=System.currentTimeMillis();
        boolean hit=cursor.accept(SteveCount.bridge.gifts,now/1000d,
            readyTicks>20 && SteveCount.bridge.snapshot.status().equals("Live"));
        if(!previousSession.equals(cursor.session))pending.clear();
        if(pending.update(now,hit,eligible,mc.isPaused()))punch();
    }
    static void preview() {animationStart=System.currentTimeMillis();}
    static Vec3 strikePosition(Vec3 player,float yaw) {
        double angle=Math.toRadians(yaw);
        return player.add(Math.cos(angle)*1.1,0,Math.sin(angle)*1.1);
    }
    static void punch() {
        var mc=Minecraft.getInstance();var server=mc.getSingleplayerServer();
        if(server==null || mc.player==null || FollowerCharacter.trail.position==null)return;
        var uuid=mc.player.getUUID();var dimension=mc.level.dimension();
        Vec3 from=strikePosition(mc.player.position(),mc.player.getYRot());
        animationStart=System.currentTimeMillis();
        server.execute(()->{
            var target=server.getPlayerList().getPlayer(uuid);
            if(target==null || !target.isAlive() || target.level().dimension()!=dimension || !SteveCount.settings.giftPunch || !SteveCount.settings.streamerMode)return;
            attack(target,from);
        });
    }
    static void attack(ServerPlayer target,Vec3 from) {
        // Vanilla Player.attack handles armor, shields, resistance, i-frames, knockback,
        // creative immunity, PvP rules, exhaustion and death. No direct health writes.
        var attacker=new Fist(target.level());
        Vec3 direction=target.position().subtract(from).normalize();
        attacker.setPos(target.position().subtract(direction.scale(1.2)));
        attacker.setYRot((float)Math.toDegrees(Math.atan2(-direction.x,direction.z)));
        attacker.setOnGround(true);
        attacker.attack(target);
        // This temporary attacker is never added to the world/player list or saved.
    }
    static final class Fist extends Player {
        Fist(ServerLevel level) {super(level,new GameProfile(UUID.fromString("30fd723c-4401-4b57-a427-fb6d990ba8b7"),"SteveCountBuddy"));}
        @Override public GameType gameMode() {return GameType.SURVIVAL;}
        @Override public float getAttackStrengthScale(float partial) {return 1f;}
    }
}
