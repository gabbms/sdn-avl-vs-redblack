package sdnscale.util;

/**
 * Contador global de rotações para as árvores AVL e Red-Black.
 *
 * <p>Implementado como Singleton com estado por instância nomeada,
 * permitindo que o Integrante 2 (SRE) mantenha contadores <em>isolados</em>
 * para a AVL e para a Red-Black, evitando contaminação de métricas entre
 * as duas estruturas durante os benchmarks comparativos.</p>
 *
 * <h2>Como usar</h2>
 * <pre>{@code
 * // Integrante 1 — dentro de AVL_Router_Tree, a cada rotação:
 * RotationCounter.getInstance("AVL").increment();
 *
 * // Integrante 1 — dentro de RedBlack_Router_Tree:
 * RotationCounter.getInstance("RBT").increment();
 *
 * // Integrante 2 — antes de cada fase de benchmark:
 * RotationCounter.getInstance("AVL").reset();
 * // ... executa carga ...
 * int totalRotacoes = RotationCounter.getInstance("AVL").getCount();
 * }</pre>
 *
 * <p><b>Thread Safety:</b> Esta implementação <em>não</em> é thread-safe.
 * Os benchmarks devem ser executados single-threaded para garantir
 * medições determinísticas em nanossegundos, conforme exigido pelo
 * professor.</p>
 *
 * @author  Integrante 1 – Lead Software Engineer
 * @version 1.0
 * @see     sdnscale.core.RouterTree#getRotationCount()
 * @see     sdnscale.core.RouterTree#resetRotationCount()
 */
public final class RotationCounter {

    // =========================================================================
    // Gerenciamento de instâncias nomeadas
    // =========================================================================

    /** Mapa de instâncias por nome (ex.: "AVL", "RBT"). */
    private static final java.util.Map<String, RotationCounter> INSTANCES =
            new java.util.HashMap<>();

    /**
     * Retorna a instância nomeada do contador, criando-a se ainda não existir.
     *
     * @param name identificador da instância (ex.: {@code "AVL"}, {@code "RBT"})
     * @return instância única associada ao nome fornecido
     * @throws IllegalArgumentException se {@code name} for {@code null} ou vazio
     */
    public static RotationCounter getInstance(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do contador não pode ser null ou vazio.");
        }
        return INSTANCES.computeIfAbsent(name, RotationCounter::new);
    }

    // =========================================================================
    // Estado interno
    // =========================================================================

    /** Nome desta instância (ex.: "AVL"). */
    private final String name;

    /** Número acumulado de rotações desde a última chamada a {@link #reset()}. */
    private int count;

    // =========================================================================
    // Construtor privado
    // =========================================================================

    private RotationCounter(String name) {
        this.name  = name;
        this.count = 0;
    }

    // =========================================================================
    // API pública
    // =========================================================================

    /**
     * Incrementa o contador em 1.
     *
     * <p>Deve ser chamado uma vez por rotação simples e uma vez por cada
     * rotação componente de uma rotação dupla — ou seja, uma rotação dupla
     * contribui com {@code +2} no total.</p>
     */
    public void increment() {
        count++;
    }

    /**
     * Retorna o número acumulado de rotações.
     *
     * @return total de rotações registradas desde a última chamada a
     *         {@link #reset()}, ou desde a criação da instância
     */
    public int getCount() {
        return count;
    }

    /**
     * Zera o contador.
     *
     * <p>O Integrante 2 deve chamar este método antes de cada fase de
     * benchmark (inserção, busca, deleção) para garantir medições isoladas.</p>
     */
    public void reset() {
        count = 0;
    }

    /**
     * Zera os contadores de <em>todas</em> as instâncias registradas.
     *
     * <p>Útil para reinicialização completa do ambiente de teste antes
     * de uma nova rodada de benchmarks.</p>
     */
    public static void resetAll() {
        INSTANCES.values().forEach(RotationCounter::reset);
    }

    // =========================================================================
    // Diagnóstico
    // =========================================================================

    /**
     * Representação textual para logs do Post-Mortem.
     *
     * <p>Exemplo: {@code "RotationCounter[AVL] = 1.847 rotações"}</p>
     */
    @Override
    public String toString() {
        return String.format("RotationCounter[%s] = %,d rotações", name, count);
    }
}