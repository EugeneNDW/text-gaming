package ndw.eugene.textgaming.services

import ndw.eugene.textgaming.data.entity.GameState
import org.springframework.stereotype.Service
import java.util.*

private const val AND_OPERATOR = "&&"

private const val OR_OPERATOR = "||"

private const val OPEN_PARENTHESES = "("

private const val CLOSE_PARENTHESES = ")"

const val CHECK_CONDITION_PREFIX = "CHECK"
const val CHECK_NOT_CONDITION_PREFIX = "CHECK_NOT"

const val MORETHAN_CONDITION_PREFIX = "MORETHAN"
const val EQUALS_CONDITION_PREFIX = "EQUALS"
const val LESSTHAN_CONDITION_PREFIX = "LESSTHAN"

private const val CONDITION_DELIMITER = ":"

@Service
class ConditionService {
    fun evaluateCondition(condition: String, gameState: GameState): Boolean {
        if (condition.isBlank()) {
            return true
        }
        val infixCondition = infixToPostfix(condition)
        return evaluatePostfix(infixCondition, gameState)
    }

    fun evaluatePostfix(postfix: List<String>, gameState: GameState): Boolean {
        val stack = Stack<Boolean>()
        for (token in postfix) {
            if (isOperand(token)) {
                stack.push(evaluateExpression(token, gameState))
            } else if (isOperator(token)) {
                val b = stack.pop()
                val a = stack.pop()
                stack.push(applyOperator(token, a, b))
            }
        }
        return stack.pop()
    }

    fun infixToPostfix(expression: String): List<String> {
        val operators = Stack<String>()
        val output = ArrayList<String>()

        val replacedExpression = expression
            .replace(AND_OPERATOR, " $AND_OPERATOR ")
            .replace(OR_OPERATOR, " $OR_OPERATOR ")
            .replace(OPEN_PARENTHESES, " $OPEN_PARENTHESES ")
            .replace(CLOSE_PARENTHESES, " $CLOSE_PARENTHESES ")
            .trim()
        val tokens = replacedExpression.split("\\s+".toRegex())

        for (token in tokens) {
            when {
                isOperand(token) -> {
                    output.add(token)
                }

                token == OPEN_PARENTHESES -> {
                    operators.push(token)
                }

                token == CLOSE_PARENTHESES -> {
                    while (operators.peek() != OPEN_PARENTHESES) {
                        output.add(operators.pop())
                    }
                    operators.pop()
                }

                isOperator(token) -> {
                    while (!operators.isEmpty() && hasPrecedence(token, operators.peek())) {
                        output.add(operators.pop())
                    }
                    operators.push(token)
                }
            }
        }

        while (!operators.isEmpty()) {
            output.add(operators.pop())
        }

        return output
    }

    private fun isOperator(token: String): Boolean {
        return token == AND_OPERATOR || token == OR_OPERATOR
    }

    private fun isOperand(token: String): Boolean {
        return !isOperator(token) && token != OPEN_PARENTHESES && token != CLOSE_PARENTHESES
    }

    //the operator op2 on the stack has higher or equal precedence compared to the current operator op1 being processed.
    private fun hasPrecedence(currentOperator: String, stackOperator: String): Boolean {
        return if (stackOperator == OPEN_PARENTHESES || stackOperator == CLOSE_PARENTHESES) {
            false
        } else if (currentOperator == AND_OPERATOR && stackOperator == OR_OPERATOR) {
            false
        } else {
            true
        }
    }

    private fun evaluateExpression(token: String, gameState: GameState): Boolean {
        val parts = token.split(CONDITION_DELIMITER)
        return when {
            token.startsWith(CHECK_NOT_CONDITION_PREFIX) -> {
                !checkCondition(parts[1], gameState)
            }

            token.startsWith(CHECK_CONDITION_PREFIX) -> {
                checkCondition(parts[1], gameState)
            }

            token.startsWith(MORETHAN_CONDITION_PREFIX) -> {
                moreThanCondition(parts[1], parts[2], gameState)
            }

            token.startsWith(EQUALS_CONDITION_PREFIX) -> {
                equals(parts[1], parts[2], gameState)
            }

            else -> {
                false
            }
        }
    }

    private fun checkCondition(choiceName: String, gameState: GameState): Boolean {
        val gameChoice = gameState.gameChoices.find { it.choice.name == choiceName }
        return gameChoice != null
    }

    private fun moreThanCondition(number: String, counterName: String, gameState: GameState): Boolean {
        val counterValue = gameState.gameCounters.find { it.counter.name == counterName }?.counterValue ?: 0
        return counterValue < number.toInt()
    }

    private fun equals(number: String, counterName: String, gameState: GameState): Boolean {
        val counterValue = gameState.gameCounters.find { it.counter.name == counterName }?.counterValue ?: 0
        return counterValue == number.toInt()
    }

    private fun applyOperator(operator: String, a: Boolean, b: Boolean): Boolean {
        return when (operator) {
            AND_OPERATOR -> a && b
            OR_OPERATOR -> a || b
            else -> false
        }
    }
}