package ru.mirea.tsybulko.lesson7

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import ru.mirea.tsybulko.lesson7.databinding.ActivityMainBinding
import java.io.IOException
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            val timeTask = TimeTask(binding).run {
                execute()
            }
        }
    }


    private class TimeTask(var binding: ActivityMainBinding) : AsyncTask<Void, Void, String>() {

        companion object {
            const val host = "time.nist.gov"
            const val port = 13

            const val tag = "timeServiceResult"
        }

        override fun doInBackground(vararg params: Void?): String {
            var timeResult = ""
            try {
                Socket(host, port).apply {
                    SocketUtils.getReader(this).apply {
                        readLine() // skip first line
                        timeResult = readLine() // read second line
                    }
                    Log.d(tag, timeResult)
                    close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return timeResult
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            binding.textView.text = splitIntoDateTime(result)
        }

        // 60101 23-06-06 20:54:45 50 0 0 476.6 UTC(NIST) *
        private fun splitIntoDateTime(toSplit: String?): String {
            val splitedInput = toSplit!!.split(" ")
            return "Date: ${splitedInput[1]}, Time: ${splitedInput[2]}"
        }
    }
}