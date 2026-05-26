package sdnscale.redblack;

import sdnscale.model.PacketRule;

/**
 * Nó interno da Árvore Red-Black do projeto SDN-Scale.
 *
 * <p>Além dos campos comuns a qualquer nó de BST ({@code rule},
 * {@code left}, {@code right}), carrega o campo {@link #color} que é
 * o elemento central das 5 propriedades da Red-Black Tree.</p>
 *
 * <p><b>Convenção do nó sentinela NIL:</b> Em vez de usar {@code null}
 * para representar folhas ausentes, a {@link RedBlack_Router_Tree} utiliza
 * um nó sentinela estático {@code NIL} de cor {@link Color#BLACK}. Isso
 * simplifica o tratamento de casos-limite nas rotações e no
 * rebalanceamento pós-deleção, eliminando verificações de {@code null}
 * espalhadas pelo código.</p>
 *
 * <p><b>Visibilidade de pacote:</b> Esta classe é intencionalmente
 * package-private. Nenhum código fora de {@code sdnscale.redblack} deve
 * manipular nós diretamente.</p>
 *
 * @author  Integrante 1 – Lead Software Engineer
 * @version 1.0
 * @see     RedBlack_Router_Tree
 */
class RBNode {

    // =========================================================================
    // Enumeração de cores
    // =========================================================================

    /**
     * As duas cores possíveis de um nó Red-Black.
     *
     * <p>As 5 propriedades fundamentais da RBT dependem desta distinção:</p>
     * <ol>
     *   <li>Todo nó é RED ou BLACK.</li>
     *   <li>A raiz é sempre BLACK.</li>
     *   <li>Todo nó NIL (folha sentinela) é BLACK.</li>
     *   <li>Se um nó é RED, ambos os filhos são BLACK.</li>
     *   <li>Todo caminho de qualquer nó até seus descendentes NIL
     *       contém o mesmo número de nós BLACK (black-height).</li>
     * </ol>
     */
    enum Color {
        RED, BLACK
    }

    // =========================================================================
    // Nó sentinela NIL
    // =========================================================================

    /**
     * Nó sentinela compartilhado por toda a árvore para representar
     * ausência de filho (substitui {@code null}).
     *
     * <p>É sempre BLACK e não carrega dados reais. Nunca modifique
     * {@code NIL.color} — isso quebraria todas as instâncias da RBT.</p>
     */
    static final RBNode NIL = new RBNode();

    static {
        NIL.color  = Color.BLACK;
        NIL.left   = NIL;
        NIL.right  = NIL;
        NIL.parent = NIL;
        NIL.rule   = null;
    }

    // =========================================================================
    // Campos
    // =========================================================================

    /** Regra de roteamento armazenada. {@code null} apenas no nó {@link #NIL}. */
    PacketRule rule;

    /** Cor atual do nó. */
    Color color;

    /** Filho esquerdo. Nunca {@code null} — usa {@link #NIL} no lugar. */
    RBNode left;

    /** Filho direito. Nunca {@code null} — usa {@link #NIL} no lugar. */
    RBNode right;

    /** Pai do nó. {@code NIL} se este nó é a raiz. */
    RBNode parent;

    // =========================================================================
    // Construtores
    // =========================================================================

    /**
     * Construtor privado usado exclusivamente para criar o nó sentinela
     * {@link #NIL}.
     */
    private RBNode() {
        this.rule   = null;
        this.color  = Color.BLACK;
        this.left   = null;
        this.right  = null;
        this.parent = null;
    }

    /**
     * Cria um novo nó RED com a regra fornecida.
     *
     * <p>Todo nó recém-inserido começa como RED — a correção de cor
     * ocorre depois em {@code RedBlack_Router_Tree.fixInsert()}.</p>
     *
     * @param rule regra a ser armazenada; não deve ser {@code null}
     */
    RBNode(PacketRule rule) {
        this.rule   = rule;
        this.color  = Color.RED;
        this.left   = NIL;
        this.right  = NIL;
        this.parent = NIL;
    }

    // =========================================================================
    // Utilitário de diagnóstico
    // =========================================================================

    /**
     * Representação textual do nó para auditoria do Integrante 3.
     *
     * <p>Exemplo: {@code "RBNode{id=42, cor=RED}"}</p>
     */
    @Override
    public String toString() {
        if (this == NIL) return "NIL(BLACK)";
        return String.format("RBNode{id=%d, cor=%s}",
                rule.getRuleId(), color);
    }
}