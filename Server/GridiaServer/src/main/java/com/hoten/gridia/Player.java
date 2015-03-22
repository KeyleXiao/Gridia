package com.hoten.gridia;

import com.hoten.gridia.Container.ContainerType;
import com.hoten.gridia.content.ItemInstance;
import com.hoten.gridia.map.Coord;
import com.hoten.gridia.serializers.GridiaGson;
import com.hoten.gridia.serving.ServingGridia;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;

public class Player {

    public static class PlayerFactory {

        public static class BadLoginException extends Exception {

            public BadLoginException(String message) {
                super(message);
            }
        }

        public static class BadRegistrationException extends Exception {

            public BadRegistrationException(String message) {
                super(message);
            }
        }

        private final File dir;

        public PlayerFactory(File world) {
            dir = new File(world, "Players/");
        }

        public Player load(ServingGridia server, String username, String passwordHash) throws BadLoginException, IOException {
            File dataFile = new File(dir, username + ".json");
            if (!dataFile.exists()) {
                throw new BadLoginException("Bad user/password");
            }
            String json = FileUtils.readFileToString(dataFile);
            AccountDetails accountDetails = GridiaGson.get().fromJson(json, AccountDetails.class);

            if (!accountDetails.passwordHash.equals(passwordHash)) {
                throw new BadLoginException("Bad user/password");
            }

            com.hoten.gridia.scripting.Entity creature = server.createCreatureForPlayer(username, accountDetails.location);
            creature.setAttribute("inventory", server.containerFactory.get(accountDetails.inventoryId));
            Container equipment = server.containerFactory.get(accountDetails.equipmentId);

            return new Player(accountDetails, creature, equipment);
        }

        public Player create(ServingGridia server, String username, String passwordHash) throws BadRegistrationException, IOException {
            if (username.length() < 3) {
                throw new BadRegistrationException("Username must be at least 3 characters");
            }

            if (new File(dir, username + ".json").exists()) {
                throw new BadRegistrationException("Username already exists");
            }

            AccountDetails accountDetails = new AccountDetails();
            accountDetails.username = username;
            accountDetails.passwordHash = passwordHash;
            accountDetails.location = server.tileMap.getDefaultPlayerSpawn();

            int invSize = 40;
            if (dir.listFiles() == null) {
                accountDetails.isAdmin = true;
            }

            com.hoten.gridia.scripting.Entity creature = server.createCreatureForPlayer(username, accountDetails.location);

            // fake an inventory
            List<ItemInstance> inv = new ArrayList<>();
            inv.addAll(Arrays.asList(
                    57, 335, 277, 280, 1067, 900, 1068, 826,
                    1974, 1039, 171, 902, 901, 339, 341,
                    29, 19, 18, 12, 913, 34, 140
            ).stream()
                    .map(i -> {
                        int quantity = server.contentManager.getItem(i).stackable ? 1000 : 1;
                        return server.contentManager.createItemInstance(i, quantity);
                    })
                    .collect(Collectors.toList()));
            while (inv.size() < invSize) {
                inv.add(server.contentManager.createItemInstance(0));
            }

            Container invContainer = server.containerFactory.create(ContainerType.Inventory, inv);
            creature.setAttribute("inventory", invContainer);
            accountDetails.inventoryId = invContainer.id;

            // fake equipment
            List<ItemInstance> equipmentItems = new ArrayList();
            equipmentItems.add(server.contentManager.createItemInstance(0));
            equipmentItems.add(server.contentManager.createItemInstance(0));
            equipmentItems.add(server.contentManager.createItemInstance(0));
            equipmentItems.add(server.contentManager.createItemInstance(0));
            equipmentItems.add(server.contentManager.createItemInstance(0));

            Container equipment = server.containerFactory.create(ContainerType.Equipment, equipmentItems);
            accountDetails.equipmentId = equipment.id;
            CreatureImage image = (CreatureImage) creature.getAttribute("image");
            if (image instanceof CustomPlayerImage) {
                ((CustomPlayerImage) (image)).moldToEquipment(equipment);
            }

            Player player = new Player(accountDetails, creature, equipment);
            save(player);

            return player;
        }

        public void save(Player player) throws IOException {
            player.accountDetails.location = player.creature.location;
            String json = GridiaGson.get().toJson(player.accountDetails);
            System.out.println("json = " + json);
            FileUtils.writeStringToFile(new File(dir, player.accountDetails.username + ".json"), json);
        }
    }

    public final com.hoten.gridia.scripting.Entity creature;
    public final Container equipment;
    public final AccountDetails accountDetails;
    public final Set<Integer> openedContainers = new HashSet();
    // :(
    public int useSourceIndex, useDestIndex;
    public int useSource, useDest;

    private Player(AccountDetails accountDetails, com.hoten.gridia.scripting.Entity creature, Container equipment) {
        this.accountDetails = accountDetails;
        this.creature = creature;
        this.equipment = equipment;
    }

    public void updatePlayerImage(ServingGridia server) {
        CreatureImage image = (CreatureImage) creature.getAttribute("image");
        if (image instanceof CustomPlayerImage) {
            ((CustomPlayerImage) image).moldToEquipment(equipment);
        }
        server.updateCreatureImage(creature);
    }

    public static class AccountDetails {

        public String username, passwordHash;
        public int inventoryId, equipmentId;
        public boolean isAdmin;
        public Coord location;

        private AccountDetails() {
        }
    }
}
