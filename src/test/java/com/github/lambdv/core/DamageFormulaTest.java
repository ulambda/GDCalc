package com.github.lambdv.core;
import com.github.lambdv.core.DamageFormulas;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;



public class DamageFormulaTest {
    @Test public void testAvgCritMultiplier(){
        assertEquals(1, DamageFormulas.AvgCritMultiplier(0, 0), 0.0001); //0 cr, cd does not matter
        assertEquals(1, DamageFormulas.AvgCritMultiplier(0, 1.0), 0.0001); //0 cr with some cd
        assertEquals(1, DamageFormulas.AvgCritMultiplier(-1.2, 1.0), 0.0001); //negative cr does not matter
        assertEquals(2, DamageFormulas.AvgCritMultiplier(1.2, 1.0), 0.0001); //negative cr does not matter

        //normal cases
        assertEquals(2, DamageFormulas.AvgCritMultiplier(1, 1.0), 0.0001);
        assertEquals(1.5, DamageFormulas.AvgCritMultiplier(0.5, 1.0), 0.0001);
        assertEquals(1.84, DamageFormulas.AvgCritMultiplier(0.7, 1.2), 0.0001);
    }

    @Test public void testDefMultiplier(){  
        var e = Enemy.KQMC();
        assertEquals(0.487179487179487, DamageFormulas.DefMultiplier(90, e, 0.0, 0.0), 0.0001);
        assertEquals(0.655172413793103, DamageFormulas.DefMultiplier(90, e, 0.5, 0), 0.0001);
        assertEquals(0.791666667, DamageFormulas.DefMultiplier(90, e, 0.5, 0.5), 0.0001);
        assertEquals(0.904761905, DamageFormulas.DefMultiplier(90, e, 0.9, 0), 0.0001);
        assertEquals(0.904761905, DamageFormulas.DefMultiplier(90, e, 100000, 0), 0.0001);
    }
}