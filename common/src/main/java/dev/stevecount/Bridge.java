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
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(r -> { var t=new Thread(r,"SteveCount bridge");t.setDaemon(true);return t; });
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private volatile boolean enabled;
    private long generation;
    Bridge() { worker.scheduleWithFixedDelay(this::poll, 1, 1, TimeUnit.SECONDS); }
    private String request(String endpoint, String body) throws Exception {
        String local = System.getenv("LOCALAPPDATA");
        if (local == null) throw new IllegalStateException("Windows LOCALAPPDATA unavailable");
        var credentials = JsonParser.parseString(Files.readString(Path.of(local,"SteveCount","connection.json"))).getAsJsonObject();
        var b=HttpRequest.newBuilder(URI.create("http://127.0.0.1:18763/"+endpoint)).timeout(Duration.ofSeconds(3))
            .header("Authorization","Bearer "+credentials.get("token").getAsString());
        if(body!=null) b.header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body));
        var result=http.send(b.build(),HttpResponse.BodyHandlers.ofString());
        if(result.statusCode()!=200) throw new IllegalStateException("Companion HTTP "+result.statusCode());
        return result.body();
    }
    private void poll() {
        if (!enabled) return;
        try {
            var j=JsonParser.parseString(request("status",null)).getAsJsonObject();
            Long count=j.get("viewers").isJsonNull()?null:j.get("viewers").getAsLong();
            if(count!=null && count<0) throw new IllegalArgumentException("Invalid count");
            snapshot=new Snapshot(j.get("status").getAsString(),count,j.get("peak").getAsLong(),j.get("updated").getAsDouble(),j.get("stale").getAsBoolean(),j.get("message").getAsString(),j.get("username").getAsString(),j.get("room").getAsString());
            gifts=j.has("gifts")?j.getAsJsonObject("gifts"):new JsonObject();
        } catch(Exception ex) {
            var old=snapshot;
            snapshot=new Snapshot("Reconnecting",old.viewers,old.peak,old.updated,old.viewers!=null,"Launch SteveCount Companion; "+ex.getClass().getSimpleName(),old.username,old.room);
        }
    }
    void control(String action, String username) {
        enabled=false;
        gifts=new JsonObject();
        snapshot=Snapshot.empty(action.equals("connect")?"Connecting":"Disconnected","");
        worker.execute(() -> {
            try { var j=new JsonObject();j.addProperty("action",action);j.addProperty("username",username); request("control",j.toString()); }
            catch(Exception ex) { snapshot=Snapshot.empty("Error","Launch companion or check username: "+ex.getMessage()); }
            enabled=action.equals("connect");
        });
    }
    void pause() { enabled=false; }
}

