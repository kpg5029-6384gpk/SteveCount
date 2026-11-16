package dev.stevecount;

import java.util.ArrayDeque;
import net.minecraft.world.phys.Vec3;

/** Bounded recent route, independent of world/server entities. */
final class FollowTrail {
    final ArrayDeque<Vec3> points=new ArrayDeque<>();
    Vec3 previous, position;
    float yaw, previousYaw, walk, speed;
    int blocked;
    float fallbackYaw;

    void reset() { points.clear();previous=position=null;blocked=0;walk=speed=0; }
    Vec3 target(Vec3 player,float playerYaw,double distance) {
        Vec3 newest=points.peekFirst();
        if(newest!=null && newest.distanceToSqr(player)>256) reset();
        if(points.isEmpty())fallbackYaw=playerYaw;
        if(points.isEmpty() || points.peekFirst().distanceToSqr(player)>.0225) points.addFirst(player);
        while(points.size()>256) points.removeLast();
        double remaining=distance;
        Vec3 from=player;
        for(Vec3 to:points) {
            double length=from.distanceTo(to);
            if(length>=remaining && length>0) return from.lerp(to,remaining/length);
            remaining-=length;from=to;
        }
        double radians=Math.toRadians(fallbackYaw);
        return from.add(Math.sin(radians)*remaining,0,-Math.cos(radians)*remaining);
    }
    Vec3 propose(Vec3 player,Vec3 target) {
        return propose(player,target,.24);
    }
    Vec3 propose(Vec3 player,Vec3 target,double pace) {
        previous=position;
        previousYaw=yaw;
        if(position==null || position.distanceToSqr(player)>256 || Math.abs(position.y-player.y)>8 || blocked>30) {
            position=previous=target;blocked=0;speed=0;
            return target;
        }
        Vec3 delta=target.subtract(position);
        double distance=delta.length();
        if(distance<.12) {speed*=.6f;return position;}
        double step=Math.min(distance, distance>5?.42:pace);
        return position.add(delta.scale(step/distance));
    }
    void accept(Vec3 next) {
        Vec3 start=position;
        if(start==null)start=next;
        double dx=next.x-start.x,dz=next.z-start.z;
        double moved=Math.hypot(dx,dz);
        if(moved>.005) {
            float desired=(float)Math.toDegrees(Math.atan2(-dx,dz));
            yaw+=wrap(desired-yaw)*.35f;
            walk+=(float)moved*4;
        }
        speed+=(Math.min(1,(float)moved*5)-speed)*.4f;
        position=next;
    }
    static float wrap(float degrees) {return (degrees%360+540)%360-180;}
    Vec3 interpolated(float partial) { return previous==null?position:previous.lerp(position,partial); }
}
