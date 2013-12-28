package com.nebel_tv.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.nebel_tv.R;
import com.nebel_tv.activity.base.BaseActivity;
import com.nebel_tv.github.GitHubIssueAsyncTask.FeedbackType;
import com.nebel_tv.utils.UIUtils;

public class FeedbackActivity extends BaseActivity {
	
	public static void launch(Context c) {
		Intent intent = new Intent(c, FeedbackActivity.class);
		c.startActivity(intent);
	}
	
	private EditText emailInput;
	private Spinner feedbackTypeSpinner;
	private EditText feedbackTextInput;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
		
		emailInput = (EditText) findViewById(R.id.input_email);
		feedbackTextInput = (EditText) findViewById(R.id.input_feedback_text);
		feedbackTypeSpinner = (Spinner) findViewById(R.id.spinner_feedback_type);
		TextView emailTitle = (TextView) findViewById(R.id.txt_feedback_email_title);
		
		//building feedback email title with two string parts of different size
		String bigPart = getString(R.string.feedback_email_title_big_part);
		String smallPart = getString(R.string.feedback_email_title_small_part);
		String feedbackEmailTitle = bigPart+" "+smallPart;
		Spannable span = new SpannableString(feedbackEmailTitle);
		span.setSpan(new RelativeSizeSpan(0.7f), bigPart.length(), feedbackEmailTitle.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		emailTitle.setText(span);
		
		ArrayAdapter<String> adapter = 
			new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, FeedbackType.getFeedBackTypeTitles(this));
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		feedbackTypeSpinner.setAdapter(adapter);
	}
	
	public void onConfirmClick(View v) {
		String email = emailInput.getText().toString();
		FeedbackType feedbackType = FeedbackType.values()[feedbackTypeSpinner.getSelectedItemPosition()];
		String feedbackText = feedbackTextInput.getText().toString();
		
		if(feedbackText!=null && !"".equals(feedbackText)) {
//			new GitHubIssueAsyncTask(
//					this, feedbackText, email, feedbackType).execute();
			onBackPressed();
		} else {
			UIUtils.showMessage(R.string.feedback_text_error_msg);
		}
	}

}
