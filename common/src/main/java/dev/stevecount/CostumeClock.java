package dev.stevecount;

/** Real elapsed time, persisted as a deadline. Each gift restarts a full thirty minutes. */
final class CostumeClock {
    static final long DURATION=30*60*1000L;
    static long start(long now) {return Math.addExact(now,DURATION);}
    static long remaining(long deadline,long now) {return Math.clamp(deadline-now,0,DURATION);}
    static boolean wears(long deadline,String wearer,String player,long now) {return wearer!=null && wearer.equals(player) && remaining(deadline,now)>0;}
}
