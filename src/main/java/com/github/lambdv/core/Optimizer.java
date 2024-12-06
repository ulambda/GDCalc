package com.github.lambdv.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;

/**
 * Utility class that provides optimization algorithms for a given character and rotation.
 */
public class Optimizer {
    public static ArtifactBuilder optimialArtifactSubStatDistrbution(final Character c, final Rotation r, double energyRechargeRequirements) throws IllegalArgumentException{
        var bob = ArtifactBuilder.KQMC(c.flower(),c.feather(),c.sands(),c.goblet(),c.circlet());
        var target = new BuffedStatTable(c.build(), ()->bob.substats()); 
        try{
            while (target.get(Stat.EnergyRecharge) < energyRechargeRequirements)
                bob.roll(Stat.EnergyRecharge, Artifacts.RollQuality.AVG);
        } catch (AssertionError e){ throw new IllegalArgumentException("Energy Recharge requirements cannot be met with substats alone");}

        Set<Stat> possibleSubsToRoll = ArtifactBuilder.possibleSubStats().collect(Collectors.toSet());

        while (bob.numRollsLeft() > 0 && !possibleSubsToRoll.isEmpty()){
            var bestSub = possibleSubsToRoll.stream()
                .filter(s->bob.numRollsLeft(s) > 0)
                .map(s -> {
                    try{ 
                        bob.roll(s, Artifacts.RollQuality.AVG);
                        var dpr = r.compute(target);
                        return Map.entry(s, dpr); 
                    }
                    finally{ bob.unRoll(s); }
                })
                .map(e-> {
                    if(e.getValue() == 0) possibleSubsToRoll.remove(e.getKey());
                    return e;
                })
                .reduce((x,y) -> x.getValue() >= y.getValue() ? x : y)
                .get().getKey();
            //System.out.println(bestSub);
            bob.roll(bestSub, Artifacts.RollQuality.AVG);
        }
        return bob;
    }

