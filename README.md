# Parametric Transformer
> computation engine for genshin impact damage/stat calculation and optimization
 PT provides abstraction to model stats tables, algorithms for stat/gearing optimization and a framework for character damage-per-rotation calculation.

## Installation
 1. **Clone The Repository**
   ```bash
   git clone https://github.com/ulambda/ParametricTransformer.git 
   ```
 2. **Navagate into the directory**
  ```bash
  cd ParametricTransformer
  ```
 3. **Install Dependencies (Maven required: https://maven.apache.org)**
```
mvn install
```
## Usage: Core
The core package is a framework as a library for Genshin Impact stat modeling, calculations and optimizations. 
Core can be used standalone for damage calculation programmatically in Java or to make your own applications/
```java
  import com.github.lambdv.core.*;
  var ayaka = Characters.of("ayaka")
    .equip(Weapons.of("mistsplitter"))
    .add(StatTable.of(
        Stat.ATKPercent, 0.20 + 0.20 + 0.48,
        Stat.CritRate, 0.4 + 0.15,
        Stat.ElementalDMGBonus, 0.15 + 0.12 + 0.28 + 0.18 + (0.0004*800),
        Stat.NormalATKDMGBonus, 0.3,
        Stat.ChargeATKDMGBonus, 0.3,
        Stat.CryoResistanceReduction, 0.4));
  var ayakaRotation = new Rotation()
      .add("n1", DamageFormulas.defaultCryoNormalATK(3.0, 0.84))
      .add("n2", DamageFormulas.defaultCryoNormalATK(2.0, 0.894))
      .add("ca", DamageFormulas.defaultCryoChargedATK(2.0, 3.039))
      .add("skill", DamageFormulas.defaultCryoSkillATK(2.0, 4.07))
      .add("burstcutts", DamageFormulas.defaultCryoBurstATK(19.0, 1.91))
      .add("burstexplosion", DamageFormulas.defaultCryoBurstATK(1.0, 2.86));
  ayaka.optimize(Optimizers.KQMSArtifactOptimizer(ayakaRotation, 1.30));
  var dps = ayakaRotation.compute(ayaka)/21;
```
## Usage: GUI (WIP)
PT comes with a launcher for a suite of programs made with the core library for users to interface with using a GUI.

## Usage: API/HTTPServer (WIP)
PT's HTTPServer found in the API package is a function as a service to allow a decoupled interface to interact with PT using HTTP JSON/XML requests