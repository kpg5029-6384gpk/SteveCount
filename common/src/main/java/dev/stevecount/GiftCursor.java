package dev.stevecount;

import com.google.gson.JsonObject;

/** Consume every feed even while disabled, so old gifts never arm later. */
final class GiftCursor {
    String session="";
    long sequence;
    double last=Double.NEGATIVE_INFINITY;
    boolean accept(JsonObject feed,double now,boolean armed) {
        try {
            String id=feed.get("session").getAsString();
            long latest=feed.get("sequence").getAsLong();
            if(!session.equals(id)) {session=id;sequence=latest;return false;}
            long previous=sequence;sequence=Math.max(sequence,latest);
            if(!armed || latest<=previous || now-last<5)return false;
            for(var element:feed.getAsJsonArray("events")) {
                var e=element.getAsJsonObject();long n=e.get("sequence").getAsLong();
                double age=now-e.get("created").getAsDouble();
                if(n>previous && n<=latest && age>=0 && age<=5) {last=now;return true;}
            }
        } catch(RuntimeException ignored) { }
        return false;
    }
}
