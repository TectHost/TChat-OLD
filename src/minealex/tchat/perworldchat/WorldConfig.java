package minealex.tchat.perworldchat;

public class WorldConfig {
    private String worldName;
    private boolean chatEnabled;
    private boolean perWorldChat;
    private boolean radiusChatEnabled;
    private int radiusChat;

    public WorldConfig(String worldName, boolean chatEnabled, boolean perWorldChat, boolean radiusChatEnabled, int radiusChat) {
        this.worldName = worldName;
        this.chatEnabled = chatEnabled;
        this.perWorldChat = perWorldChat;
        this.radiusChatEnabled = radiusChatEnabled;
        this.radiusChat = radiusChat;
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
    
    public boolean isRadiusChatEnabled() {
        return radiusChatEnabled;
    }

    public void setRadiusChatEnabled(boolean radiusChatEnabled) {
        this.radiusChatEnabled = radiusChatEnabled;
    }

    public int getRadiusChat() {
        return radiusChat;
    }

    public void setRadiusChat(int radiusChat) {
        this.radiusChat = radiusChat;
    }
}
