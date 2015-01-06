package hoten.gridia.serving.protocols;

import com.google.gson.JsonObject;
import hoten.gridia.Player;
import hoten.gridia.content.ItemInstance;
import hoten.gridia.serving.ConnectionToGridiaClientHandler;
import hoten.gridia.serving.ServingGridia;
import hoten.serving.message.JsonMessageHandler;
import java.io.IOException;

public class UnequipItem extends JsonMessageHandler<ConnectionToGridiaClientHandler> {

    @Override
    protected void handle(ConnectionToGridiaClientHandler connection, JsonObject data) throws IOException {
        ServingGridia server = connection.getServer();
        Player player = connection.getPlayer();
        int slotIndex = data.get("slotIndex").getAsInt();
        
        ItemInstance itemToUnequip = player.equipment.get(slotIndex);
        if (player.creature.inventory.add(itemToUnequip)) {
            player.equipment.deleteSlot(slotIndex);
            player.updatePlayerImage(server);
        } else {
            connection.send(server.messageBuilder.chat("Your inventory is full.", player.creature.location));
        }
    }
}