package minealex.tchat.perworldchat;

public class WorldConfig {
    private String worldName;
    private boolean chatEnabled;
    private boolean perWorldChat;

    public WorldConfig(String worldName, boolean chatEnabled, boolean perWorldChat) {
        this.worldName = worldName;
        this.chatEnabled = chatEnabled;
        this.perWorldChat = perWorldChat;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public boolean isChatEnabled() {
        return chatEnabled;
    }

    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }

    public boolean isPerWorldChat() {
        return perWorldChat;
    }

    public void setPerWorldChat(boolean perWorldChat) {
        this.perWorldChat = perWorldChat;
    }
}
