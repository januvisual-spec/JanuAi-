package com.example.service

import com.example.data.repository.JanuRepository

class ProjectMemoryManager(private val repository: JanuRepository) {

    suspend fun saveProjectDecision(topic: String, decision: String) {
        repository.saveMemory("project_$topic", decision, "PROJECT_ARCHITECTURE")
    }

    suspend fun getProjectDecisions(): List<Pair<String, String>> {
        val memories = repository.getMemoryByKey("project_architecture")
        return listOf()
    }

    suspend fun recordCodeGeneration(template: String, description: String) {
        repository.saveMemory("last_code_gen", "$template: $description", "CODE_HISTORY")
    }
}
