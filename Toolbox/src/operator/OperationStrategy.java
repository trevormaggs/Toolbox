package operator;

/**
 * Strategy interface for performing a comparison between two operand values.
 *
 * <p>
 * Clients can implement this interface to define specific comparison logic (equality, less-than,
 * greater-than, etc) between two {@link Comparable} values.
 * </p>
 *
 * <p>
 * Typical use cases include defining pluggable operations for filtering, rule evaluation, or
 * dynamic query construction.
 * </p>
 *
 * @version 1.0
 * @author Trevor Maggs
 * @since 11 July 2025
 */
public interface OperationStrategy
{
    /**
     * Applies the comparison operation to two operands.
     *
     * @param <T>
     *        the type of operands, must be {@link Comparable}
     * @param operand1
     *        the first operand
     * @param operand2
     *        the second operand
     * 
     * @return true if the comparison succeeds according to the strategy, false otherwise
     */
    <T extends Comparable<T>> boolean doOperation(T operand1, T operand2);
}