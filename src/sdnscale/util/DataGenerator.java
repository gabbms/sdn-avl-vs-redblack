package sdnscale.util;

import sdnscale.model.PacketRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public final class DataGenerator {


    /**
     * Semente fixa para o gerador de números aleatórios.
     * Garante reprodutibilidade total dos datasets entre execuções.
     */
    public static final long DEFAULT_SEED = 42L;

    /** Prioridade mínima de uma regra (conforme protocolo OpenFlow). */
    private static final int PRIORITY_MIN = 1;

    /** Prioridade máxima de uma regra (conforme protocolo OpenFlow). */
    private static final int PRIORITY_MAX = 65535;


    private DataGenerator() {
        throw new UnsupportedOperationException(
                "DataGenerator é uma classe utilitária e não deve ser instanciada.");
    }



    public static List<PacketRule> generateSequential(int n) {
        validateN(n);
        Random random = new Random(DEFAULT_SEED);
        List<PacketRule> dataset = new ArrayList<>(n);

        for (int i = 1; i <= n; i++) {
            int priority = PRIORITY_MIN + random.nextInt(PRIORITY_MAX);
            dataset.add(new PacketRule(i, priority));
        }

        return Collections.unmodifiableList(dataset);
    }


    public static List<PacketRule> generateShuffled(int n) {
        validateN(n);
        Random random = new Random(DEFAULT_SEED);

        // Gera IDs de 1 a n e embaralha com a mesma seed
        List<Integer> ids = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) {
            ids.add(i);
        }
        Collections.shuffle(ids, new Random(DEFAULT_SEED));

        List<PacketRule> dataset = new ArrayList<>(n);
        for (int id : ids) {
            int priority = PRIORITY_MIN + random.nextInt(PRIORITY_MAX);
            dataset.add(new PacketRule(id, priority));
        }

        return dataset;
    }


    public static List<PacketRule> generateDescending(int n) {
        validateN(n);
        Random random = new Random(DEFAULT_SEED);
        List<PacketRule> dataset = new ArrayList<>(n);

        for (int i = n; i >= 1; i--) {
            int priority = PRIORITY_MIN + random.nextInt(PRIORITY_MAX);
            dataset.add(new PacketRule(i, priority));
        }

        return Collections.unmodifiableList(dataset);
    }


    public static List<Integer> generateDeleteIds(List<PacketRule> dataset,
                                                  int percentual) {
        if (dataset == null || dataset.isEmpty()) {
            throw new IllegalArgumentException(
                    "O dataset não pode ser nulo ou vazio.");
        }
        if (percentual < 1 || percentual > 100) {
            throw new IllegalArgumentException(
                    "O percentual deve estar entre 1 e 100. Recebido: " + percentual);
        }

        int quantidade = (int) Math.ceil(dataset.size() * (percentual / 100.0));


        List<PacketRule> copia = new ArrayList<>(dataset);
        Collections.shuffle(copia, new Random(DEFAULT_SEED));

        List<Integer> ids = new ArrayList<>(quantidade);
        for (int i = 0; i < quantidade; i++) {
            ids.add(copia.get(i).getRuleId());
        }

        return Collections.unmodifiableList(ids);
    }


    private static void validateN(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade de regras deve ser positiva. Recebido: " + n);
        }
    }
}