    public static ArtifactBuilder greedyOptimialArtifactSubStatDistrbution(final Character c, final Rotation r, double energyRechargeRequirements) throws IllegalArgumentException{
        var bob = ArtifactBuilder.KQMC(c.flower(),c.feather(),c.sands(),c.goblet(),c.circlet());
        var target = new BuffedStatTable(c.build(), ()->bob.substats()); 
        try{
            while (target.get(Stat.EnergyRecharge) < energyRechargeRequirements && bob.numRollsLeft(Stat.EnergyRecharge) > 0)
                bob.roll(Stat.EnergyRecharge, Artifacts.RollQuality.AVG);
        } catch (AssertionError e){ throw new IllegalArgumentException("Energy Recharge requirements cannot be met with substats alone");}

        assert target.get(Stat.EnergyRecharge) >= energyRechargeRequirements : "Energy Recharge requirements cannot be met with substats alone";

        Set<Stat> possibleSubsToRoll = ArtifactBuilder.possibleSubStats().collect(Collectors.toSet());

        while (bob.numRollsLeft() > 0 && !possibleSubsToRoll.isEmpty()){
            var bestSub = possibleSubsToRoll.stream()
                .filter(s->bob.numRollsLeft(s) > 0)
                .map(s -> {
                    try{ 
                        bob.roll(s, Artifacts.RollQuality.AVG);
                        return Map.entry(s, r.compute(target)); 
                    }
                    finally{ bob.unRoll(s); }
                })
                .map(e-> {
                    if(e.getValue() == 0) possibleSubsToRoll.remove(e.getKey());
                    return e;
                })
                .reduce((x,y) -> x.getValue() >= y.getValue() ? x : y)
                .get().getKey();
            var rollsToFill = Math.min(bob.numRollsLeft(), bob.numRollsLeft(bestSub));
            //System.out.println(bestSub);
            bob.roll(bestSub, Artifacts.RollQuality.AVG, rollsToFill);
        }
        return bob;
    }
    /**
     * This method is deprecated 
     * Use {@link #KQMSArtifactOptimizer.visitCharacter(Character)} instead.
     * @description Find the optimal 5 star artifact main stats for a given character and rotation and return the artifact builder used
     * @note character is not modified/mutated
     * @param character
     * @param rotation
     * @param energyRechargeRequirements
     * @return ArtifactBuilder
     */
    @Deprecated
    public static ArtifactBuilder optimal5StarArtifactMainStats(final Character c, final Rotation r, double energyRechargeRequirements){
        boolean needERSands = c.get(Stat.EnergyRecharge) < energyRechargeRequirements;
        boolean ERSandsIsntEnough = c.get(Stat.EnergyRecharge) + Artifacts.getMainStatValue(5, 20, Stat.EnergyRecharge) < energyRechargeRequirements;
        if(needERSands && ERSandsIsntEnough)
            throw new IllegalArgumentException("Energy Recharge requirements cannot be met with mainstats alone");
        
        Character copy = c.clone();
        copy.unequipAllArtifacts();
        //c.substats = new HashMap<>();
        c.clearSubstats();
        Flower bestFlower = new Flower(5, 20);
        Feather bestFeather = new Feather(5, 20);
        Sands bestSands = new Sands(1, 0, Stat.ATKPercent);
        Goblet bestGoblet = new Goblet(1, 0, Stat.ATKPercent);
        Circlet bestCirclet = new Circlet(1, 0, Stat.ATKPercent);

        copy.equip(bestFlower);
        copy.equip(bestFeather);

        double bestComboDPR = 0;
        
        for(Stat sandsMainStat : needERSands ? List.of(Stat.EnergyRecharge) : Sands.allowlist()){
            for(Stat gobletMainStat : Goblet.allowlist()){
                for(Stat circletMainStat : Circlet.allowlist()){
                    copy.equip(new Sands(5, 20, sandsMainStat));
                    copy.equip(new Goblet(5, 20, gobletMainStat));
                    copy.equip(new Circlet(5, 20, circletMainStat));
                   
                    double thisComboDPR = r.compute(copy);
                    if(thisComboDPR > bestComboDPR){
                        bestComboDPR = thisComboDPR;
                        bestSands = copy.sands().get();
                        bestGoblet = copy.goblet().get();
                        bestCirclet = copy.circlet().get();
                    }
                }
            }
        }
        copy.equip(bestSands);
        copy.equip(bestGoblet);
        copy.equip(bestCirclet);
        return new ArtifactBuilder(copy.flower().get(), copy.feather().get(), copy.sands().get(), copy.goblet().get(), copy.circlet().get());
    }
}   


record KQMSArtifactOptimizer(Rotation r, double energyRechargeRequirements) implements StatTableVisitor<ArtifactBuilder> {

