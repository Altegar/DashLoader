package dev.quantumfusion.dashloader.data.model;

import dev.quantumfusion.dashloader.api.DashDependencies;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.data.common.ObjectObjectList;
import dev.quantumfusion.dashloader.data.image.DashSprite;
import dev.quantumfusion.dashloader.data.model.components.DashBakedQuad;
import dev.quantumfusion.dashloader.data.model.components.DashModelOverrideList;
import dev.quantumfusion.dashloader.data.model.components.DashModelTransformation;
import dev.quantumfusion.dashloader.mixin.accessor.BasicBakedModelAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import java.util.Random;

@DashObject(BasicBakedModel.class)
@DashDependencies({DashSprite.class, DashBakedQuad.class})
public final class DashBasicBakedModel implements DashModel {
	public final List<Integer> quads;
	public final ObjectObjectList<Direction, List<Integer>> faceQuads;
	public final boolean usesAo;
	public final boolean hasDepth;
	public final boolean isSideLit;
	@DataNullable
	public final DashModelTransformation transformation;
	public final DashModelOverrideList itemPropertyOverrides;
	public final int spritePointer;

	public DashBasicBakedModel(List<Integer> quads,
							   ObjectObjectList<Direction, List<Integer>> faceQuads,
							   boolean usesAo, boolean hasDepth, boolean isSideLit,
							   DashModelTransformation transformation,
							   DashModelOverrideList itemPropertyOverrides,
							   int spritePointer) {
		this.quads = quads;
		this.faceQuads = faceQuads;
		this.usesAo = usesAo;
		this.hasDepth = hasDepth;
		this.isSideLit = isSideLit;
		this.transformation = transformation;
		this.itemPropertyOverrides = itemPropertyOverrides;
		this.spritePointer = spritePointer;
	}


	public DashBasicBakedModel(BasicBakedModel basicBakedModel, RegistryWriter writer) {
		BasicBakedModelAccessor access = ((BasicBakedModelAccessor) basicBakedModel);
		this.quads = new ArrayList<>();
		basicBakedModel.getQuads(null, null, new Random());
		for (var quad : access.getQuads()) {
			this.quads.add(writer.add(quad));
		}

		this.faceQuads = new ObjectObjectList<>();
		access.getFaceQuads().forEach((direction, bakedQuads) -> {
			var bakedQuadsOut = new ArrayList<Integer>();
			for (var bakedQuad : bakedQuads) {
				bakedQuadsOut.add(writer.add(bakedQuad));
			}
			this.faceQuads.put(direction, bakedQuadsOut);
		});

		this.itemPropertyOverrides = new DashModelOverrideList(access.getItemPropertyOverrides(), writer);
		this.usesAo = access.getUsesAo();
		this.hasDepth = access.getHasDepth();
		this.isSideLit = access.getIsSideLit();
		this.transformation = DashModelTransformation.createDashOrReturnNullIfDefault(access.getTransformation());
		this.spritePointer = writer.add(access.getSprite());
	}


	@Override
	public BasicBakedModel export(final RegistryReader reader) {
		final Sprite sprite = reader.get(this.spritePointer);

		var quadsOut = new ArrayList<BakedQuad>();
		for (var quad : this.quads) {
			quadsOut.add(reader.get(quad));
		}

		var faceQuadsOut = new HashMap<Direction, List<BakedQuad>>();
		for (var entry : this.faceQuads.list()) {
			var out = new ArrayList<BakedQuad>();
			for (Integer integer : entry.value()) {
				out.add(reader.get(integer));
			}
			faceQuadsOut.put(entry.key(), out);
		}

		return new BasicBakedModel(quadsOut, faceQuadsOut, this.usesAo, this.isSideLit, this.hasDepth, sprite, DashModelTransformation.exportOrDefault(this.transformation), this.itemPropertyOverrides.export(reader));
	}

	@Override
	public void postExport(RegistryReader reader) {
		this.itemPropertyOverrides.applyOverrides(reader);
	}
}
