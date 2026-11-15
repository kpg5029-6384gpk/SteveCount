package dev.stevecount;

import com.google.gson.JsonObject;
import java.util.Set;

/** Baseline new sessions, consume while disabled, never replay old microphone commands. */
final class VoiceCursor {
    private String session="";
    private long sequence;
    private static final Set<String> COMMANDS=Set.of("head_on","head_off","Dance","Wave","Cheer","stop","recall");
    String accept(JsonObject feed,double now,boolean armed) {
        try {
            String id=feed.get("session").getAsString();
            long latest=feed.get("sequence").getAsLong();
            if(!session.equals(id)){session=id;sequence=latest;return "";}
            long previous=sequence;sequence=Math.max(sequence,latest);
            if(!armed || latest<=previous)return "";
            String command="";
            for(var element:feed.getAsJsonArray("events")) {
                var e=element.getAsJsonObject();long n=e.get("sequence").getAsLong();
                double age=now-e.get("created").getAsDouble();
                String action=e.get("command").getAsString();
                if(n>previous && n<=latest && Double.isFinite(age) && age>=0 && age<=4 && COMMANDS.contains(action))command=action;
            }
            return command;
        }catch(RuntimeException ignored){return "";}
    }
}
