package dev.quantumfusion.dashloader.util;

import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.state.StateManager;

public class BooleanSelector implements MultipartModelSelector {

	public final boolean selector;

	public BooleanSelector(MultipartModelSelector selector) {
		this.selector = selector == MultipartModelSelector.TRUE;
	}

	@Override
	public Predicate<BlockState> getPredicate(StateManager<Block, BlockState> stateFactory) {
		return null;
	}
}
