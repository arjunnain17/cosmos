package cope.cosmos.client.features.modules.misc;

import cope.cosmos.asm.mixins.accessor.ICPacketChatMessage;
import cope.cosmos.asm.mixins.accessor.ITextComponentString;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ChatUtil;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
@SuppressWarnings("unused")
public class ChatModifications extends Module {
    public static ChatModifications INSTANCE;

    public ChatModifications() {
        super("ChatModifications", Category.MISC, "Allows you to modify the in-game chat window");
        INSTANCE = this;
    }

    public static Setting<Time> time = new Setting<>("Time", Time.NA).setDescription("Time format");
    public static Setting<Boolean> prefix = new Setting<>("Prefix", false).setDescription("Add a cosmos prefix before chat messages");
    public static Setting<Boolean> suffix = new Setting<>("Suffix", true).setDescription("Add a cosmos suffix after chat messages");
    public static Setting<Boolean> colored = new Setting<>("Colored", true).setDescription("Add a > before public messages");

    /*
    public static Setting<Boolean> highlight = new Setting<>("Highlight", true);
    public static Setting<TextFormatting> self = new Setting<>("Self", TextFormatting.DARK_PURPLE).setParent(highlight);
    public static Setting<TextFormatting> friends = new Setting<>("Friends", TextFormatting.AQUA).setParent(highlight);
     */

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        // packet for chat messages
        if (event.getPacket() instanceof CPacketChatMessage) {
            // make sure the message is not a command
            if (((CPacketChatMessage) event.getPacket()).getMessage().startsWith("/") || ((CPacketChatMessage) event.getPacket()).getMessage().startsWith("!") || ((CPacketChatMessage) event.getPacket()).getMessage().startsWith("$") || ((CPacketChatMessage) event.getPacket()).getMessage().startsWith("?") || ((CPacketChatMessage) event.getPacket()).getMessage().startsWith(".") || ((CPacketChatMessage) event.getPacket()).getMessage().startsWith(",")) {
                return;
            }

            // reformat message
            String formattedMessage = (colored.getValue() ? "> " : "") + ((CPacketChatMessage) event.getPacket()).getMessage() + (suffix.getValue() ? " \u23d0 " + ChatUtil.toUnicode(Cosmos.NAME) : "");

            // update the message
            ((ICPacketChatMessage) event.getPacket()).setMessage(formattedMessage);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        // packet for server chat messages
        if (event.getPacket() instanceof SPacketChat) {

            // get the text
            if (((SPacketChat) event.getPacket()).getChatComponent() instanceof TextComponentString && !((SPacketChat) event.getPacket()).getType().equals(ChatType.GAME_INFO)) {
                // the chat message
                TextComponentString component = (TextComponentString) ((SPacketChat) event.getPacket()).getChatComponent();

                // timestamp
                String formattedTime = "";
                switch (time.getValue()) {
                    case NA:
                        formattedTime = new SimpleDateFormat("h:mm a").format(new Date());
                        break;
                    case EU:
                        formattedTime = new SimpleDateFormat("k:mm").format(new Date());
                        break;
                }

                if (component.getText() != null) {
                    // timestamp formatted
                    String formattedText = (!time.getValue().equals(Time.NONE) ? TextFormatting.GRAY + "[" + formattedTime + "] " + TextFormatting.RESET : "") + (prefix.getValue() ? ChatUtil.getPrefix() : "") + component.getText();

                    /*
                    if (highlight.getValue()) {
                        formattedText = formattedText.replaceAll("(?i)" + mc.player.getName(), self.getValue() + mc.player.getName() + ChatFormatting.RESET);

                        for (EntityPlayer friend : mc.world.playerEntities.stream().filter(entityPlayer -> Cosmos.INSTANCE.getSocialManager().getSocial(entityPlayer.getName()).equals(Relationship.FRIEND)).collect(Collectors.toList())) {
                            formattedText = formattedText.replaceAll("(?i)" + friend.getName(), friends.getValue() + friend.getName() + ChatFormatting.RESET);
                        }
                    }
                     */

                    // replace the chat message
                    ((ITextComponentString) component).setText(formattedText);
                }
            }
        }
    }

    public enum Time {

        /**
         * Display NA time
         */
        NA,

        /**
         * Display EU time
         */
        EU,

        /**
         * No timestamps
         */
        NONE
    }
}
