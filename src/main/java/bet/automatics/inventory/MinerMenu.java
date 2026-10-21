package bet.automatics.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class MinerMenu extends AbstractContainerMenu {
    private final Container miner;

    public MinerMenu(int containerId, Inventory inventory, Container miner) {
        super(MenuType.GENERIC_3x3, containerId);
        checkContainerSize(miner, 9);
        this.miner = miner;
        miner.startOpen(inventory.player);
        this.add3x3GridSlots(miner, 62, 17);
        this.addStandardInventorySlots(inventory, 8, 84);
    }

    protected void add3x3GridSlots(Container container, int left, int top) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int slot = x + y * 3;
                this.addSlot(new Slot(container, slot, left + x * 18, top + y * 18));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack clicked = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            clicked = stack.copy();
            if (slotIndex < 9) {
                if (!this.moveItemStackTo(stack, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == clicked.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return clicked;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.miner.stillValid(player);
    }

}
