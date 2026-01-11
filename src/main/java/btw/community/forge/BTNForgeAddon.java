package btw.community.forge;

import api.AddonHandler;
import api.BTWAddon;

public class BTNForgeAddon extends BTWAddon {
    public static BTNForgeAddon INSTANCE = new BTNForgeAddon();

    public BTNForgeAddon() {
        super();
    }

    @Override
    public void postSetup() {
        this.modID = "btn";
        BTWAddon.addResourcePackDomain("forge");
    }

    @Override
    public void initialize() {
        AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");
    }
}