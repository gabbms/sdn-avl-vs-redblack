package sdnscale.core;

import sdnscale.model.PacketRule;

/**
 * Contrato polimórfico para os motores de busca do projeto SDN-Scale.
 *
 * <p>Define as operações obrigatórias que toda estrutura de roteamento deve
 * implementar, garantindo que o Integrante 2 (SRE) possa executar benchmarks
 * idênticos e comparáveis entre {@code AVL_Router_Tree} e
 * {@code RedBlack_Router_Tree} sem acoplamento à implementação concreta.</p>
 *
 * <p><b>Contrato de Complexidade Esperado:</b></p>
 * <ul>
 *   <li>insert  – O(log n) amortizado</li>
 *   <li>search  – O(log n) garantido</li>
 *   <li>delete  – O(log n) com rebalanceamento obrigatório até a raiz</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> As implementações desta interface <em>não</em> são
 * thread-safe por padrão. Sincronização externa é responsabilidade do chamador
 * em ambientes concorrentes.</p>
 *
 * @author  Integrante 1 – Lead Software Engineer
 * @version 1.0
 * @see     sdnscale.avl.AVL_Router_Tree
 * @see     sdnscale.redblack.RedBlack_Router_Tree
 */
public interface RouterTree {

    // =========================================================================
    // Operações Nucleares (Escrita)
    // =========================================================================

    /**
     * Insere uma regra de pacote na árvore, mantendo o invariante de
     * balanceamento da implementação concreta após a inserção.
     *
     * <p>Para a AVL, o rebalanceamento deve garantir {@code |FB| ≤ 1} em todos
     * os ancestrais do nó inserido. Para a Red-Black, as 5 propriedades de
     * coloração devem ser restauradas via rotações e recoloração.</p>
     *
     * @param rule regra a ser inserida; não deve ser {@code null}
     * @throws IllegalArgumentException se {@code rule} for {@code null}
     * @throws IllegalStateException    se uma regra com o mesmo ID já existir
     */
    void insert(PacketRule rule);

    /**
     * Remove a regra identificada pelo {@code ruleId}, aplicando o
     * rebalanceamento necessário em toda a cadeia de ancestrais afetados.
     *
     * <p><b>Atenção (Desafio da Deleção):</b> A remoção é o ponto crítico
     * auditado pelo Integrante 2. O algoritmo deve tratar os três casos
     * clássicos (nó folha, nó com um filho, nó com dois filhos via sucessor
     * in-order) e propagar o rebalanceamento até a raiz, prevenindo o
     * "efeito dominó" de desbalanceamento.</p>
     *
     * @param ruleId identificador único da regra a ser removida
     * @return {@code true} se a regra foi encontrada e removida com sucesso;
     *         {@code false} se o ID não existir na árvore
     */
    boolean delete(int ruleId);

    // =========================================================================
    // Operações de Leitura
    // =========================================================================

    /**
     * Localiza e retorna a regra com o {@code ruleId} especificado.
     *
     * <p>Implementações devem garantir que a busca percorra no máximo
     * {@code h} comparações, onde {@code h} é a altura atual da árvore.</p>
     *
     * @param ruleId identificador único da regra buscada
     * @return o objeto {@link PacketRule} correspondente, ou {@code null}
     *         se não encontrado
     */
    PacketRule search(int ruleId);

    // =========================================================================
    // Operações de Inspeção Estrutural (Suporte ao Integrante 3 – QA)
    // =========================================================================

    /**
     * Retorna a altura atual da árvore.
     *
     * <p>Convenção: árvore vazia retorna {@code -1}; árvore com apenas a raiz
     * retorna {@code 0}. Esse valor é utilizado pelo Integrante 3 para validar
     * os invariantes de altura da AVL ({@code h < 1.44 log₂(n+2)}).</p>
     *
     * @return altura da árvore ({@code -1} se vazia)
     */
    int getHeight();

    /**
     * Retorna o número total de nós atualmente armazenados na árvore.
     *
     * @return cardinalidade do conjunto de regras
     */
    int size();

    /**
     * Indica se a árvore não contém nenhuma regra.
     *
     * @return {@code true} se {@code size() == 0}
     */
    boolean isEmpty();

    /**
     * Retorna o número acumulado de rotações realizadas desde a criação da
     * instância (ou desde o último {@code resetRotationCount()}).
     *
     * <p>Métrica primária para a análise de trade-off entre AVL e Red-Black:
     * a AVL tende a realizar mais rotações em workloads write-intensive,
     * enquanto a Red-Black prefere recoloração. O Integrante 3 usa esse valor
     * para quantificar o custo de manutenção de cada estrutura.</p>
     *
     * @return número total de rotações (simples + duplas somadas individualmente)
     */
    int getRotationCount();

    /**
     * Zera o contador interno de rotações.
     *
     * <p>Deve ser chamado pelo Integrante 2 antes de cada fase de benchmark
     * para garantir medições isoladas por operação (inserção vs. deleção).</p>
     */
    void resetRotationCount();

    // =========================================================================
    // Utilitários de Diagnóstico
    // =========================================================================

    /**
     * Executa um percurso em-ordem (in-order) e retorna os IDs das regras
     * em ordem crescente, separados por vírgula.
     *
     * <p>Utilizado pelo Integrante 3 para verificar rapidamente se a
     * propriedade de BST foi preservada após operações de inserção e deleção.</p>
     *
     * <p>Exemplo de saída: {@code "3, 7, 12, 45, 99"}</p>
     *
     * @return string com os IDs em ordem crescente
     */
    String inOrderTraversal();

    /**
     * Retorna uma representação textual estruturada da árvore para fins de
     * depuração e auditoria visual pelo Integrante 3.
     *
     * <p>O formato exato é definido por cada implementação, mas deve incluir,
     * no mínimo, o ID do nó, sua altura (AVL) ou cor (RBT) e os filhos.</p>
     *
     * @return representação em árvore legível por humanos
     */
    String toTreeString();
}