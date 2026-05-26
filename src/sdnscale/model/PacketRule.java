package sdnscale.model;

import java.util.Objects;


public final class PacketRule implements Comparable<PacketRule> {


    private final int ruleId;


    /** Endereço IP de origem no formato CIDR (ex.: {@code "10.0.0.1/32"}). */
    private String sourceIp;

    /** Endereço IP de destino no formato CIDR (ex.: {@code "172.16.0.0/16"}). */
    private String destinationIp;


    private int priority;


    public PacketRule(int ruleId, String sourceIp, String destinationIp, int priority) {
        validateRuleId(ruleId);
        validateIp(sourceIp, "sourceIp");
        validateIp(destinationIp, "destinationIp");
        validatePriority(priority);

        this.ruleId        = ruleId;
        this.sourceIp      = sourceIp;
        this.destinationIp = destinationIp;
        this.priority      = priority;
    }


    public PacketRule(int ruleId, int priority) {
        this(ruleId,
                generateSyntheticIp(ruleId, true),
                generateSyntheticIp(ruleId, false),
                priority);
    }


    @Override
    public int compareTo(PacketRule other) {
        Objects.requireNonNull(other, "A regra comparada não pode ser null.");
        return Integer.compare(this.ruleId, other.ruleId);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PacketRule)) return false;
        PacketRule other = (PacketRule) obj;
        return this.ruleId == other.ruleId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(ruleId);
    }


    @Override
    public String toString() {
        return String.format(
                "PacketRule{id=%d, src='%s', dst='%s', priority=%d}",
                ruleId, sourceIp, destinationIp, priority
        );
    }


    public int getRuleId() {
        return ruleId;
    }


    public String getSourceIp() {
        return sourceIp;
    }

    public String getDestinationIp() {
        return destinationIp;
    }


    public int getPriority() {
        return priority;
    }


    public void setSourceIp(String sourceIp) {
        validateIp(sourceIp, "sourceIp");
        this.sourceIp = sourceIp;
    }


    public void setDestinationIp(String destinationIp) {
        validateIp(destinationIp, "destinationIp");
        this.destinationIp = destinationIp;
    }


    public void setPriority(int priority) {
        validatePriority(priority);
        this.priority = priority;
    }

    private static void validateRuleId(int ruleId) {
        if (ruleId <= 0) {
            throw new IllegalArgumentException(
                    "ruleId deve ser positivo. Recebido: " + ruleId
            );
        }
    }

    private static void validateIp(String ip, String fieldName) {
        if (ip == null || ip.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " não pode ser null ou vazio."
            );
        }
    }

    private static void validatePriority(int priority) {
        if (priority < 1 || priority > 65535) {
            throw new IllegalArgumentException(
                    "priority deve estar entre 1 e 65535. Recebido: " + priority
            );
        }
    }


    private static String generateSyntheticIp(int ruleId, boolean isSource) {
        int base   = isSource ? 10 : 172;
        int octet2 = (ruleId >> 16) & 0xFF;
        int octet3 = (ruleId >> 8)  & 0xFF;
        int octet4 = ruleId         & 0xFF;
        return String.format("%d.%d.%d.%d/32", base, octet2, octet3, octet4);
    }
}