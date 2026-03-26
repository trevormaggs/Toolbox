package operator;


/**
 * <p>
 * A strategy class used as an input instance in the operator context to provide a <em>Greater
 * Than</em> comparison operation.
 * </p>
 * 
 * <ul>
 * <li>Trevor Maggs created on 14 January 2020</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @since 14 January 2020
 */
public class OperationLessThan implements OperationStrategy
{    
    /**
     * Executes the {@code Less Than} operation.
     * 
     * @param operand1
     *        the left value to be compared
     * @param operand2
     *        the right value to compare with the left value
     * 
     * @return boolean true if the operation is successful
     */
    @Override
    public <T extends Comparable<T>> boolean doOperation(T operand1, T operand2)
    {
        return (operand1.compareTo(operand2) < 0);
    }
}