package operator;

/**
 * <p>
 * A utility class that performs dynamic comparison operations between two values at runtime using
 * the Strategy design pattern. The operation is determined based on the operator string provided
 * by the client. Supported operators include:
 * </p>
 *
 * <ul>
 * <li><b>eq</b>, <b>==</b> — Equal to</li>
 * <li><b>ne</b>, <b>!=</b> — Not equal to</li>
 * <li><b>lt</b> — Less than</li>
 * <li><b>le</b> — Less than or equal to</li>
 * <li><b>gt</b> — Greater than</li>
 * <li><b>ge</b> — Greater than or equal to</li>
 * <li><b>=~</b> — Matches regular expression</li>
 * <li><b>!~</b> — Does not match regular expression</li>
 * <li><b>ci</b> — Case-insensitive equals (Windows only)</li>
 * <li><b>me</b> — Must exclude (case-insensitive, Windows only)</li>
 * </ul>
 *
 * <p>
 * This class acts as the context in the Strategy pattern, delegating comparison logic to specific
 * strategy implementations based on the operator.
 * </p>
 *
 * <p>
 * <b>Change Log:</b>
 * </p>
 * <ul>
 * <li>Trevor Maggs - Created on 14 January 2020</li>
 * </ul>
 *
 * @author Trevor Maggs
 * @since 14 January 2020
 */
public final class RelationalOperator
{
    private final OperationStrategy algorithm;

    /**
     * Private constructor to initialise the strategy.
     *
     * @param strategy
     *        a type of strategy that represents a specific comparison operator
     */
    private RelationalOperator(OperationStrategy strategy)
    {
        this.algorithm = strategy;
    }

    /**
     * Executes the strategy with the specified operands.
     *
     * @param <T>
     *        the type of operands, must implement Comparable
     * @param operand1
     *        the left value to be compared
     * @param operand2
     *        the right value to compare with the left value
     * 
     * @return {@code true} if the comparison is successful, {@code false} otherwise
     */
    private <T extends Comparable<T>> boolean executeStrategy(T operand1, T operand2)
    {
        return algorithm.doOperation(operand1, operand2);
    }

    /**
     * Executes a comparison operation between two operands using the specified operator.
     * 
     * 
     * The operator must be one of the supported types: eq, ne, lt, le, gt, ge, =~, !~, ci, me.
     *
     * @param <T>
     *        the type of operands, must implement Comparable
     * @param operand1
     *        the first operand (left-hand side)
     * @param operator
     *        the comparison operator (e.g., "eq", "lt", "=~")
     * @param operand2
     *        the second operand (right-hand side)
     * 
     * @return {@code true} if the comparison operation is successful, {@code false} otherwise
     * @throws IllegalArgumentException
     *         if the operator is undefined or unsupported
     */
    public static <T extends Comparable<T>> boolean execute(T operand1, String operator, T operand2)
    {
        OperationStrategy strategy;

        if (operator == null)
        {
            throw new IllegalArgumentException("Operator cannot be null");
        }

        switch (operator.trim().toLowerCase())
        {
            case "eq":
            case "==":
                strategy = new OperationEqual();
            break;

            case "ne":
            case "!=":
                strategy = new OperationNotEqual();
            break;

            case "lt":
                strategy = new OperationLessThan();
            break;

            case "le":
                strategy = new OperationLessEqual();
            break;

            case "gt":
                strategy = new OperationGreaterThan();
            break;

            case "ge":
                strategy = new OperationGreaterEqual();
            break;

            case "=~":
                strategy = new OperationRegex();
            break;

            case "!~":
                strategy = new OperationRegexNegation();
            break;

            // For Windows only
            case "ci":
            case "me":
                strategy = new OperationEqualIgnoreCase();
            break;

            default:
                throw new IllegalArgumentException("Unknown operator type [" + operator + "]");
        }

        return new RelationalOperator(strategy).executeStrategy(operand1, operand2);
    }
}