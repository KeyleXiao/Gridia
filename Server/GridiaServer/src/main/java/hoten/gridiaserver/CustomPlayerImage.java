package hoten.gridiaserver;

import hoten.gridiaserver.content.Item;
import hoten.gridiaserver.content.ItemInstance;

public class CustomPlayerImage implements CreatureImage {

    public int bareHead, bareArms, bareChest, bareLegs;

    public int head, arms, chest, legs, shield, weapon;

    public void moldToEquipment(Container equipment) {
        ItemInstance headEquipment = equipment.get(Item.ArmorSpot.Head.ordinal());
        ItemInstance chestEquipment = equipment.get(Item.ArmorSpot.Chest.ordinal());
        ItemInstance legsEquipment = equipment.get(Item.ArmorSpot.Legs.ordinal());
        if (headEquipment != ItemInstance.NONE) {
            head = headEquipment.data.wearImage;
        } else {
            head = bareHead;
        }
        if (chestEquipment != ItemInstance.NONE) {
            chest = chestEquipment.data.wearImage;
        } else {
            chest = bareChest;
        }
        if (legsEquipment != ItemInstance.NONE) {
            legs = legsEquipment.data.wearImage;
        } else {
            legs = bareLegs;
        }
    }
}