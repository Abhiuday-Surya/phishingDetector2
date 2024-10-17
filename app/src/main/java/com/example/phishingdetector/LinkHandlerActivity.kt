package com.example.phishingdetector

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class LinkHandlerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_link_handler)

        // Get the incoming URL
        val intent = intent
        val url: Uri? = intent.data

        // If there's a valid URL, check if it's genuine
        if (url != null) {
            val urlString = url.toString()
            checkLinkGenuineness(urlString)
        }
    }

    private fun checkLinkGenuineness(url: String) {
        // Display loading screen while checking
        findViewById<TextView>(R.id.textView).text = "Checking URL..."

        // Example of calling an API to check if the link is genuine (pseudo-code)
        GlobalScope.launch(Dispatchers.IO) {
            val isGenuine = checkUrlWithApi(url) // Make API call to verify URL

            withContext(Dispatchers.Main) {
                if (isGenuine) {
                    findViewById<TextView>(R.id.textView).text = "This URL is genuine."
                    openLinkInBrowser(url)
                } else {
                    findViewById<TextView>(R.id.textView).text =
                        "Warning! This URL may be malicious."
                }

                // Wait a few seconds to show the message, then redirect to the browser

            }
        }
    }

    private suspend fun checkUrlWithApi(url: String): Boolean {
        // Call your URL-checking API here
        // (For example, you could use the IPQualityScore API or any similar API)
        // Here, we'll simulate an API call with a delay and a random result for simplicity
        //delay(1000)
        //return (0..1).random() == 0 // Randomly return true or false as a placeholder

        val retrofit = Retrofit.Builder()
            .baseUrl("https://ipqualityscore.com/api/json/url/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(PhishingDetectionService::class.java)

//        try {
//            val response = service.checkUrl(API_KEY, url)
//            return !response.unsafe
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // In case of an error, we'll assume the URL is unsafe
//            return false
//        }
        return try {
            val response = service.checkUrl(API_KEY, url)
            //Log.d("LinkHandlerActivity", "API Response: $response")

            if (response.success) {
                // Consider the URL safe if it's not unsafe, suspicious, phishing, malware, or spamming
                !(response.unsafe || response.suspicious || response.phishing || response.malware || response.spamming || response.adult)
            } else {
                // If the API call wasn't successful, assume it's unsafe
                false
            }
        } catch (e: Exception) {
            //Log.e("LinkHandlerActivity", "Error in API call", e)
            // In case of an error, we'll assume the URL is unsafe
            false
        }
    }


    private fun openLinkInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setPackage("com.android.chrome")
        startActivity(intent)
        finish() // Close the activity after redirecting
    }

    companion object {
        private const val API_KEY = "ZfQShFIh1Mm4hDBaJDZLasVeI1RNKlbb"
    }
}

interface PhishingDetectionService {
    @GET("{api_key}")
    suspend fun checkUrl(
        @Path("api_key") apiKey: String,
        @Query("url") url: String
    ): PhishingCheckResponse
}

data class PhishingCheckResponse(
    val message: String,
    val success: Boolean,
    val unsafe: Boolean,
    val domain: String,
    @SerializedName("root_domain") val rootDomain: String,
    @SerializedName("ip_address") val ipAddress: String,
    @SerializedName("country_code") val countryCode: String,
    @SerializedName("language_code") val languageCode: String,
    val server: String,
    @SerializedName("content_type") val contentType: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("page_size") val pageSize: Int,
    @SerializedName("domain_rank") val domainRank: Int,
    @SerializedName("dns_valid") val dnsValid: Boolean,
    val parking: Boolean,
    val spamming: Boolean,
    val malware: Boolean,
    val phishing: Boolean,
    val suspicious: Boolean,
    @SerializedName("domain_trust") val domainTrust: String,
    val adult: Boolean,
    @SerializedName("risk_score") val riskScore: Int,
    val category: String,
    val redirected: Boolean,
    @SerializedName("request_id") val requestId: String
)


