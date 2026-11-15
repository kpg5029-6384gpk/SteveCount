package dev.stevecount;

/** One expiring punch while truly paused; never a burst or a persistent statistic. */
final class PendingPunch {
    private long expires, lastStrike=-1;
    void clear() {expires=0;}
    boolean waiting() {return expires!=0;}
    boolean update(long now,boolean incoming,boolean eligible,boolean paused) {
        if(!eligible) {clear();return false;}
        if(expires!=0 && now>=expires)clear();
        if(incoming && (lastStrike<0 || now-lastStrike>=5000) && expires==0)expires=now+60000;
        if(expires==0 || paused)return false;
        clear();
        if(lastStrike>=0 && now-lastStrike<5000)return false;
        lastStrike=now;return true;
    }
}
