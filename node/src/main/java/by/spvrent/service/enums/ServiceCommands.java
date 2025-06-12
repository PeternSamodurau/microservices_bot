package by.spvrent.service.enums;

public enum ServiceCommands {
    HELP ("/help"),
    START("/start"),
    REGISTRATION("/registration"),
    CANCEL("/cancel");

    private final String cmd;

    ServiceCommands(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public String toString() {
        return cmd;
    }

    public boolean equals (String cmd){
        return this.toString().equals(cmd);
    }
}
