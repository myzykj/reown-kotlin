package com.reown.walletkit.client

import org.json.JSONObject

object SessionPropertiesConfig {
    
    private val config: JSONObject by lazy {
        try {
            val jsonString = this::class.java.classLoader
                .getResourceAsStream("SessionProperties.json")
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: "{}"
            JSONObject(jsonString)
        } catch (e: Exception) {
            JSONObject()
        }
    }
    
    /**
     * Returns properties for namespaces that exist both in the provided namespaces set
     * and in the configuration file.
     * 
     * For example, if namespaces contains "tron" and config has "tron" entry,
     * it will return properties defined for "tron" in the config.
     * If namespaces doesn't contain "tron", no properties will be returned for "tron".
     * 
     * @param namespaces Set of namespace keys to check in the configuration
     * @return Map of property key-value pairs for matching namespaces
     */
    fun getPropertiesForNamespaces(namespaces: Set<String>): Map<String, String> =
        buildMap {
            for (namespace in namespaces) {
                val namespaceConfig = config.optJSONObject(namespace) ?: continue
                namespaceConfig.keys().forEach { key ->
                    try {
                        put(key, namespaceConfig.getString(key))
                    } catch (e: Exception) {
                        // Skip invalid property values
                    }
                }
            }
        }
}

