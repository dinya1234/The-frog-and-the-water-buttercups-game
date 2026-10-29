package com.example.game;

import com.example.model.Pond;
import com.example.solver.FrogJumper;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomPondFactoryTest {

    @Test
    void generatedPondsArePlayableAndNotEmpty() {
        for (int seed = 0; seed < 25; seed++) {
            Pond pond = RandomPondFactory.random(new Random(seed));

            assertTrue(pond.size() >= RandomPondFactory.MIN_PADS,
                    "seed=" + seed + ", size=" + pond.size());
            assertTrue(pond.size() <= RandomPondFactory.MAX_PADS,
                    "seed=" + seed + ", size=" + pond.size());
            assertEquals(0, pond.jump(pond.lastIndex()), "последняя кувшинка должна быть целью");
            assertTrue(FrogJumper.canReach(pond.jumps()), "seed=" + seed + ": " + pond);
            assertTrue(FrogJumper.minJumps(pond.jumps()) >= 3,
                    "seed=" + seed + ": слишком простой пруд " + pond);
        }
    }
}
