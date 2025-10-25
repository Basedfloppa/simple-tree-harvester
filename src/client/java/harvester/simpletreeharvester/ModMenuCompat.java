package harvester.simpletreeharvester;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> HarvesterConfigFactory.create(parent, SimpleTreeHarvester.CONFIG_MGR);
    }
}
