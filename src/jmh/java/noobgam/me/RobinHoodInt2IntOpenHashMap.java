package noobgam.me;

import it.unimi.dsi.fastutil.HashCommon;

import java.util.Arrays;

public class RobinHoodInt2IntOpenHashMap
{
    private int[] keys;
    private int[] values;

    // take from the rich - give to the poor
    public byte[] dist;

    private int realSize;
    private transient int mask;

    public RobinHoodInt2IntOpenHashMap(int expected, float loadFactor) {
        int realSize = HashCommon.arraySize(expected, loadFactor);
        mask = realSize - 1;
        keys = new int[realSize];
        values = new int[realSize];
        dist = new byte[realSize];
        Arrays.fill(dist, (byte)-1);
    }

    // undefined behaviour if too many hash collisions occur
    //  or map is completely full
    public Integer get(int key) {
        int pos = (it.unimi.dsi.fastutil.HashCommon.mix((key))) & mask;
        for (;;) {
            if (dist[pos] == -1) {
                return null;
            } else if (keys[pos] == key) {
                return values[pos];
            }
            pos = (pos + 1) & mask;
        }
    }

    // undefined behaviour if too many hash collisions occur
    //  or map is completely full
    public void put(int key, int val) {
        int pos = (it.unimi.dsi.fastutil.HashCommon.mix((key))) & mask;
        byte currentDist = 0;

        for (;;) {
            if (dist[pos] == -1) {
                keys[pos] = key;
                values[pos] = val;
                dist[pos] = currentDist;
                return;
            } else if (dist[pos] < currentDist) {
                // seems odd. Perhaps due to primitives Entry approach could be faster.

                int oldKey = keys[pos];
                int oldVal = values[pos];
                byte oldDist = dist[pos];

                keys[pos] = key;
                values[pos] = val;
                dist[pos] = currentDist;

                key = oldKey;
                val = oldVal;
                currentDist = oldDist;
            }
            //undefined behaviour if overflows.
            ++currentDist;
            pos = (pos + 1) & mask;
        }
    }
}

