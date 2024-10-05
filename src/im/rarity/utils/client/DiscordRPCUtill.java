package im.rarity.utils.client;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;

import java.time.OffsetDateTime;


public class DiscordRPCUtill {

    static IPCClient client = new IPCClient(1288388786228822056L);
    public static void startRPC() {

        client.setListener(new IPCListener(){
            @Override
            public void onPacketReceived(IPCClient client, Packet packet) {
                IPCListener.super.onPacketReceived(client, packet);
            }

            @Override
            public void onReady(IPCClient client)
            {
                RichPresence.Builder builder = new RichPresence.Builder();
                builder.setDetails("Beta Test")
                        .setStartTimestamp(OffsetDateTime.now())
                        .setLargeImage("", "");
                client.sendRichPresence(builder.build());
            }
        });
        try {
            client.connect();
        } catch (NoDiscordClientException e) {
            System.out.println("DiscordRPC: " + e.getMessage());
        }
    }
}