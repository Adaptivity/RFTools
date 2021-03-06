package mcjty.rftools.dimension.world.terrain;

import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.dimension.world.GenericChunkProvider;
import mcjty.rftools.dimension.world.types.TerrainType;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.NoiseGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class CavernTerrainGenerator implements BaseTerrainGenerator {
    private World world;
    private GenericChunkProvider provider;

    public enum CavernHeight {
        HEIGHT_64,
        HEIGHT_128,
        HEIGHT_196,
        HEIGHT_256
    }
    private CavernHeight heightsetting;
    private static int cavernheight[] = new int[] {8, 16, 24, 32};

    /** A NoiseGeneratorOctaves used in generating nether terrain */
    private NoiseGeneratorOctaves netherNoiseGen1;
    private NoiseGeneratorOctaves netherNoiseGen2;
    private NoiseGeneratorOctaves netherNoiseGen3;
    /** Determines whether something other than nettherack can be generated at a location */
    private NoiseGeneratorOctaves netherrackExculsivityNoiseGen;
    private NoiseGeneratorOctaves netherNoiseGen6;
    private NoiseGeneratorOctaves netherNoiseGen7;

    private double[] noiseField;
    /** Holds the noise used to determine whether something other than the baseblock can be generated at a location */
    private double[] baseBlockExclusivityNoise = new double[256];
    private double[] noiseData1;
    private double[] noiseData2;
    private double[] noiseData3;
    private double[] noiseData4;
    private double[] noiseData5;

    public CavernTerrainGenerator(CavernHeight heightsetting) {
        if (heightsetting == null) {
            int hs = DimletConfiguration.cavernHeightLimit;
            if (hs < 0) {
                hs = 0;
            } else if (hs > 3) {
                hs = 3;
            }
            this.heightsetting = CavernHeight.values()[hs];
        } else {
            this.heightsetting = heightsetting;
        }
    }

    @Override
    public void setup(World world, GenericChunkProvider provider) {
        this.world = world;
        this.provider = provider;

        this.netherNoiseGen1 = new NoiseGeneratorOctaves(provider.rand, 16);
        this.netherNoiseGen2 = new NoiseGeneratorOctaves(provider.rand, 16);
        this.netherNoiseGen3 = new NoiseGeneratorOctaves(provider.rand, 8);
        /* Determines whether slowsand or gravel can be generated at a location */
        NoiseGeneratorOctaves slowsandGravelNoiseGen = new NoiseGeneratorOctaves(provider.rand, 4);
        this.netherrackExculsivityNoiseGen = new NoiseGeneratorOctaves(provider.rand, 4);
        this.netherNoiseGen6 = new NoiseGeneratorOctaves(provider.rand, 10);
        this.netherNoiseGen7 = new NoiseGeneratorOctaves(provider.rand, 16);

        NoiseGenerator[] noiseGens = {netherNoiseGen1, netherNoiseGen2, netherNoiseGen3, slowsandGravelNoiseGen, netherrackExculsivityNoiseGen, netherNoiseGen6, netherNoiseGen7};
        noiseGens = TerrainGen.getModdedNoiseGenerators(world, provider.rand, noiseGens);
        this.netherNoiseGen1 = (NoiseGeneratorOctaves)noiseGens[0];
        this.netherNoiseGen2 = (NoiseGeneratorOctaves)noiseGens[1];
        this.netherNoiseGen3 = (NoiseGeneratorOctaves)noiseGens[2];
        this.netherrackExculsivityNoiseGen = (NoiseGeneratorOctaves)noiseGens[4];
        this.netherNoiseGen6 = (NoiseGeneratorOctaves)noiseGens[5];
        this.netherNoiseGen7 = (NoiseGeneratorOctaves)noiseGens[6];
    }

    /**
     * generates a subset of the level's terrain data. Takes 7 arguments: the [empty] noise array, the position, and the
     * size.
     */
    private double[] initializeNoiseField(double[] noiseField, int x, int y, int z, int sx, int sy, int sz) {
        ChunkProviderEvent.InitNoiseField event = new ChunkProviderEvent.InitNoiseField(provider, noiseField, x, y, z, sx, sy, sz);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DENY) {
            return event.noisefield;
        }

        int syr = cavernheight[heightsetting.ordinal()];

        if (noiseField == null) {
            noiseField = new double[sx * sy * sz];
        }

        double d0 = 684.412D;
        double d1 = 2053.236D;
        this.noiseData4 = this.netherNoiseGen6.generateNoiseOctaves(this.noiseData4, x, y, z, sx, 1, sz, 1.0D, 0.0D, 1.0D);
        this.noiseData5 = this.netherNoiseGen7.generateNoiseOctaves(this.noiseData5, x, y, z, sx, 1, sz, 100.0D, 0.0D, 100.0D);
        this.noiseData1 = this.netherNoiseGen3.generateNoiseOctaves(this.noiseData1, x, y, z, sx, sy, sz, d0 / 80.0D, d1 / 60.0D, d0 / 80.0D);
        this.noiseData2 = this.netherNoiseGen1.generateNoiseOctaves(this.noiseData2, x, y, z, sx, sy, sz, d0, d1, d0);
        this.noiseData3 = this.netherNoiseGen2.generateNoiseOctaves(this.noiseData3, x, y, z, sx, sy, sz, d0, d1, d0);
        int k1 = 0;
        double[] adouble1 = new double[sy];
        int i2;

        for (i2 = 0; i2 < sy; ++i2) {
            adouble1[i2] = Math.cos(i2 * Math.PI * 6.0D / syr) * 2.0D;
            double d2 = i2;

            if (i2 > syr) {
                d2 = 0;
            } else if (i2 > syr / 2) {
                d2 = (syr - 1 - i2);
            }

            if (d2 < 4.0D) {
                d2 = 4.0D - d2;
                adouble1[i2] -= d2 * d2 * d2 * 10.0D;
            }
        }

        for (i2 = 0; i2 < sx; ++i2) {
            for (int k2 = 0; k2 < sz; ++k2) {
                double d4 = 0.0D;

                for (int j2 = 0; j2 < sy; ++j2) {
                    double d6;
                    double d7 = adouble1[j2];
                    double d8 = this.noiseData2[k1] / 512.0D;
                    double d9 = this.noiseData3[k1] / 512.0D;
                    double d10 = (this.noiseData1[k1] / 10.0D + 1.0D) / 2.0D;

                    if (d10 < 0.0D) {
                        d6 = d8;
                    } else if (d10 > 1.0D) {
                        d6 = d9;
                    } else {
                        d6 = d8 + (d9 - d8) * d10;
                    }

                    d6 -= d7;
                    double d11;

                    if (j2 > sy - 4) {
                        d11 = ((j2 - (sy - 4)) / 3.0F);
                        d6 = d6 * (1.0D - d11) + -10.0D * d11;
                    }

                    if (j2 < d4) {
                        d11 = (d4 - j2) / 4.0D;

                        if (d11 < 0.0D) {
                            d11 = 0.0D;
                        }

                        if (d11 > 1.0D) {
                            d11 = 1.0D;
                        }

                        d6 = d6 * (1.0D - d11) + -10.0D * d11;
                    }

                    noiseField[k1] = d6;
                    ++k1;
                }
            }
        }

        return noiseField;
    }

    @Override
    public void generate(int chunkX, int chunkZ, Block[] aBlock, byte[] meta) {
        Block baseBlock = provider.dimensionInformation.getBaseBlockForTerrain().getBlock();
        byte baseMeta = provider.dimensionInformation.getBaseBlockForTerrain().getMeta();
        Block baseLiquid = provider.dimensionInformation.getFluidForTerrain();

        byte b0 = 4;
        int liquidlevel = 32;
        if (provider.dimensionInformation.getTerrainType() == TerrainType.TERRAIN_FLOODED_CAVERN) {
            liquidlevel = 127;
        }
        int k = b0 + 1;
        byte b2 = 33;
        int l = b0 + 1;
        this.noiseField = this.initializeNoiseField(this.noiseField, chunkX * b0, 0, chunkZ * b0, k, b2, l);

        for (int x4 = 0; x4 < b0; ++x4) {
            for (int z4 = 0; z4 < b0; ++z4) {
                for (int height32 = 0; height32 < cavernheight[heightsetting.ordinal()]; ++height32) {
                    double d0 = 0.125D;
                    double d1 = this.noiseField[((x4 + 0) * l + z4 + 0) * b2 + height32 + 0];
                    double d2 = this.noiseField[((x4 + 0) * l + z4 + 1) * b2 + height32 + 0];
                    double d3 = this.noiseField[((x4 + 1) * l + z4 + 0) * b2 + height32 + 0];
                    double d4 = this.noiseField[((x4 + 1) * l + z4 + 1) * b2 + height32 + 0];
                    double d5 = (this.noiseField[((x4 + 0) * l + z4 + 0) * b2 + height32 + 1] - d1) * d0;
                    double d6 = (this.noiseField[((x4 + 0) * l + z4 + 1) * b2 + height32 + 1] - d2) * d0;
                    double d7 = (this.noiseField[((x4 + 1) * l + z4 + 0) * b2 + height32 + 1] - d3) * d0;
                    double d8 = (this.noiseField[((x4 + 1) * l + z4 + 1) * b2 + height32 + 1] - d4) * d0;

                    for (int h = 0; h < 8; ++h) {
                        double d9 = 0.25D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        for (int x = 0; x < 4; ++x) {
                            int height = (height32 * 8) + h;
                            int index = ((x + (x4 * 4)) << 12) | ((0 + (z4 * 4)) << 8) | height;
                            short maxheight = 256;
                            double d14 = 0.25D;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;

                            for (int k2 = 0; k2 < 4; ++k2) {
                                if (d15 > 0.0D) {
                                    aBlock[index] = baseBlock;
                                    meta[index] = baseMeta;
                                } else if (height < liquidlevel) {
                                    aBlock[index] = baseLiquid;
                                } else {
                                    aBlock[index] = null;
                                }

                                index += maxheight;
                                d15 += d16;
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }



    @Override
    public void replaceBlocksForBiome(int chunkX, int chunkZ, Block[] aBlock, byte[] abyte, BiomeGenBase[] biomeGenBases) {
        ChunkProviderEvent.ReplaceBiomeBlocks event = new ChunkProviderEvent.ReplaceBiomeBlocks(provider, chunkX, chunkZ, aBlock, abyte, biomeGenBases, world);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DENY) {
            return;
        }

        Block baseBlock = provider.dimensionInformation.getBaseBlockForTerrain().getBlock();
        byte baseMeta = provider.dimensionInformation.getBaseBlockForTerrain().getMeta();
        Block baseLiquid = provider.dimensionInformation.getFluidForTerrain();

        byte b0 = 64;
        double d0 = 0.03125D;
        this.baseBlockExclusivityNoise = this.netherrackExculsivityNoiseGen.generateNoiseOctaves(this.baseBlockExclusivityNoise, chunkX * 16, chunkZ * 16, 0, 16, 16, 1, d0 * 2.0D, d0 * 2.0D, d0 * 2.0D);

        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                int i1 = (int)(this.baseBlockExclusivityNoise[k + l * 16] / 3.0D + 3.0D + provider.rand.nextDouble() * 0.25D);
                int j1 = -1;
                Block block = baseBlock;
                byte meta = baseMeta;

                for (int k1 = 255; k1 >= 0; --k1) {
                    int l1 = (l * 16 + k) * 256 + k1;

                    if (k1 < DimletConfiguration.bedrockLayer) {
                        aBlock[l1] = Blocks.bedrock;
                    } else if (k1 < 255 - provider.rand.nextInt(5) && k1 > provider.rand.nextInt(5)) {
                        Block block2 = aBlock[l1];

                        if (block2 != null && block2.getMaterial() != Material.air) {
                            if (block2 == baseBlock) {
                                if (j1 == -1) {
                                    if (i1 <= 0) {
                                        block = null;
                                        meta = 0;
                                    } else if (k1 >= b0 - 4 && k1 <= b0 + 1) {
                                        block = baseBlock;
                                        meta = baseMeta;
                                    }

                                    if (k1 < b0 && (block == null || block.getMaterial() == Material.air)) {
                                        block = baseLiquid;
                                        meta = 0;
                                    }

                                    j1 = i1;

                                    if (k1 >= b0 - 1) {
                                        aBlock[l1] = block;
                                        abyte[l1] = meta;
                                    } else {
                                        aBlock[l1] = baseBlock;
                                        abyte[l1] = baseMeta;
                                    }
                                }
                                else if (j1 > 0) {
                                    --j1;
                                    aBlock[l1] = baseBlock;
                                    abyte[l1] = baseMeta;
                                }
                            }
                        }
                        else {
                            j1 = -1;
                        }
                    } else if (heightsetting == CavernHeight.HEIGHT_256) {
                        // Only use a bedrock ceiling if the height is 256.
                        aBlock[l1] = Blocks.bedrock;
                    }
                }
            }
        }
    }

}