    public ArtifactBuilder visitCharacter(Character c){
        if(c.get(Stat.EnergyRecharge) + Artifacts.getMainStatValue(5, 20, Stat.EnergyRecharge) + 
        (Artifacts.getSubStatValue(5, Stat.EnergyRecharge) * Artifacts.RollQuality.AVG.multiplier * 8) < energyRechargeRequirements) 
            throw new IllegalArgumentException("Energy Recharge requirements cannot be met");

        Optional<ArtifactBuilder> bob = Optional.empty();
        c.unequipAllArtifacts();
        c.clearSubstats();
        c.equip(new Flower(5, 20));
        c.equip(new Feather(5, 20));
        double bestComboDPR = 0;


        var base = r.compute(c);

        var sandsList = new ArrayList<Stat>(Sands.allowlist().stream().filter(s -> r.compute(c, StatTable.of(s, 1)) > base).toList());
        sandsList.add(Stat.EnergyRecharge);
        var gobletList = Goblet.allowlist().stream().filter(s -> r.compute(c, StatTable.of(s, 1)) > base).toList();
        var circletList = Circlet.allowlist().stream().filter(s -> r.compute(c, StatTable.of(s, 1)) > base).toList();

        for (Stat sandsMainStat : sandsList) {
            for (Stat gobletMainStat : gobletList) {
                for (Stat circletMainStat : circletList) {
                    Optional<ArtifactBuilder> bob2 = Optional.empty();
                    try {
                        c.equip(new Sands(5, 20, sandsMainStat));
                        c.equip(new Goblet(5, 20, gobletMainStat));
                        c.equip(new Circlet(5, 20, circletMainStat));
                        c.clearSubstats();
                        bob2 = Optional.of(Optimizer.greedyOptimialArtifactSubStatDistrbution(c, r, energyRechargeRequirements));
                        c.setSubstats(bob2.get().substats());
                        if (c.get(Stat.EnergyRecharge) < energyRechargeRequirements) continue;
                        double thisComboDPR = r.compute(c);
                        if (thisComboDPR > bestComboDPR) {
                            bestComboDPR = thisComboDPR;
                            bob = bob2;
                        }
                    } catch (IllegalArgumentException e) {continue;}
                }
            }
        }
        c.equip(bob.get().sands().get());
        c.equip(bob.get().goblet().get());
        c.equip(bob.get().circlet().get());
        c.setSubstats(bob.get().substats());
        return bob.get();
    }

    /**
     * 
    public ArtifactBuilder visitCharacterAux(Character c){
        if(c.get(Stat.EnergyRecharge) + Artifacts.getMainStatValue(5, 20, Stat.EnergyRecharge) + 
        (Artifacts.getSubStatValue(5, Stat.EnergyRecharge) * Artifacts.RollQuality.AVG.multiplier * 8) < energyRechargeRequirements) 
            throw new IllegalArgumentException("Energy Recharge requirements cannot be met");
        
        Character copy = c.clone();
        copy.clearSubstats();
        copy.unequipAllArtifacts();
        copy.equip(new Flower(5, 20));
        copy.equip(new Feather(5, 20));
        Optional<Sands> bestSands = Optional.empty();
        Optional<Goblet> bestGoblet = Optional.empty();
        Optional<Circlet> bestCirclet = Optional.empty();
        ArtifactBuilder bestSubs = new ArtifactBuilder();

        double base = r.compute(copy);
        List<Stat> useableStats = Stream.of(Sands.allowlist(), Goblet.allowlist(), Circlet.allowlist())
            .flatMap(List::stream)
            .distinct()
            .filter(s -> r.compute(copy, StatTable.of(s, 1)) > base)
            .toList();
        //var sandsList = needERSands ? List.of(Stat.EnergyRecharge) : Sands.allowlist().stream().filter(useableStats::contains).toList();
        var sandsList = new ArrayList<Stat>(Sands.allowlist().stream().filter(useableStats::contains).toList());
        var gobletList = Goblet.allowlist().stream().filter(useableStats::contains).toList();
        var circletList = Circlet.allowlist().stream().filter(useableStats::contains).toList();
        sandsList.add(Stat.EnergyRecharge);

        double bestComboDPR = 0;
        for(Stat sandsMainStat : sandsList){
            for(Stat gobletMainStat : gobletList){
                for(Stat circletMainStat : circletList){
                    Optional<ArtifactBuilder> bob2 = Optional.empty();
                    try{
                        copy.equip(new Sands(5, 20, sandsMainStat));
                        copy.equip(new Goblet(5, 20, gobletMainStat));
                        copy.equip(new Circlet(5, 20, circletMainStat));
                        bob2 = Optional.of(Optimizer.greedyOptimialArtifactSubStatDistrbution(copy, r, energyRechargeRequirements));
                        copy.setSubstats(bob2.get().stats());
                        assert  copy.get(Stat.EnergyRecharge) >= energyRechargeRequirements : "Energy Recharge requirements cannot be met";
                    } catch(IllegalArgumentException e){ continue; }
                    
                    double thisComboDPR = r.compute(copy);
                    if(thisComboDPR > bestComboDPR && copy.get(Stat.EnergyRecharge) >= energyRechargeRequirements){
                        bestComboDPR = thisComboDPR;
                        bestSands = copy.sands();
                        bestGoblet = copy.goblet();
                        bestCirclet = copy.circlet();
                        bestSubs = bob2.get();
                    }
                }
            }
        }
        c.equip(new Flower(5, 20));
        c.equip(new Feather(5, 20));
        c.equip(bestSands.get());
        c.equip(bestGoblet.get());
        c.equip(bestCirclet.get());
        c.setSubstats(bestSubs.stats());
        return bestSubs;
    }
     */


