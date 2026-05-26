package sdnscale.avl;

import sdnscale.model.PacketRule;


class AVLNode {


    /** Regra de roteamento armazenada neste nó. Nunca {@code null}. */
    PacketRule rule;

    /** Filho esquerdo (chave menor). {@code null} se ausente. */
    AVLNode left;

    /** Filho direito (chave maior). {@code null} se ausente. */
    AVLNode right;


    int height;


    AVLNode(PacketRule rule) {
        this.rule   = rule;
        this.left   = null;
        this.right  = null;
        this.height = 0;
    }


    @Override
    public String toString() {
        int leftHeight  = (left  != null) ? left.height  : -1;
        int rightHeight = (right != null) ? right.height : -1;
        int fb          = leftHeight - rightHeight;
        return String.format("AVLNode{id=%d, h=%d, FB=%d}",
                rule.getRuleId(), height, fb);
    }
}