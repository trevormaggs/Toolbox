package operator;


public class StrategyPatternTest
{
    public static void main(String[] args)
    {
        System.out.println(RelationalOperator.execute(5, "eq", 6));
        System.out.println(RelationalOperator.execute(5, "ne", 6));
        System.out.println(RelationalOperator.execute(5, "lt", 6));
        System.out.println(RelationalOperator.execute("Apple", "eq", "apple"));
        System.out.println(RelationalOperator.execute("Pear", "le", "Apple"));
        System.out.println(RelationalOperator.execute("Pear", "gt", "Apple"));
        System.out.println(RelationalOperator.execute(7, "ge", 8));
        System.out.println(RelationalOperator.execute("^\\s*Hello.*$", "=~", "Hello Trev!"));
        System.out.println(RelationalOperator.execute("^\\s*Hello.*$", "!~", "Trev!"));
    }
}