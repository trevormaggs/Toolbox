package operator;


/**
 * A strategy class that may be used as an input instance in the operator context to facilitate a
 * case-insensitive <em>Equal To</em> comparison operation.
 * 
 * <ul>
 * <li>Trevor Maggs created on 28 September 2020</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @since 28 September 2020
 */
public class OperationEqualIgnoreCase implements OperationStrategy
{
    /**
     * Executes the case-insensitive {@code Equal To} operation.
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
        return (operand1.toString().toLowerCase().compareTo(operand2.toString().toLowerCase()) == 0);
    }
}