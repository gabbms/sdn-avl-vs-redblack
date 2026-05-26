package sdnscale.util;

import sdnscale.model.PacketRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Gerador determinístico de datasets para os benchmarks do projeto SDN-Scale.
 *
 * <p>Utiliza uma semente (seed) fixa para garantir que os mesmos dados sejam
 * gerados sempre que o Integrante 2 rodar os testes de carga, tornando os
 * resultados reproduzíveis e comparáveis entre máquinas diferentes.</p>
 *
 * <p><b>Seed padrão:</b> {@value #DEFAULT_SEED}. Qualquer alteração nesse
 * valor invalida comparações históricas de benchmark — só mude com
 * consenso da equipe.</p>
 *
 * <p><b>Exemplo de uso pelo Integrante 2:</b></p>
 * <pre>{@code
 * List<PacketRule> dataset = DataGenerator.generateSequential(100_000);
 * List<PacketRule> aleatorio = DataGenerator.generateShuffled(100_000);
 * List<Integer> parasRemover = DataGenerator.generateDeleteIds(dataset, 20);
 * }</pre>
 *
 * @author  Integrante 1 – Lead Software Engineer
 * @version 1.0
 * @see     PacketRule
 */
public final class DataGenerator {

    // =========================================================================
    // Constantes
    // =========================================================================

    /**
     * Semente fixa para o gerador de números aleatórios.
     * Garante reprodutibilidade total dos datasets entre execuções.
     */
    public static final long DEFAULT_SEED = 42L;

    /** Prioridade mínima de uma regra (conforme protocolo OpenFlow). */
    private static final int PRIORITY_MIN = 1;

    /** Prioridade máxima de uma regra (conforme protocolo OpenFlow). */
    private static final int PRIORITY_MAX = 65535;

    // =========================================================================
    // Construtor privado — classe utilitária, não instanciável
    // =========================================================================

    private DataGenerator() {
        throw new UnsupportedOperationException(
                "DataGenerator é uma classe utilitária e não deve ser instanciada.");
    }

    // =========================================================================
    // Geração de datasets
    // =========================================================================

    /**
     * Gera uma lista de {@code n} regras com IDs sequenciais (1 a n) e
     * prioridades aleatórias com seed fixa.
     *
     * <p>Inserção sequencial é o cenário mais favorável para a AVL e o
     * mais crítico para testar o rebalanceamento em árvores degeneradas
     * (sem balanceamento, viraria uma lista encadeada).</p>
     *
     * @param n quantidade de regras a gerar; deve ser positivo
     * @return lista imutável com {@code n} regras em ordem crescente de ID
     * @throws IllegalArgumentException se {@code n <= 0}
     */
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

    /**
     * Gera uma lista de {@code n} regras com IDs embaralhados aleatoriamente
     * e prioridades aleatórias, ambos com seed fixa.
     *
     * <p>Inserção aleatória é o cenário médio de workload real de um
     * roteador SDN, onde regras chegam em ordem imprevisível. É o dataset
     * principal dos benchmarks comparativos AVL vs. Red-Black.</p>
     *
     * @param n quantidade de regras a gerar; deve ser positivo
     * @return lista com {@code n} regras em ordem aleatória de ID
     * @throws IllegalArgumentException se {@code n <= 0}
     */
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

    /**
     * Gera uma lista de {@code n} regras com IDs em ordem decrescente
     * (n até 1) e prioridades aleatórias com seed fixa.
     *
     * <p>Inserção decrescente é o pior caso clássico de uma BST sem
     * balanceamento (degeneraria para lista encadeada com altura n-1).
     * Serve para estressar o rebalanceamento e validar que a altura
     * da AVL permanece dentro de {@code h < 1.44·log₂(n+2)}.</p>
     *
     * @param n quantidade de regras a gerar; deve ser positivo
     * @return lista com {@code n} regras em ordem decrescente de ID
     * @throws IllegalArgumentException se {@code n <= 0}
     */
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

    /**
     * Seleciona deterministicamente {@code percentual}% dos IDs de um
     * dataset para serem usados nas operações de deleção nos benchmarks.
     *
     * <p>Conforme exigido pelo professor, os testes de deleção devem
     * remover aproximadamente 20% dos nós inseridos. Usando seed fixa,
     * o conjunto de IDs a deletar é sempre o mesmo para uma mesma
     * entrada, garantindo reprodutibilidade.</p>
     *
     * <p><b>Exemplo:</b> Para 100.000 nós inseridos com
     * {@code percentual = 20}, retorna uma lista de 20.000 IDs
     * escolhidos aleatoriamente (mas deterministicamente) do dataset.</p>
     *
     * @param dataset    lista de regras já inseridas na árvore
     * @param percentual porcentagem de nós a deletar (ex.: {@code 20} para 20%)
     * @return lista de IDs a serem passados para {@code RouterTree.delete()}
     * @throws IllegalArgumentException se {@code dataset} for nulo/vazio ou
     *                                  se {@code percentual} estiver fora de
     *                                  [1, 100]
     */
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

        // Copia e embaralha com seed fixa para seleção determinística
        List<PacketRule> copia = new ArrayList<>(dataset);
        Collections.shuffle(copia, new Random(DEFAULT_SEED));

        List<Integer> ids = new ArrayList<>(quantidade);
        for (int i = 0; i < quantidade; i++) {
            ids.add(copia.get(i).getRuleId());
        }

        return Collections.unmodifiableList(ids);
    }

    // =========================================================================
    // Auxiliar de validação
    // =========================================================================

    private static void validateN(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade de regras deve ser positiva. Recebido: " + n);
        }
    }
}