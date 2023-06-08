package ru.mirea.tsybulko.httpurlconnection

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Bundle
import android.os.Debug
import android.service.carrier.CarrierIdentifier
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONException
import org.json.JSONObject
import ru.mirea.tsybulko.httpurlconnection.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var taskIdentifier: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonWeather.isEnabled = false

        binding.buttonData.setOnClickListener {
            val connectivityManager: ConnectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo: NetworkInfo = connectivityManager.activeNetworkInfo!!
            if (networkInfo.isConnected) {
                DownloadPageTask(binding, "IP").execute("https://ipinfo.io/json")
            } else {
                Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonWeather.setOnClickListener {
            val coordinates = binding.textViewCoordinates.text.split(",").dropLastWhile { it.isEmpty() }.toTypedArray()
            DownloadPageTask(
                binding,
                "Weather"
            ).execute("https://api.open-meteo.com/v1/forecast?latitude=${coordinates[0]}&longitude=${coordinates[1]}&current_weather=true")
        }
    }

    private class DownloadPageTask(var binding: ActivityMainBinding, var taskIdentifier: String) :
        AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String {
            return try {
                downloadInfo(params[0]!!)
            } catch (e: IOException) {
                e.printStackTrace()
                "error"
            }
        }

        override fun onPreExecute() {
            if (taskIdentifier == "IP") {
                binding.run {
                    "Loading...".also {
                        textViewIp.text = it
                        textViewCity.text = it
                        textViewCountry.text = it
                        textViewRegion.text = it
                        textViewWeather.text = it
                        textViewCoordinates.text = it
                    }
                }
            }

            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            Log.d(MainActivity::class.java.simpleName, result!!)
            try {
                Log.d("Errors", result)
                val responseJson = JSONObject(result)
                Log.d(
                    MainActivity::class.java.simpleName,
                    "Response: $responseJson"
                )
                if (taskIdentifier == "IP") {
                    val ip = responseJson.getString("ip")
                    val coordinates = responseJson.getString("loc").split(",")
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()


                    val latitude = coordinates[0]
                    val longitude = coordinates[1]
                    binding.textViewIp.text = responseJson.getString("ip")
                    binding.textViewCity.text = responseJson.getString("city")
                    binding.textViewCountry.text = responseJson.getString("country")
                    binding.textViewRegion.text = responseJson.getString("region")

                    binding.textViewCoordinates.text = "$latitude,$longitude"

                    binding.buttonWeather.isEnabled = true
                    Log.d(
                        MainActivity::class.java.simpleName,
                        "IP: $ip"
                    )
                }
                if (taskIdentifier == "Weather") {
                    val weatherJsonData = responseJson.getJSONObject("current_weather")
                    val weather = String.format(
                        "Temperature:%s, wind:%s",
                        weatherJsonData.getString("temperature"),
                        weatherJsonData.getString("windspeed")
                    )
                    binding.textViewWeather.text = weather
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            super.onPostExecute(result)
        }

        private fun downloadInfo(address: String): String {
            var inputStream: InputStream? = null
            var data = ""
            try {
                (URL(address).openConnection() as HttpURLConnection).apply {
                    readTimeout = 100000
                    connectTimeout = 100000
                    requestMethod = "GET"
                    instanceFollowRedirects = true
                    useCaches = false
                    doInput = true

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        inputStream = getInputStream()

                        val bos = ByteArrayOutputStream()
                        var read = 0
                        while (inputStream!!.read().also { read = it } != -1) {
                            bos.write(read)
                        }
                        bos.close()
                        data = bos.toString()
                        disconnect()
                    } else {
                        data = "$responseMessage. Error Code: $responseCode"
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (inputStream != null) {
                    inputStream!!.close()
                }
            }
            return data
        }

    }
}