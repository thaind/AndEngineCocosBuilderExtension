package org.andengine.extension.cocosbuilder.loader;

import java.io.IOException;

import org.andengine.entity.IEntity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.cocosbuilder.CCBEntityLoaderData;
import org.andengine.extension.cocosbuilder.entity.CCSprite;
import org.andengine.extension.cocosbuilder.exception.CCBLevelLoaderException;
import org.andengine.extension.texturepacker.opengl.texture.util.texturepacker.TexturePack;
import org.andengine.extension.texturepacker.opengl.texture.util.texturepacker.TexturePackLoader;
import org.andengine.extension.texturepacker.opengl.texture.util.texturepacker.exception.TexturePackParseException;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.util.SAXUtils;
import org.xml.sax.Attributes;

import android.content.res.AssetManager;

/**
 * (c) Zynga 2012
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 18:25:58 - 18.04.2012
 */
public class CCSpriteEntityLoader extends CCNodeEntityLoader {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final String ENTITY_NAMES = "CCSprite";

	private static final String TAG_CCSPRITE_ATTRIBUTE_TEXTUREPACK = "texturePack";
	private static final String TAG_CCSPRITE_ATTRIBUTE_TEXTUREREGION = "textureRegion";

	private static final String TAG_CCSPRITE_ATTRIBUTE_FLIPPED_HORIZONTAL = "flipX";
	private static final boolean TAG_CCSPRITE_ATTRIBUTE_FLIPPED_HORIZONTAL_VALUE_DEFAULT = false;
	private static final String TAG_CCSPRITE_ATTRIBUTE_FLIPPED_VERTICAL = "flipY";
	private static final boolean TAG_CCSPRITE_ATTRIBUTE_FLIPPED_VERTICAL_VALUE_DEFAULT = false;

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	public CCSpriteEntityLoader() {
		super(CCSpriteEntityLoader.ENTITY_NAMES);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected IEntity createEntity(final String pEntityName, final float pX, final float pY, final float pWidth, final float pHeight, final Attributes pAttributes, final CCBEntityLoaderData pCCBEntityLoaderData) throws IOException {
		final ITextureRegion textureRegion = this.getTextureRegion(pAttributes, pCCBEntityLoaderData);

		return new CCSprite(pX, pY, pWidth, pHeight, textureRegion, pCCBEntityLoaderData.getVertexBufferObjectManager());
	}

	@Override
	protected void setAttributes(final IEntity pEntity, final Attributes pAttributes) {
		super.setAttributes(pEntity, pAttributes);

		this.setCCSpriteAttributes((Sprite)pEntity, pAttributes);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	protected void setCCSpriteAttributes(final Sprite pSprite, final Attributes pAttributes) {
		this.setCCSpriteFlipped(pSprite, pAttributes);
		this.setCCSpriteBlendFunction(pSprite, pAttributes);
	}


	protected void setCCSpriteBlendFunction(final Sprite pSprite, final Attributes pAttributes) {
		pSprite.setBlendFunction(this.getBlendFunctionSource(pAttributes), this.getBlendFunctionDestination(pAttributes));
	}


	protected void setCCSpriteFlipped(final Sprite pSprite, final Attributes pAttributes) {
		pSprite.setFlipped(this.isFlippedHorizontal(pAttributes), this.isFlippedVertical(pAttributes));
	}

	protected boolean isFlippedHorizontal(final Attributes pAttributes) {
		return SAXUtils.getBooleanAttribute(pAttributes, CCSpriteEntityLoader.TAG_CCSPRITE_ATTRIBUTE_FLIPPED_HORIZONTAL, CCSpriteEntityLoader.TAG_CCSPRITE_ATTRIBUTE_FLIPPED_HORIZONTAL_VALUE_DEFAULT);
	}

	protected boolean isFlippedVertical(final Attributes pAttributes) {
		return SAXUtils.getBooleanAttribute(pAttributes, CCSpriteEntityLoader.TAG_CCSPRITE_ATTRIBUTE_FLIPPED_VERTICAL, CCSpriteEntityLoader.TAG_CCSPRITE_ATTRIBUTE_FLIPPED_VERTICAL_VALUE_DEFAULT);
	}

	protected ITextureRegion getTextureRegion(final Attributes pAttributes, final CCBEntityLoaderData pCCBEntityLoaderData) throws IOException, CCBLevelLoaderException {
		return CCSpriteEntityLoader.getTextureRegion(pAttributes, CCSpriteEntityLoader.TAG_CCSPRITE_ATTRIBUTE_TEXTUREPACK, CCSpriteEntityLoader.TAG_CCSPRITE_ATTRIBUTE_TEXTUREREGION, pCCBEntityLoaderData);
	}

	public static ITextureRegion getTextureRegion(final Attributes pAttributes, final String pTexturePackAttributeName, final String pTextureRegionAttributeName, final CCBEntityLoaderData pCCBEntityLoaderData) throws IOException, CCBLevelLoaderException {
		final boolean isOnTexturePack = SAXUtils.hasAttribute(pAttributes, pTexturePackAttributeName);

		final TextureManager textureManager = pCCBEntityLoaderData.getTextureManager();
		final AssetManager assetManager = pCCBEntityLoaderData.getAssetManager();
		final String assetBasePath = pCCBEntityLoaderData.getAssetBasePath();
		if(isOnTexturePack) {
			final String texturePackName = SAXUtils.getAttributeOrThrow(pAttributes, pTexturePackAttributeName);
			final String textureRegionName = SAXUtils.getAttributeOrThrow(pAttributes, pTextureRegionAttributeName);

			if(!pCCBEntityLoaderData.hasTexturePack(texturePackName)) {
				final TexturePack texturePack;
				try {
					final String texturePackPath = assetBasePath + texturePackName;
					texturePack = new TexturePackLoader(textureManager).loadFromAsset(assetManager, texturePackPath, assetBasePath);
				} catch (final TexturePackParseException e) {
					throw new CCBLevelLoaderException("Error loading TexturePack: '" + texturePackName + "'.", e);
				}
				texturePack.loadTexture();
				pCCBEntityLoaderData.putTexturePack(texturePackName, texturePack);
			}
			final TexturePack texturePack = pCCBEntityLoaderData.getTexturePack(texturePackName);

			return texturePack.getTexturePackTextureRegionLibrary().get(textureRegionName);
		} else {
			final String textureName = SAXUtils.getAttributeOrThrow(pAttributes, pTextureRegionAttributeName);
			final String texturePath = assetBasePath + textureName;

			final ITexture texture;
			if(textureManager.hasMappedTexture(textureName)) {
				texture = textureManager.getMappedTexture(textureName);
			} else {
				texture = textureManager.getTexture(textureName, assetManager, texturePath);
				texture.load();
			}

			return TextureRegionFactory.extractFromTexture(texture);
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
