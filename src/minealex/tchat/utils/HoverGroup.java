package minealex.tchat.utils;

import java.util.List;

public class HoverGroup {
    private boolean enabled;
    private List<String> hoverText;
    private String suggestCommand;

    public HoverGroup(boolean enabled, List<String> hoverText, String suggestCommand) {
        this.enabled = enabled;
        this.hoverText = hoverText;
        this.suggestCommand = suggestCommand;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<String> getHoverText() {
        return hoverText;
    }

    public String getSuggestCommand() {
        return suggestCommand;
    }
}