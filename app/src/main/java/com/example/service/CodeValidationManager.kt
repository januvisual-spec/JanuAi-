package com.example.service

data class CodeValidationResult(
    val isValid: Boolean,
    val issuesFound: List<String>,
    val suggestions: List<String>
)

class CodeValidationManager {

    fun validateCode(code: String, language: String): CodeValidationResult {
        val issues = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        if (code.isBlank()) {
            issues.add("Code snippet is empty.")
            return CodeValidationResult(false, issues, listOf("Provide or generate source code."))
        }

        when (language.uppercase()) {
            "KOTLIN", "JAVA" -> {
                // 1. Check Brace Balance
                val openBraces = code.count { it == '{' }
                val closeBraces = code.count { it == '}' }
                if (openBraces != closeBraces) {
                    issues.add("Unbalanced curly braces: $openBraces '{' vs $closeBraces '}'.")
                    suggestions.add("Ensure all open class or function blocks have corresponding closing braces.")
                }

                // 2. Check Parentheses Balance
                val openParens = code.count { it == '(' }
                val closeParens = code.count { it == ')' }
                if (openParens != closeParens) {
                    issues.add("Unbalanced parentheses: $openParens '(' vs $closeParens ')'.")
                    suggestions.add("Check function parameters or lambda arguments for missing parentheses.")
                }

                // 3. Package Declaration
                if (!code.contains("package ")) {
                    issues.add("Missing package declaration at file top.")
                    suggestions.add("Add 'package com.example...' at the beginning of the file.")
                }

                // 4. Missing Common Imports
                if (code.contains("@Composable") && !code.contains("androidx.compose.runtime.Composable")) {
                    suggestions.add("Ensure '@Composable' import 'androidx.compose.runtime.Composable' is present.")
                }
            }

            "PYTHON" -> {
                val openParens = code.count { it == '(' }
                val closeParens = code.count { it == ')' }
                if (openParens != closeParens) {
                    issues.add("Unbalanced parentheses: $openParens '(' vs $closeParens ')'.")
                }
                if (code.contains("def ") && !code.contains(":")) {
                    issues.add("Function definition appears to lack a colon ':'.")
                }
            }

            "XML" -> {
                val openTags = code.count { it == '<' }
                val closeTags = code.count { it == '>' }
                if (openTags != closeTags) {
                    issues.add("Unbalanced XML angle brackets: $openTags '<' vs $closeTags '>'.")
                }
                if (!code.contains("<?xml") && !code.contains("<manifest") && !code.contains("<LinearLayout")) {
                    suggestions.add("Verify the root XML tag is properly opened and closed.")
                }
            }
        }

        val isValid = issues.isEmpty()
        if (isValid) {
            suggestions.add("Code syntax validation passed. Ready for project integration.")
        }

        return CodeValidationResult(isValid, issues, suggestions)
    }
}
