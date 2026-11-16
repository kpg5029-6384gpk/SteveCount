package dev.stevecount;

import com.google.gson.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.concurrent.*;

/** One background worker, bounded timeouts, immutable snapshots. No render-thread I/O. */
final class Bridge {
    record Snapshot(String status, Long viewers, long peak, double updated, boolean stale, String message, String username, String room) {
        static Snapshot empty(String state, String message) { return new Snapshot(state,null,0,0,false,message,"",""); }
    }
    volatile Snapshot snapshot = Snapshot.empty("Not Configured", "");
    volatile JsonObject gifts = new JsonObject();
    volatile JsonObject costumes = new JsonObject();
    volatile JsonObject voice = new JsonObject();
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(r -> { var t=new Thread(r,"SteveCount bridge");t.setDaemon(true);return t; });
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private volatile boolean enabled;
    private volatile boolean controlFailed;
    Bridge() { worker.scheduleWithFixedDelay(this::poll, 1, 1, TimeUnit.SECONDS); }
    private String request(String endpoint, String body) throws Exception {
        String local = System.getenv("LOCALAPPDATA");
        if (local == null) throw new IllegalStateException("Windows LOCALAPPDATA unavailable");
        var credentials = JsonParser.parseString(Files.readString(Path.of(local,"SteveCount","connection.json"))).getAsJsonObject();
        var b=HttpRequest.newBuilder(URI.create("http://127.0.0.1:18763/"+endpoint)).timeout(Duration.ofSeconds(3))
            .header("Authorization","Bearer "+credentials.get("token").getAsString());
        if(body!=null) b.header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body));
        var result=http.send(b.build(),HttpResponse.BodyHandlers.ofString());
        if(result.statusCode()!=200) {
            String detail="";
            try {
                var error=JsonParser.parseString(result.body()).getAsJsonObject().get("error");
                if(error!=null && error.isJsonPrimitive()) detail=error.getAsString();
            } catch(RuntimeException ignored) { }
            if(detail.length()>240) detail=detail.substring(0,240);
            detail=detail.replaceAll("[\\p{Cntrl}]", " ");
            throw new IllegalStateException("Companion HTTP "+result.statusCode()+(detail.isBlank()?"":": "+detail));
        }
        return result.body();
    }
    private void poll() {
        if (!enabled && !SteveCount.settings.voiceControl) return;
        try {
            var j=JsonParser.parseString(request("status",null)).getAsJsonObject();
            Long count=j.get("viewers").isJsonNull()?null:j.get("viewers").getAsLong();
            if(count!=null && count<0) throw new IllegalArgumentException("Invalid count");
            if(!controlFailed) snapshot=new Snapshot(j.get("status").getAsString(),count,j.get("peak").getAsLong(),j.get("updated").getAsDouble(),j.get("stale").getAsBoolean(),j.get("message").getAsString(),j.get("username").getAsString(),j.get("room").getAsString());
            voice=j.has("voice")?j.getAsJsonObject("voice"):new JsonObject();
            costumes=j.has("costumes")?j.getAsJsonObject("costumes"):new JsonObject();
            gifts=j.has("gifts")?j.getAsJsonObject("gifts"):new JsonObject();
        } catch(Exception ex) {
            voice=new JsonObject();
            if(!enabled)return;
            var old=snapshot;
            snapshot=new Snapshot("Reconnecting",old.viewers,old.peak,old.updated,old.viewers!=null,"Launch SteveCount Companion; "+ex.getClass().getSimpleName(),old.username,old.room);
        }
    }
    void control(String action, String username) {
        enabled=false;
        controlFailed=false;
        gifts=new JsonObject();costumes=new JsonObject();
        snapshot=Snapshot.empty(action.equals("connect")?"Connecting":"Disconnected","");
        worker.execute(() -> {
            try { var j=new JsonObject();j.addProperty("action",action);j.addProperty("username",username); request("control",j.toString()); enabled=action.equals("connect"); }
            catch(Exception ex) { controlFailed=true; snapshot=Snapshot.empty("Error",ex.getMessage()); }
        });
    }
    void pause() { enabled=false; }
}

