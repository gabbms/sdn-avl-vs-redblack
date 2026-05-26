package sdnscale.avl;

import sdnscale.model.PacketRule;

/**
 * Nó interno da Árvore AVL do projeto SDN-Scale.
 *
 * <p>Encapsula a {@link PacketRule} armazenada, as referências para os filhos
 * esquerdo e direito, e a altura do nó — dado essencial para o cálculo do
 * Fator de Balanceamento (FB) durante inserções e deleções.</p>
 *
 * <p><b>Invariante mantida pela {@code AVL_Router_Tree}:</b>
 * Para todo nó {@code n}, {@code |FB(n)| ≤ 1}, onde
 * {@code FB(n) = altura(n.left) - altura(n.right)}.</p>
 *
 * <p><b>Visibilidade de pacote:</b> Esta classe é intencionalmente
 * package-private. Nenhum código fora de {@code sdnscale.avl} deve
 * manipular nós diretamente — toda interação ocorre pela interface
 * {@link sdnscale.core.RouterTree}.</p>
 *
 * @author  Integrante 1 – Lead Software Engineer
 * @version 1.0
 * @see     AVL_Router_Tree
 */
class AVLNode {

    // =========================================================================
    // Campos
    // =========================================================================

    /** Regra de roteamento armazenada neste nó. Nunca {@code null}. */
    PacketRule rule;

    /** Filho esquerdo (chave menor). {@code null} se ausente. */
    AVLNode left;

    /** Filho direito (chave maior). {@code null} se ausente. */
    AVLNode right;

    /**
     * Altura deste nó na árvore.
     *
     * <p>Convenção: nó folha tem altura {@code 0}. Um nó {@code null}
     * é tratado como altura {@code -1} pelo método auxiliar
     * {@link AVL_Router_Tree#height(AVLNode)}.</p>
     */
    int height;

    // =========================================================================
    // Construtor
    // =========================================================================

    /**
     * Cria um novo nó folha com a regra fornecida.
     *
     * <p>Filhos iniciam como {@code null} e altura como {@code 0},
     * pois todo nó recém-inserido começa como folha.</p>
     *
     * @param rule regra a ser armazenada; não deve ser {@code null}
     */
    AVLNode(PacketRule rule) {
        this.rule   = rule;
        this.left   = null;
        this.right  = null;
        this.height = 0;
    }

    // =========================================================================
    // Utilitário de diagnóstico
    // =========================================================================

    /**
     * Representação textual do nó para depuração e auditoria do Integrante 3.
     *
     * <p>Exemplo: {@code "AVLNode{id=42, h=2, FB=1}"}</p>
     *
     * @return string com ID, altura e fator de balanceamento do nó
     */
    @Override
    public String toString() {
        int leftHeight  = (left  != null) ? left.height  : -1;
        int rightHeight = (right != null) ? right.height : -1;
        int fb          = leftHeight - rightHeight;
        return String.format("AVLNode{id=%d, h=%d, FB=%d}",
                rule.getRuleId(), height, fb);
    }
}