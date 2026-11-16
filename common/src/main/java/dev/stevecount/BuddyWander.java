package dev.stevecount;

import java.util.random.RandomGenerator;
import net.minecraft.world.phys.Vec3;

/** Tick-driven, bounded destination planning. No world access, entity AI or render work. */
final class BuddyWander {
    private final RandomGenerator random;
    private Vec3 lastPlayer,goal;
    private double heading,angle,distance,lastRadius;
    private int quietTicks,remaining,dwell=-1;
    boolean moving;
    int destinations;
    BuddyWander(RandomGenerator random) {this.random=random;}
    void reset() {lastPlayer=goal=null;quietTicks=remaining=0;dwell=-1;moving=false;lastRadius=0;}
    void obstructed() {remaining=0;goal=null;dwell=-1;}
    Vec3 target(Vec3 player,float yaw,double radius,Vec3 buddy,Vec3 route) {
        if(lastPlayer!=null && lastPlayer.distanceToSqr(player)>256)reset();
        if(lastPlayer==null)heading=Math.toRadians(yaw);
        Vec3 velocity=lastPlayer==null?Vec3.ZERO:player.subtract(lastPlayer);
        lastPlayer=player;
        if(velocity.horizontalDistanceSqr()>.0004) {
            double desired=Math.atan2(-velocity.x,velocity.z);
            heading+=Math.atan2(Math.sin(desired-heading),Math.cos(desired-heading))*.25;
            quietTicks=0;
        } else quietTicks++;
        boolean walking=quietTicks<10 && velocity.horizontalDistanceSqr()>.00001 || moving && quietTicks<10;
        boolean changed=walking!=moving || Math.abs(lastRadius-radius)>.001;
        moving=walking;lastRadius=radius;
        if(remaining>0)remaining--;
        if(changed || goal==null || moving && remaining==0 || !moving && dwell==0)choose(player,radius);
        if(moving) {
            double a=heading+angle;
            // The rear choice uses recent footsteps to help turn around corners.
            goal=Math.abs(angle-Math.PI)<.01?bound(player,route,radius):player.add(-Math.sin(a)*distance,0,Math.cos(a)*distance);
        } else if(buddy!=null && buddy.subtract(goal).horizontalDistanceSqr()<.04) {
            if(dwell<0)dwell=30+random.nextInt(61);
            else dwell--;
        }
        if(!moving && remaining==0 && dwell<0)choose(player,radius); // Unreachable destination: try another.
        return bound(player,new Vec3(goal.x,player.y,goal.z),radius);
    }
    private void choose(Vec3 player,double radius) {
        distance=radius*(.3+random.nextDouble()*.65);
        if(moving) {
            angle=switch(random.nextInt(4)) {
                case 0 -> (random.nextDouble()-.5)*.6; // Ahead.
                case 1 -> Math.PI/2+(random.nextDouble()-.5)*.5;
                case 2 -> -Math.PI/2+(random.nextDouble()-.5)*.5;
                default -> Math.PI;
            };
            remaining=80+random.nextInt(81);
        } else {
            angle=random.nextDouble()*Math.PI*2;
            remaining=180; // Includes travel time; rest starts only on arrival.
        }
        double a=moving?heading+angle:angle;
        goal=player.add(-Math.sin(a)*distance,0,Math.cos(a)*distance);
        dwell=-1;destinations++;
    }
    static Vec3 bound(Vec3 player,Vec3 position,double radius) {
        double dx=position.x-player.x,dz=position.z-player.z,length=Math.hypot(dx,dz);
        if(length<=radius)return position;
        return new Vec3(player.x+dx*radius/length,position.y,player.z+dz*radius/length);
    }
}
