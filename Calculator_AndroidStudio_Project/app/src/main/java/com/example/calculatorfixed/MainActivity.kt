package com.example.calculatorfixed

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btn_0).setOnClickListener { showSymbol('0') }
        findViewById<Button>(R.id.btn_point).setOnClickListener { showSymbol('.') }
        findViewById<Button>(R.id.btn_equals).setOnClickListener { calculate() }
        findViewById<Button>(R.id.btn_minus).setOnClickListener { showSymbol('-') }

        findViewById<Button>(R.id.btn_1).setOnClickListener { showSymbol('1') }
        findViewById<Button>(R.id.btn_2).setOnClickListener { showSymbol('2') }
        findViewById<Button>(R.id.btn_3).setOnClickListener { showSymbol('3') }
        findViewById<Button>(R.id.btn_plus).setOnClickListener { showSymbol('+') }

        findViewById<Button>(R.id.btn_4).setOnClickListener { showSymbol('4') }
        findViewById<Button>(R.id.btn_5).setOnClickListener { showSymbol('5') }
        findViewById<Button>(R.id.btn_6).setOnClickListener { showSymbol('6') }
        findViewById<Button>(R.id.btn_multiply).setOnClickListener { showSymbol('*') }

        findViewById<Button>(R.id.btn_7).setOnClickListener { showSymbol('7') }
        findViewById<Button>(R.id.btn_8).setOnClickListener { showSymbol('8') }
        findViewById<Button>(R.id.btn_9).setOnClickListener { showSymbol('9') }
        findViewById<Button>(R.id.btn_divide).setOnClickListener { showSymbol('/') }

        findViewById<Button>(R.id.btn_allClear).setOnClickListener { findViewById<TextView>(R.id.text_calculation).text = "" }
        findViewById<Button>(R.id.btn_openingBracket).setOnClickListener { showSymbol('(') }
        findViewById<Button>(R.id.btn_closingBracket).setOnClickListener { showSymbol(')') }
        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            val textCalc = findViewById<TextView>(R.id.text_calculation)
            textCalc.text = textCalc.text.dropLast(1)
        }
    }

    public fun showSymbol(symbol: Char) {
        val textCalc = findViewById<TextView>(R.id.text_calculation)
        val newText = textCalc.text.toString() + symbol
        textCalc.text = newText
    }

    public fun calculate() {
        val textCalc = findViewById<TextView>(R.id.text_calculation)
        val answer = calculateWithParentheses(textCalc.text.toString())
        textCalc.text = answer.toString()
    }

    fun evaluateExpression(expression: String): String {
        try {
            // Убираем лишние пробелы и проверяем, пустая ли строка
            val cleanedExpression = expression.replace("\\s+".toRegex(), "")
            if (cleanedExpression.isEmpty()) return "Ошибка: пустое выражение"

            // Парсим и вычисляем выражение
            val result = calculateWithParentheses(cleanedExpression)
            // Проверяем, является ли результат бесконечностью (например, деление на 0)
            return if (result.isFinite()) {
                // Убираем .0 для целых чисел
                if (result % 1 == 0.0) result.toLong().toString() else result.toString()
            } else {
                "Ошибка: деление на ноль"
            }
        } catch (e: Exception) {
            return "Ошибка: неверное выражение"
        }
    }

    // Основная функция вычисления с поддержкой скобок
    private fun calculateWithParentheses(expression: String): Double {
        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<Char>()
        var currentNumber = StringBuilder()
        var i = 0

        while (i < expression.length) {
            val char = expression[i]

            when {
                char.isDigit() || char == '.' -> currentNumber.append(char)
                char in listOf('+', '-', '*', '/') -> {
                    if (currentNumber.isNotEmpty()) {
                        numbers.add(currentNumber.toString().toDouble())
                        currentNumber = StringBuilder()
                    }
                    // Обрабатываем приоритет операций * и /
                    while (operators.isNotEmpty() && hasPrecedence(char, operators.last())) {
                        applyOperator(numbers, operators.removeAt(operators.size - 1))
                    }
                    operators.add(char)
                }
                char == '(' -> {
                    if (currentNumber.isNotEmpty()) {
                        numbers.add(currentNumber.toString().toDouble())
                        currentNumber = StringBuilder()
                    }
                    // Находим конец скобки и рекурсивно вычисляем выражение внутри
                    val closingIndex = findClosingParenthesis(expression, i)
                    if (closingIndex == -1) throw IllegalArgumentException("Незакрытая скобка")
                    val subExpression = expression.substring(i + 1, closingIndex)
                    val subResult = calculateWithParentheses(subExpression)
                    numbers.add(subResult)
                    i = closingIndex
                }
                char == ')' -> throw IllegalArgumentException("Лишняя закрывающая скобка")
                else -> throw IllegalArgumentException("Недопустимый символ")
            }
            i++
        }

        // Добавляем последнее число
        if (currentNumber.isNotEmpty()) {
            numbers.add(currentNumber.toString().toDouble())
        }

        // Выполняем оставшиеся операции
        while (operators.isNotEmpty()) {
            applyOperator(numbers, operators.removeAt(operators.size - 1))
        }

        return numbers[0]
    }

    // Находим индекс закрывающей скобки
    private fun findClosingParenthesis(expression: String, openIndex: Int): Int {
        var count = 1
        var i = openIndex + 1
        while (i < expression.length) {
            when (expression[i]) {
                '(' -> count++
                ')' -> {
                    count--
                    if (count == 0) return i
                }
            }
            i++
        }
        return -1 // Если не найдена закрывающая скобка
    }

    // Проверка приоритета операторов
    private fun hasPrecedence(op1: Char, op2: Char): Boolean {
        return (op2 == '*' || op2 == '/') && (op1 == '+' || op1 == '-')
    }

    // Применение оператора к числам
    private fun applyOperator(numbers: MutableList<Double>, operator: Char) {
        val b = numbers.removeAt(numbers.size - 1)
        val a = numbers.removeAt(numbers.size - 1)
        val result = when (operator) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> a / b
            else -> throw IllegalArgumentException("Недопустимый оператор")
        }
        numbers.add(result)
    }
}



