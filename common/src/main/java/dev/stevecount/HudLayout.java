package dev.stevecount;

/** Relative anchors plus GUI-pixel offsets, clamped after GUI scaling. */
final class HudLayout {
    static int coordinate(float anchor, int offset, int screen, int extent) {
        int available=Math.max(0,screen-extent);
        return Math.clamp(Math.round(anchor*available)+offset,0,available);
    }
}
