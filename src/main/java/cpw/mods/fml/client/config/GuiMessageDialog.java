package cpw.mods.fml.client.config;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiDisconnected;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.I18n;
import net.minecraft.src.IChatComponent;

public class GuiMessageDialog extends GuiDisconnected
{
    private String buttonText;

    public GuiMessageDialog(GuiScreen nextScreen, String title, IChatComponent message, String buttonText)
    {
        super(nextScreen, title, message);
        this.buttonText = buttonText;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        super.initGui();
        ((GuiButton) buttonList.get(0)).displayString = I18n.format(buttonText);
    }
}