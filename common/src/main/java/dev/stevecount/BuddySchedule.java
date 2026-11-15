package dev.stevecount;

import java.util.random.RandomGenerator;

/** No catch-up bursts after pauses, world changes, muting or disabled options. */
final class BuddySchedule {
    private final RandomGenerator random;
    long nextEmote,nextNoise;
    private boolean playful;
    BuddySchedule(RandomGenerator random) {this.random=random;}
    void reset() {nextEmote=nextNoise=0;}
    int tick(long now,boolean eligible,boolean emotes,boolean noises,boolean animations,boolean busy,boolean playful) {
        if(!eligible) {reset();return 0;}
        if(this.playful!=playful) {this.playful=playful;reset();}
        int result=0;
        if(!emotes || !animations)nextEmote=0;
        else if(nextEmote==0)nextEmote=now+delay(true);
        else if(now>=nextEmote) {nextEmote=now+delay(true);if(!busy)result|=1;}
        if(!noises)nextNoise=0;
        else if(nextNoise==0)nextNoise=now+delay(false);
        else if(now>=nextNoise) {nextNoise=now+delay(false);if(!busy)result|=2;}
        return result;
    }
    private long delay(boolean emote) {
        long min=emote?(playful?20000:35000):(playful?25000:50000);
        long max=emote?(playful?45000:75000):(playful?60000:100000);
        return random.nextLong(min,max+1);
    }
}
