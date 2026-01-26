package ru.lera.useblock;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lera.useblock.command.UseBlockCommand;
import ru.lera.useblock.interaction.BlockUseHandler;
import ru.lera.useblock.network.UseBlockPackets;
import ru.lera.useblock.client.UseBlockClientPackets;

public class UseBlockMod implements ModInitializer {

	public static final String MOD_ID = "useblock";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		UseBlockCommand.register();
		BlockUseHandler.register();
		UseBlockPackets.register();
		LOGGER.info("[UseBlock] Мод успешно загружен");
	}
}
