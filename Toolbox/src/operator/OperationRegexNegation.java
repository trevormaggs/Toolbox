package operator;

import common.PatternMatch;

/**
 * <p>
 * A strategy class used as an input instance in the operator context to check for a negative
 * Regular Expression (negation) evaluation result.
 * </p>
 * 
 * <ul>
 * <li>Trevor Maggs created on 14 January 2020</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @since 14 January 2020
 */
public class OperationRegexNegation implements OperationStrategy
{
    /**
     * Evaluates the Regular Expression to determine whether the result is successfully negative
     * (negation check).
     * 
     * @param regex
     *        the given Regular Expression
     * @param value
     *        the actual value to be evaluated
     * 
     * @return boolean true if the operation is successful
     */
    @Override
    public <T extends Comparable<T>> boolean doOperation(T regex, T value)
    {
        String a = (String) regex;
        String b = (String) value;

        return (!PatternMatch.matches(a, b));
    }
}