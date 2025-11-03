package com.reown.sign.engine.model.tvf

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONObject

@JsonClass(generateAdapter = true)
data class TransactionResponse(
    val result: TransactionResult
)

@JsonClass(generateAdapter = true)
data class TransactionResult(
    val txID: String,
    val signature: List<String>?,
    val raw_data: RawData?,
    val visible: Boolean?,
    val raw_data_hex: String?
)

@JsonClass(generateAdapter = true)
data class RawData(
    val expiration: Long?,
    val contract: List<Contract>?,
    val ref_block_hash: String?,
    val fee_limit: Long?,
    val timestamp: Long?,
    val ref_block_bytes: String?
)

@JsonClass(generateAdapter = true)
data class Contract(
    val parameter: Parameter?,
    val type: String?
)

@JsonClass(generateAdapter = true)
data class Parameter(
    val type_url: String?,
    @Json(name = "value")
    val value: ContractValue?
)

@JsonClass(generateAdapter = true)
data class ContractValue(
    val data: String?,
    val contract_address: String?,
    val owner_address: String?
)

// Request parameter data classes
@JsonClass(generateAdapter = true)
data class TronSignTransactionRequest(
    val address: String,
    val transaction: TronTransactionWrapper
)

@JsonClass(generateAdapter = true)
data class TronTransactionWrapper(
    val result: TronTransactionResult? = null,
    val transaction: TransactionResult? = null  // Reuse TransactionResult for the actual transaction
)

@JsonClass(generateAdapter = true)
data class TronTransactionResult(
    val result: Boolean? = null
)

object TronRequestNormalizer {
    
    /**
     * Normalizes TRON sign transaction request params to ensure consistent structure.
     * Converts nested format (transaction.transaction) to direct format (transaction).
     * 
     * This normalizer is only used in wallet applications (walletkit).
     * 
     * Input format 1 (nested):
     *   { "address": "...", "transaction": { "result": {...}, "transaction": {...} } }
     * 
     * Input format 2 (direct):
     *   { "address": "...", "transaction": {...} }
     * 
     * Output format (normalized):
     *   { "address": "...", "transaction": {...} }
     * 
     * @param paramsJson Original params JSON string
     * @param namespace The namespace extracted from chainId (e.g., "tron") - unused, kept for API compatibility
     * @return Normalized params JSON string with direct transaction format, or original params if parsing fails
     */
    fun normalize(paramsJson: String, namespace: String? = null): String {
        return try {
            val paramsObj = JSONObject(paramsJson)
            
            // Validate required fields
            if (!paramsObj.has("address") || !paramsObj.has("transaction")) {
                return paramsJson
            }
            
            val address = paramsObj.getString("address")
            val transactionObj = paramsObj.getJSONObject("transaction")
            
            // Check if nested format exists: transaction.transaction
            if (transactionObj.has("transaction") && transactionObj.get("transaction") is JSONObject) {
                // Nested format: extract inner transaction
                val innerTransaction = transactionObj.getJSONObject("transaction")
                
                // Create normalized structure
                val normalizedParams = JSONObject().apply {
                    put("address", address)
                    put("transaction", innerTransaction)
                }
                
                normalizedParams.toString()
            } else {
                // Already in direct format, return as-is
                paramsJson
            }
        } catch (e: Exception) {
            // If parsing fails, return original params
            paramsJson
        }
    }
}