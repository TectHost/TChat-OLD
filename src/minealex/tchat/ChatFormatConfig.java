package minealex.tchat;

public class ChatFormatConfig {
    private String prefix;
    private String suffix;
    private String format;

    public ChatFormatConfig(String prefix, String suffix, String format) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.format = format;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getFormat() {
        return format;
    }
}