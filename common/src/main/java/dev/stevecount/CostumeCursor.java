package dev.stevecount;

import com.google.gson.JsonObject;

/** Independent cosmetic feed: no route to combat and no accumulated cheap-gift totals. */
final class CostumeCursor {
    private String session="";
    private long sequence;
    boolean accept(JsonObject feed,double now,boolean armed) {
        try {
            String id=feed.get("session").getAsString();long latest=feed.get("sequence").getAsLong();
            if(!id.equals(session)){session=id;sequence=latest;return false;}
            long previous=sequence;sequence=Math.max(sequence,latest);
            if(!armed || latest<=previous)return false;
            for(var entry:feed.getAsJsonArray("events")) {
                var event=entry.getAsJsonObject();long n=event.get("sequence").getAsLong();
                if(!event.get("unitCoins").getAsJsonPrimitive().isNumber())continue;
                double age=now-event.get("created").getAsDouble();double coins=event.get("unitCoins").getAsDouble();
                if(n>previous && n<=latest && Double.isFinite(age) && age>=0 && age<=5
                    && Double.isFinite(coins) && coins>=1000 && coins<=Integer.MAX_VALUE && coins==Math.rint(coins))return true;
            }
        }catch(RuntimeException ignored){}
        return false;
    }
}
