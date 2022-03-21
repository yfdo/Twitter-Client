package com.codepath.apps.restclienttemplate

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers

class ComposeActivity : AppCompatActivity() {
    lateinit var etCompose: EditText
    lateinit var btnTweet: Button
    lateinit var client: TwitterClient
    lateinit var tvWordCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)

        etCompose = findViewById(R.id.etTweetCompose)
        btnTweet = findViewById(R.id.btnTweet)
        client = TwitterApplication.getRestClient(this)
        tvWordCount = findViewById(R.id.tvWordCount)

        //updates word count
        etCompose.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Fires right as the text is being changed (even supplies the range of text)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Fires right before text is changing
            }

            override fun afterTextChanged(s: Editable) {
                // Fires right after the text has changed
                tvWordCount.text = s.length.toString() + "/280"
                //if exceeds max count
                if (s.length > 280) {
                    tvWordCount.setTextColor(Color.RED)
                    //disable submit button
                    btnTweet.isEnabled = false;
                    btnTweet.isClickable = false;
                } else {
                    tvWordCount.setTextColor(Color.DKGRAY)
                    btnTweet.isEnabled = true;
                    btnTweet.isClickable = true;
                }
            }
        })

        //when user clicks on button
        btnTweet.setOnClickListener {
            //grab the content of edittext(etCompose)
            val tweetContent = etCompose.text.toString()

            //check corner cases
            //1. make sure tweet isnt empty
            if (tweetContent.isEmpty()) {
                Toast.makeText(this, "Empty tweets are not allowed!", Toast.LENGTH_SHORT).show()
                //look into displaying SnackBar message
            }
            //2. make sure tweet is under character count
            else if (tweetContent.length > 280) {
                Toast.makeText(
                    this,
                    "Tweet is too long! Limit is 280 characters",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, tweetContent, Toast.LENGTH_SHORT).show()

                //make api call to publish tweet
                client.publishTweet(tweetContent, object : JsonHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                        Log.i(TAG, "Successfully published tweet!")
                        //send the tweet back to TimelineActivity to show
                        val tweet = Tweet.fromJson(json.jsonObject)
                        val intent = Intent()
                        intent.putExtra("tweet", tweet)
                        setResult(RESULT_OK, intent)
                        finish()
                    }

                    override fun onFailure(
                        statusCode: Int,
                        headers: Headers?,
                        response: String?,
                        throwable: Throwable?
                    ) {
                        Log.e(TAG, "Failed to publish tweet", throwable)
                    }

                })
            }
        }
    }

    companion object {
        val TAG = "ComposeActivity"
    }
}