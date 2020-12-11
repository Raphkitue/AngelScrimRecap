package model;

public enum CommandsRegistry

{
    RECAP_START("Angel, recap start", "Starts a recap round for a scrim"),
    HELP("Angel, help", "Prints this help"),
    SETUP("Angel, setup", "Initial bot setup with creation of a channel");

    private final String command;
    private final String description;

    CommandsRegistry(String command, String description)
    {
        this.command = command;
        this.description = description;
    }

    public String getCommand()
    {
        return command;
    }

    public String getDescription()
    {
        return description;
    }
}
