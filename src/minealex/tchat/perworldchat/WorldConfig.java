package minealex.tchat.perworldchat;

public class WorldConfig {
    private String worldName;
    private boolean chatEnabled;

    public WorldConfig(String worldName, boolean chatEnabled) {
        this.worldName = worldName;
        this.chatEnabled = chatEnabled;
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
}
