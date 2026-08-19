package com.stayplugged.dailyca.bank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.stayplugged.dailyca.model.GearProfile;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

public class BankGearClassifierTest
{
	@Test
	public void classifiesWhipAsMidTierMelee()
	{
		GearProfile profile = new BankGearClassifier().classify(
			Collections.singleton(ItemID.ABYSSAL_WHIP));

		assertEquals(2, profile.getMeleeTier());
		assertTrue(profile.isBankScanned());
	}

	@Test
	public void usesBestMeleeWeaponInBank()
	{
		GearProfile profile = new BankGearClassifier().classify(new HashSet<>(Arrays.asList(
			ItemID.ABYSSAL_WHIP,
			ItemID.SOULREAPER)));

		assertEquals(4, profile.getMeleeTier());
	}

	@Test
	public void recognizesBowfaWithCrystalArmourAsHighEndRanged()
	{
		GearProfile profile = new BankGearClassifier().classify(new HashSet<>(Arrays.asList(
			ItemID.BOW_OF_FAERDHINEN,
			ItemID.CRYSTAL_CHESTPLATE,
			ItemID.CRYSTAL_PLATELEGS)));

		assertEquals(4, profile.getRangedTier());
	}

	@Test
	public void classifiesRepresentativeEndgameWeaponsAcrossStyles()
	{
		assertEquals(4, new BankGearClassifier().classify(
			Collections.singleton(ItemID.SCYTHE_OF_VITUR)).getMeleeTier());
		assertEquals(3, new BankGearClassifier().classify(
			Collections.singleton(ItemID.OSMUMTENS_FANG)).getMeleeTier());
		assertEquals(4, new BankGearClassifier().classify(
			Collections.singleton(ItemID.TWISTED_BOW)).getRangedTier());
		assertEquals(3, new BankGearClassifier().classify(
			Collections.singleton(ItemID.TOXIC_BLOWPIPE_LOADED)).getRangedTier());
		assertEquals(4, new BankGearClassifier().classify(
			Collections.singleton(ItemID.TUMEKENS_SHADOW)).getMagicTier());
		assertEquals(2, new BankGearClassifier().classify(
			Collections.singleton(ItemID.TOXIC_TOTS_CHARGED)).getMagicTier());
	}

	@Test
	public void recognizesEquivalentOrnamentAndCrystalColourVariants()
	{
		assertEquals(3, new BankGearClassifier().classify(
			Collections.singleton(ItemID.OSMUMTENS_FANG_ORNAMENT)).getMeleeTier());
		assertEquals(3, new BankGearClassifier().classify(
			Collections.singleton(ItemID.BLADE_OF_SAELDOR_INFINITE_ITHELL)).getMeleeTier());
		GearProfile recolouredBowfa = new BankGearClassifier().classify(new HashSet<>(Arrays.asList(
			ItemID.BOW_OF_FAERDHINEN_INFINITE_ITHELL,
			ItemID.CRYSTAL_CHESTPLATE_ITHELL,
			ItemID.CRYSTAL_PLATELEGS_ITHELL)));
		assertEquals(4, recolouredBowfa.getRangedTier());
	}

	@Test
	public void recognizesCurrentEquivalentWeaponVariants()
	{
		BankGearClassifier classifier = new BankGearClassifier();
		for (int whip : new int[]{ItemID.ABYSSAL_WHIP_LAVA, ItemID.ABYSSAL_WHIP_ICE})
		{
			assertEquals(2, classifier.classify(Collections.singleton(whip)).getMeleeTier());
		}
		for (int scythe : new int[]{ItemID.SCYTHE_OF_VITUR_BL, ItemID.SCYTHE_OF_VITUR_UNCHARGED_BL})
		{
			assertEquals(4, classifier.classify(Collections.singleton(scythe)).getMeleeTier());
		}
		assertEquals(3, classifier.classify(
			Collections.singleton(ItemID.BLADE_OF_SAELDOR_INACTIVE)).getMeleeTier());
		assertEquals(3, classifier.classify(
			Collections.singleton(ItemID.BOW_OF_FAERDHINEN_INACTIVE)).getRangedTier());

		for (int staff : new int[]{
			ItemID.TOTS_CHARGED, ItemID.TOTS_UNCHARGED, ItemID.TOTS_I_CHARGED, ItemID.TOTS_I_UNCHARGED,
			ItemID.TOXIC_TOTS_CHARGED_ORN, ItemID.TOXIC_TOTS_UNCHARGED_ORN,
			ItemID.TOXIC_TOTS_I_CHARGED_ORN, ItemID.TOXIC_TOTS_I_UNCHARGED_ORN,
			ItemID.TOTS_CHARGED_ORN, ItemID.TOTS_UNCHARGED_ORN,
			ItemID.TOTS_I_CHARGED_ORN, ItemID.TOTS_I_UNCHARGED_ORN})
		{
			assertTrue(classifier.classify(Collections.singleton(staff)).getMagicTier() >= 1);
		}
	}
}