    // public ArtifactBuilder visitCharacter(Character c){
    //     c.clearSubstats();
        
    //     ArtifactBuilder mains = Optimizer.optimal5StarArtifactMainStats(c, r, 0);
    //     /**
    //      * issue with this code is that its finding the best mainstats without considering substats
    //      * this may favor a mainstat that is not actually the optimal one overall but instead the optimal one with no subs
    //      * eg: favouring atk% sands over cirt circlet due to low cirt without subs
    //      */
        
    //     c.equip(mains.flower().get());
    //     c.equip(mains.feather().get());
    //     c.equip(mains.sands().get());
    //     c.equip(mains.goblet().get());
    //     c.equip(mains.circlet().get());

    //     if(c.get(Stat.EnergyRecharge) >= energyRechargeRequirements){
    //         ArtifactBuilder subs = Optimizer.optimialArtifactSubStatDistrbution(c, r, energyRechargeRequirements);
    //         c.setSubstats(subs.stats());
    //         return subs;
    //     }

    //     ArtifactBuilder subs = new ArtifactBuilder();
    //     Optional<ArtifactBuilder> erSands = Optional.empty();
    //     Optional<ArtifactBuilder> anySands = Optional.empty();
    //     double DPRwithERSands = 0;
    //     double DPRwithOutERSands = 0;

    //     try{
    //         erSands = Optional.of(Optimizer.greedyOptimialArtifactSubStatDistrbution(c.equip(new Sands(5, 20, Stat.EnergyRecharge)), r, energyRechargeRequirements));
    //         final var ferSands = erSands.get().substats();
    //         DPRwithERSands = r.compute(c, ()->ferSands);
    //     }
    //     catch(Throwable e){ throw new IllegalArgumentException("Energy Recharge Requirements cannot be met with both er sands artifact substats"); } //if er sands and subs are not enough then just throw the exception

    //     try{
    //         anySands = Optional.of(Optimizer.greedyOptimialArtifactSubStatDistrbution(c.equip(mains.sands().get()), r, energyRechargeRequirements));
    //         final var fanySands = anySands.get().substats();
    //         DPRwithOutERSands = r.compute(c, ()->fanySands);
    //     }
    //     catch(IllegalArgumentException e){
    //        // System.out.println("Energy Recharge requirements cannot be met with substats alone");
    //     }//this means an er sands is needed, ignore the anySands case (DPRwithOutERSands will be 0)

    //     System.out.println(DPRwithERSands);
    //     System.out.println(DPRwithOutERSands);

    //     if(DPRwithERSands > DPRwithOutERSands){
    //         subs = erSands.get();
    //         c.equip(new Sands(5, 20, Stat.EnergyRecharge)); 
    //     } 
    //     else {
    //         subs = anySands.get();
    //         c.equip(mains.sands().get());
    //     }
    //     c.setSubstats(subs.stats());
    //     return subs;
    // }

    public ArtifactBuilder visitWeapon(Weapon w){ throw new UnsupportedOperationException("Not yet implemented");}
    public ArtifactBuilder visitArtifact(Artifact a){ throw new UnsupportedOperationException("Not yet implemented"); }
    public ArtifactBuilder visitStatTable(StatTable s){ throw new UnsupportedOperationException("Not yet implemented"); }
}