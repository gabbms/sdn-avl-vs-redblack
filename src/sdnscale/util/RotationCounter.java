package sdnscale.util;


public final class RotationCounter {


    /** Mapa de instâncias por nome (ex.: "AVL", "RBT"). */
    private static final java.util.Map<String, RotationCounter> INSTANCES =
            new java.util.HashMap<>();


    public static RotationCounter getInstance(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do contador não pode ser null ou vazio.");
        }
        return INSTANCES.computeIfAbsent(name, RotationCounter::new);
    }

    /** Nome desta instância (ex.: "AVL"). */
    private final String name;

    /** Número acumulado de rotações desde a última chamada a {@link #reset()}. */
    private int count;


    private RotationCounter(String name) {
        this.name  = name;
        this.count = 0;
    }


    public void increment() {
        count++;
    }

    public int getCount() {
        return count;
    }


    public void reset() {
        count = 0;
    }


    public static void resetAll() {
        INSTANCES.values().forEach(RotationCounter::reset);
    }



    @Override
    public String toString() {
        return String.format("RotationCounter[%s] = %,d rotações", name, count);
    }
}