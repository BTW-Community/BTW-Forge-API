package btw.community.forge;

import api.AddonHandler;
import api.BTWAddon;

public class ForgeAPIAddon extends BTWAddon {
    private static ForgeAPIAddon instance;

    public ForgeAPIAddon() {
        super();
    }

    @Override
    public void initialize() {
        AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");
    }
}