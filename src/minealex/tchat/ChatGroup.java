package minealex.tchat;

public class ChatGroup {
    private String prefix;
    private String suffix;

    public ChatGroup(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }
}