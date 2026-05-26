package sdnscale.redblack;

import sdnscale.model.PacketRule;


class RBNode {



    enum Color {
        RED, BLACK
    }



    static final RBNode NIL = new RBNode();

    static {
        NIL.color  = Color.BLACK;
        NIL.left   = NIL;
        NIL.right  = NIL;
        NIL.parent = NIL;
        NIL.rule   = null;
    }


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



    private RBNode() {
        this.rule   = null;
        this.color  = Color.BLACK;
        this.left   = null;
        this.right  = null;
        this.parent = null;
    }


    RBNode(PacketRule rule) {
        this.rule   = rule;
        this.color  = Color.RED;
        this.left   = NIL;
        this.right  = NIL;
        this.parent = NIL;
    }



    @Override
    public String toString() {
        if (this == NIL) return "NIL(BLACK)";
        return String.format("RBNode{id=%d, cor=%s}",
                rule.getRuleId(), color);
    }
}